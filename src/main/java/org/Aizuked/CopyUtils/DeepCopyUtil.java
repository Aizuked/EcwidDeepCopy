package org.Aizuked.CopyUtils;

import org.Aizuked.TestObjPackage.Man;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class DeepCopyUtil {
    //Возможены оверхеды мб структуру на хэшах
    private static final ThreadLocal<HashMap<Integer, Object>> deepCopyObjects = new ThreadLocal<>();
    private static final ThreadLocal<ArrayList<Integer>> selfReferenceFields = new ThreadLocal<>();
    private static final ThreadLocal<HashMap<Field, Object>> stillNeedToGetReferenced = new ThreadLocal<>();

    public static <T> T deepCopy(T o) {
        if (o == null)
            return null;

        T newObj = instantiateNewCopyObj(o);
        fillNewObj(o, newObj);


        if (deepCopyObjects.get().size() != 0 && deepCopyObjects.get().get(0) == newObj) {
            //Добавить проверку на полное заполнение по хэшам
            //Для случая референса на себя, который не было возможности заполнить при изначальном проходе
            deepCopyObjects.set(new HashMap<>());
            selfReferenceFields.set(new ArrayList<>());
        }

        deepCopyObjects.get().put(System.identityHashCode(newObj), newObj);
        return newObj;
    }

    private static void getSelfReferences(Object o) {
        //Не гарантирована уникальность хэш-кодов :(
        //Рекурсивно ресурсоёмко
        ArrayList<Integer> toCheckHashCodes = new ArrayList<>();
        ArrayList<Integer> selfReferences = new ArrayList<>();
        Object objToCheck = null;
        for (Field field : o.getClass().getFields()) {
            if (!Modifier.isFinal(field.getModifiers()) && !field.getClass().isPrimitive()) {
                try {
                    objToCheck = field.get(o);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (objToCheck != null) {
                    int currHashCode = System.identityHashCode(objToCheck);
                    if (toCheckHashCodes.contains(currHashCode) &&
                            !selfReferenceFields.get().contains(currHashCode)) {
                        selfReferences.add(currHashCode);
                    } else {
                        toCheckHashCodes.add(currHashCode);
                    }
                }
            }
        }
        selfReferenceFields.get().addAll(selfReferences);
    }

    private static boolean isWrapper(Object o) {
        Class<?> type = o.getClass();
        return type == Double.class || type == Float.class ||
                type == Long.class || type == Integer.class || type == Short.class ||
                type == Character.class || type == Byte.class || type == Boolean.class;
    }

    private static <T> T fillNewObj(T src, T dst) {
        try {
            //V  Ссылка на себя
            //V  Проверка на примитивный тип данных -> //Примитивные типы
            //V  Проверка на примитивную упаковку -> //Обертки примитивных типов
            //V  Проверка на массивы -> //Массивы
            //V  Проверка на коллекции -> //Коллекции
            //V  Проверка на интерфейсы без конструктора
            //X  Проверка на сложный тип Man -> deepCopy() else -> deepCopy()

            //СДЕЛАТЬ ПРОВЕРКУ НА PROPERTIES.FINAL

            //Ссылка на себя || как сделать на часть себя??
            Field field = Man.class.getDeclaredField("self");
            field.setAccessible(true);
            if (field.get(src) == src) {
                field.set(dst, dst);
            }

            //Массивы
            field = Man.class.getDeclaredField("neighboringRoomNumbers");
            field.setAccessible(true);
            Object srcArr = field.get(src);
            int len = Array.getLength(srcArr);
            Object array = Array.newInstance(field.getType().getComponentType(), len);
            for (int i = 0; i < len; i++) {
                Array.set(array, i, deepCopy(Array.get(srcArr, i)));
            }
            field.set(dst, array);

            //Примитивные типы switch-case
            field = Man.class.getDeclaredField("age");
            field.setAccessible(true);
            field.set(dst, (Integer) field.get(src));

            //Обертки примитивных типов switch-case
            field = Man.class.getDeclaredField("age");
            field.setAccessible(true);
            Object obj = field.get(src);
            field.set(dst, (int) obj);

            //Коллекции кроме реализаций интерфейса Map
            ArrayList<String> testArrayList = new ArrayList<>();
            testArrayList.add("first");
            testArrayList.add("second");

            ArrayList<String> testArrayListOnList = new ArrayList<>(List.of("first", "second"));

            LinkedHashSet<Double> testLinkedHashSetOnSet = new LinkedHashSet<>(Set.of(1d, 0.2));

            LinkedHashSet<Double> testLinkedHashSet = new LinkedHashSet<>();
            testLinkedHashSet.add(1d);
            testLinkedHashSet.add(0.2);

            field = Man.class.getDeclaredField("testLinkedHashSet");
            field.setAccessible(true);
            Collection srcCollection = (Collection) field.get(src);
            Constructor<?> testCtor = getLeastArgsObjConstructor(field.get(src));
            Collection<Object> testCollection = (Collection<Object>) testCtor.newInstance();
            for (var entry : srcCollection) {
                testCollection.add(deepCopy(entry));
            }
            field.set(dst, testCollection);

            System.out.println("a");

            //Наследники Map
            HashMap<Integer, Integer> testHashMap = new HashMap<>();
            testHashMap.put(1, 2);
            testHashMap.put(3, 4);
            HashMap<Integer, Integer> testHashMapOnMap = new HashMap<>(Map.of(1, 2, 3, 4));


            field = Man.class.getDeclaredField("testHashMap");
            field.setAccessible(true);
            Map<?, ?> mapObject = (Map<?, ?>) field.get(src);
            Map<?, ?> newMap = mapObject
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(deepCopy(Map.Entry::getKey), deepCopy(Map.Entry::getValue)));
            field.set(dst, newMap);

            System.out.println("s");

            //Интерфейсы без конструктора, Set.of, List.of, Map.of
            //Set.of, List.of -> toArray
            //Map.of -> entrySet -> ofEntries
            //switch-case по типу



/*            if (possibleSelfRef) {
                int hashCodeToCheck = System.identityHashCode(o);
                Object copiedReference = deepCopyObjects.get().get(hashCodeToCheck);
                if (copiedReference != null) {
                    return needsToBeFilled ? copiedReference : null;
                }
                //Объект для референса ещё не был создан, но возможно что будет
                else if (selfReferenceFields.get().contains(hashCodeToCheck)) {
                    //Плейсхолдер для примитивов, null для объектов
                    stillNeedToGetReferenced.get().put(givenField, o);
                    if (isWrapper || oClass.isPrimitive())
                        return createPrimitive(o);
                    else
                        return null;
                }
            }*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dst;
    }

    private static <T> T instantiateNewCopyObj(T original) {
        T copy;
        original.getClass();

        Constructor<?> ctor = getLeastArgsObjConstructor(original);
        Object[] blankArgs = ctor.getParameterTypes().length != 0 ?
                createBlankArgsForGivenConstructor(ctor) : null;


        try {
            copy = (T) ctor.newInstance(blankArgs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return copy;
    }

    private static Constructor<?> getLeastArgsObjConstructor(Object obj) {
        Constructor<?>[] constructors = obj.getClass().getConstructors();

        if (constructors.length > 0) {
            AtomicReference<Constructor<?>> atomicConstructor = new AtomicReference<Constructor<?>>(constructors[0]);
            Arrays.stream(constructors).forEach(i -> {
                if (i.getParameterTypes().length < atomicConstructor.get().getParameterTypes().length)
                    atomicConstructor.set(i);
            });
            return atomicConstructor.get();
        } else
            throw new DeepCopyUtil.ConstructorsNotFoundException("No suitable constructor is found!");
    }

    private static Object[] createBlankArgsForGivenConstructor(Constructor<?> ctor) {
        Class<?>[] ctorParamTypes = ctor.getParameterTypes();
        Object[] blankArgs = new Object[ctorParamTypes.length];

        for (int i = 0; i < ctorParamTypes.length; i++) {
            //blankArgs[i] = createNewObj();
            if (ctorParamTypes[i].isPrimitive()) {

            }
        }

        return blankArgs;
    }

    private static Object createNewObj(Object o) {
        Object newObj = null;
        Class<?> oClass = o.getClass();

/*        if (isWrapper(o)) {
            return createPrimitive(o);
        } else if () {

        }*/

        return newObj;

        //V  Ссылка на себя
        //V  Проверка на примитивный тип данных -> //Примитивные типы
        //V  Проверка на примитивную упаковку -> //Обертки примитивных типов
        //V  Проверка на массивы -> //Массивы
        //V  Проверка на коллекции -> //Коллекции
        //V  Проверка на интерфейсы без конструктора
        //V  Проверка на строки
        //X  Проверка на сложный тип Man -> deepCopy() else -> deepCopy()

    }

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

    private static Object createFilledMap(Object o) {
        Map<?, ?> mapObject = (Map<?, ?>) o;
        Map<?, ?> newMap = mapObject
                .entrySet()
                .stream()
                .collect(Collectors.toMap(deepCopy(Map.Entry::getKey), deepCopy(Map.Entry::getValue)));
        return null;
    }
    private static Object createFilledImmutableCollection(Object o) {
        //Поскольку .of методы возвращают Immutable объекты не обязательно делать deepCopy?
        if (o instanceof List<?> copy) {
            return List.of(deepCopy(copy.toArray()));
        } else if (o instanceof Set<?> copy) {
            return Set.of(deepCopy(copy.toArray()));
        } else if (o instanceof Map<?, ?> copy) {
            return copy
                .entrySet()
                .stream()
                .collect(Collectors.toMap(deepCopy(Map.Entry::getKey), deepCopy(Map.Entry::getValue)));
        } else {
            return null;
        }
    }
    private static Object createPlaceHolderPrimitiveOrWrapper(Class<?> cls) {
        //Object o - из Constructor.
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

    private static Object createFilledPrimitiveOrWrapper(Object o) {
        //
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

    private static Object createFilledString(Object o) {
        //Так-то они неизменяемые, и пересоздаются в памяти по подобию данного референса,
        //но, возможно, сборщик мусора не сможет удалить объект если на его поле ссылаются?
        String copy = (String) o;
        copy = String.copyValueOf(copy.toCharArray());
        return copy;
    }

    private static class ConstructorsNotFoundException extends RuntimeException {
        ConstructorsNotFoundException(String... msg) {
            super(Arrays.toString(msg));
        }
    }

}
