package org.Aizuked.CopyUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * До Jep 411, в SecurityManager Policy:
 * grant  {
 * permission java.lang.reflect.ReflectPermission "suppressAccessChecks";
 * }, иначе Field::setAccessible() -> SecurityException.
 */
//агрегация - особый случай ассоциации

public class DeepCopyUtil {
    private static ThreadLocal<Integer> originalObjHashCode = ThreadLocal.withInitial(() -> 0);
    private static ThreadLocal<HashMap<Integer, Object>> originallySelfReferencedObjects = ThreadLocal.withInitial(HashMap::new);

    public static <T> T deepCopy(T o) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (o == null)
            return null;

        T newObj = null;
        Class<?> cls = o.getClass();
        int currentHashCode = System.identityHashCode(o);

        if (originalObjHashCode.get() == 0) {
            originalObjHashCode.set(currentHashCode);
        }

        if (originallySelfReferencedObjects.get().containsKey(currentHashCode)) {
            newObj = (T) originallySelfReferencedObjects.get().get(currentHashCode);
        } else if (isWrapper(o) || cls.isPrimitive()) {
            newObj = (T) createFilledPrimitiveOrWrapper(o);
        } else if (cls == String.class) {
            newObj = (T) createFilledString(o);
        } else if (cls.isArray()) {
            newObj = (T) createFilledArray(o);
        } else if (cls.getName().contains("java.util.ImmutableCollections$List") ||
                cls.getName().contains("java.util.ImmutableCollections$Set") ||
                cls.getName().contains("java.util.ImmutableCollections$MapN")) {
            newObj = (T) createFilledImmutableCollection(o);
        } else if (o instanceof Collection<?>) {
            newObj = (T) createFilledCollection(o);
        } else if (o instanceof Map<?, ?>) {
            newObj = (T) createFilledMap(o);
        } else {
            newObj = createBlankObjCopy(o);
            originallySelfReferencedObjects.get().putIfAbsent(currentHashCode, newObj);
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(newObj, deepCopy(field.get(o)));
                field.setAccessible(false);
            }
        }

        originallySelfReferencedObjects.get().putIfAbsent(System.identityHashCode(o), newObj);

        if (originalObjHashCode.get() == System.identityHashCode(o)) {
            //вытащить поля суперклассов до Object
            if (!newObj.getClass().isArray()) {
                Class<?> leveledSuperClass = newObj.getClass().getSuperclass();
                while (leveledSuperClass != Object.class) {
                    for (Field field : leveledSuperClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(newObj, deepCopy(field.get(o)));
                        field.setAccessible(false);
                    }
                    leveledSuperClass = leveledSuperClass.getSuperclass();
                }
            }

            originalObjHashCode.remove();
            originallySelfReferencedObjects.remove();
        }

        return newObj;
    }

    /***/
    private static <T> Constructor<?> getLeastArgsObjConstructor(T o) {
        return Arrays.stream(o.getClass().getConstructors())
                .min(Comparator.comparing(obj -> obj.getParameterTypes().length))
                .orElseThrow(RuntimeException::new);
    }

    /***/
    private static Object[] createBlankArgsForGivenConstructor(Constructor<?> ctor) {
        return Arrays.stream(ctor.getParameterTypes())
                .map(i -> i.isPrimitive() ? createPlaceHolderPrimitive(i) : null)
                .toArray();
    }

    /***/
    private static <T> T createBlankObjCopy(T o)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<T> ctor = (Constructor<T>) getLeastArgsObjConstructor(o);
        return ctor.newInstance(createBlankArgsForGivenConstructor(ctor));
    }

    /***/
    private static boolean isWrapper(Object o) {
        Class<?> type = o.getClass();
        return type == Double.class || type == Float.class ||
                type == Long.class || type == Integer.class || type == Short.class ||
                type == Character.class || type == Byte.class || type == Boolean.class;
    }

    /***/
    private static Object createPlaceHolderPrimitive(Class<?> cls) {
        if (cls == Integer.class || cls == int.class) {
            return (int) 0;
        } else if (cls == Byte.class || cls == byte.class) {
            return (byte) 0;
        } else if (cls == Short.class || cls == short.class) {
            return (short) 0;
        } else if (cls == Boolean.class || cls == boolean.class) {
            return (boolean) false;
        } else if (cls == Long.class || cls == long.class) {
            return (long) 0;
        } else if (cls == Float.class || cls == float.class) {
            return (float) 0;
        } else if (cls == Double.class || cls == double.class) {
            return (double) 0;
        } else if (cls == Character.class || cls == char.class) {
            return (char) ' ';
        } else {
            return null;
        }
    }

    /***/
    private static Object createFilledArray(Object o)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        //Возможно нужно дергать param1 = field.getType().getComponentType() для Array.newInstance(param1, arrLen)
        int arrLen = Array.getLength(o);
        Object array = Array.newInstance(o.getClass().getComponentType(), arrLen);

        for (int i = 0; i < arrLen; i++) {
            Array.set(array, i, deepCopy(Array.get(o, i)));
        }

        return array;
    }

    /***/
    private static Object createFilledCollection(Object o)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Collection copy = (Collection) getLeastArgsObjConstructor(o).newInstance();

        for (var entry : (Collection) o) {
            copy.add(deepCopy(entry));
        }

        return copy;
    }

    /***/
    private static Object createFilledMap(Object o)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Map copy = (Map) getLeastArgsObjConstructor(o).newInstance();

        for (var entry : ((Map<?, ?>) o).entrySet()) {
            copy.put(deepCopy(entry.getKey()), deepCopy(entry.getValue()));
        }

        return copy;
    }

    /***/
    private static Object createFilledImmutableCollection(Object o)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        //Поскольку .of методы возвращают Immutable объекты не обязательно делать deepCopy?
        if (o instanceof List<?> copy) {
            return List.of(deepCopy(copy.toArray()));
        } else if (o instanceof Set<?> copy) {
            return Set.of(deepCopy(copy.toArray()));
        } else if (o instanceof Map<?, ?> copy) {
            return Map.ofEntries((Map.Entry<?, ?>[]) copy.entrySet()
                    .stream()
                    .map(i -> {
                        try {
                            return Map.entry(deepCopy(i.getKey()), deepCopy(i.getValue()));
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray());
        } else {
            return null;
        }
    }

    /***/
    private static Object createFilledPrimitiveOrWrapper(Object o) {
        Class<?> classInQuestion = o.getClass();
        if (classInQuestion == Integer.class) {
            return (int) o;
        } else if (classInQuestion == Byte.class) {
            return (byte) o;
        } else if (classInQuestion == Short.class) {
            return (short) o;
        } else if (classInQuestion == Boolean.class) {
            return (boolean) o;
        } else if (classInQuestion == Long.class) {
            return (long) o;
        } else if (classInQuestion == Float.class) {
            return (float) o;
        } else if (classInQuestion == Double.class) {
            return (double) o;
        } else if (classInQuestion == Character.class) {
            return (char) o;
        } else {
            return null;
        }
    }

    /***/
    private static Object createFilledString(Object o) {
        //String - неизменяемый объект, пересоздаваемый в памяти по подобию изначального референса.
        //Сможет ли сбощик мусора уничтожить использующий одинаковую ссылку на строку неиспользуемый объект?
        String copy = (String) o;
        return String.copyValueOf(copy.toCharArray());
    }

}
