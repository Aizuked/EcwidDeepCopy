package org.Aizuked.CopyUtils;

import org.Aizuked.TestObjPackage.Man;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
    //HashCode объекта поля изначального объекта -> {Field              : Object}
    //  используется при проверке для копирования    объекта копирования  референс скопированного объекта
    private static Integer originalObjHashCode;
    private static final ThreadLocal<HashMap<Integer, HashMap<Field, Object>>> originallySelfReferencedObjects = new ThreadLocal<>();

    public static <T> T deepCopy(T o) {
        if (o == null)
            return null;

        if (originalObjHashCode == null)
            originalObjHashCode = System.identityHashCode(o);

        //Первый метод.
        T newObj = null;
        fillNewObj(o, newObj);


        //Добавление в allReferencesOfCopiedObjects.
        //Проверка является ли это оригинальным объектом ->
        //      (проверить плейсхолдеры | для примитивов соответствие с оригинальным объектом) в originallySelfReferencedObjects

        //Требуется чистить selfReferencedObjects после возвращение изначально запрашиваемого объекта.
        if (originalObjHashCode == System.identityHashCode(o)) {
            originallySelfReferencedObjects.get().clear();
        }
        return newObj;
    }

    private static void getSelfReferences(Object o) throws IllegalAccessException {
        //System.identityHashCode не гарантирует полную уникальность хэш-кодов.
        ArrayList<Integer> toCheckHashCodes = new ArrayList<>();
        ArrayList<Integer> selfReferences = new ArrayList<>();
        if (!(o instanceof Map<?, ?> || o instanceof Collection<?> || isWrapper(o))) {
            for (Field field : o.getClass().getFields()) {
                if (!Modifier.isFinal(field.getModifiers()) && !field.getClass().isPrimitive()) {
                    Object objToCheck = field.get(o);
                    if (objToCheck != null) {
                        int currHashCode = System.identityHashCode(objToCheck);
//                        if (toCheckHashCodes.contains(currHashCode) &&
//                                !selfReferenceFields.get().contains(currHashCode)) {
//                            selfReferences.add(currHashCode);
//                        } else {
//                            toCheckHashCodes.add(currHashCode);
//                        }
                    }
                }
            }
        } else {

        }
        //selfReferenceFields.get().addAll(selfReferences);
    }

    private static boolean isWrapper(Object o) {
        Class<?> type = o.getClass();
        return type == Double.class || type == Float.class ||
                type == Long.class || type == Integer.class || type == Short.class ||
                type == Character.class || type == Byte.class || type == Boolean.class;
    }

    private static <T> T fillNewObj(T src, T dst) {
        try {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dst;
    }

    /***/
    private static Constructor<?> getLeastArgsObjConstructor(Object o) {
        Constructor<?>[] constructors = o.getClass().getConstructors();
        Constructor<?> shortestCtor = constructors[0];

        for (var ctor : constructors) {
            if (ctor.getParameterTypes().length < shortestCtor.getParameterTypes().length) {
                shortestCtor = ctor;
            }
        }

        return shortestCtor;
    }

    /***/
    private static Object[] createBlankArgsForGivenConstructor(Constructor<?> ctor) {
        Class<?>[] ctorParamTypes = ctor.getParameterTypes();
        Object[] blankArgs = new Object[ctorParamTypes.length];

        for (int i = 0; i < ctorParamTypes.length; i++) {
            blankArgs[i] = ctorParamTypes[i].isPrimitive() ?
                    createPlaceHolderPrimitive(ctorParamTypes[i]) : null;
        }

        return blankArgs;
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

    private static Object createObjCopy(Object o) {
        Object newObj = null;
        Class<?> oClass = o.getClass();

/*        if (isWrapper(o)) {
            return createPrimitive(o);
        } else if () {

        }*/

        return newObj;

        //X  Ссылка на себя
        //М  Проверка на примитивный тип данных
        //М  Проверка на примитивную упаковку
        //М  Проверка на массивы
        //М  Проверка на коллекции
        //М  Проверка на интерфейсы без конструктора
        //М  Проверка на строки
        //X  Проверка на сложный тип Man -> deepCopy() else -> deepCopy()

    }

    /***/
    public static Object createFilledArray(Object o) {
        //Возможно нужно дергать param1 = field.getType().getComponentType() для Array.newInstance(param1, arrLen)
        int arrLen = Array.getLength(o);
        Object array = Array.newInstance(o.getClass().getComponentType(), arrLen);

        for (int i = 0; i < arrLen; i++) {
            Array.set(array, i, deepCopy(Array.get(o, i)));
        }

        return array;
    }

    /***/
    private static Object createFilledCollection(Object o) {
        Constructor<?> testCtor = getLeastArgsObjConstructor(o);
        Collection copy = null;

        try {
            copy = (Collection) testCtor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (var entry : (Collection) o) {
            copy.add(deepCopy(entry));
        }
        return copy;
    }

    /***/
    private static Object createFilledMap(Object o) {
        Map<?, ?> mapObject = (Map<?, ?>) o;
        return mapObject
                .entrySet()
                .stream()
                .collect(Collectors.toMap(deepCopy(Map.Entry::getKey), deepCopy(Map.Entry::getValue)));
    }

    /***/
    private static Object createFilledImmutableCollection(Object o) {
        //Поскольку .of методы возвращают Immutable объекты не обязательно делать deepCopy?
        if (o instanceof List<?> copy) {
            return List.of(deepCopy(copy.toArray()));
        } else if (o instanceof Set<?> copy) {
            return Set.of(deepCopy(copy.toArray()));
        } else if (o instanceof Map<?, ?> copy) {
            return Map.ofEntries((Map.Entry<?, ?>[]) copy.entrySet()
                    .stream()
                    .map(i -> Map.entry(deepCopy(i.getKey()), deepCopy(i.getValue())))
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
        copy = String.copyValueOf(copy.toCharArray());
        return copy;
    }


}
