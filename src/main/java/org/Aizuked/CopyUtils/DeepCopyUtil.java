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
    private static final ThreadLocal<ArrayList<Object>> deepCopyObjects = new ThreadLocal<>();
    private static final ThreadLocal<ArrayList<Integer>> selfReferenceFields = new ThreadLocal<>();

    public static <T> T deepCopy(T o) {
        if (o == null)
            return null;

        T newObj = instantiateNewCopyObj(o);
        fillNewObj(o, newObj);

        deepCopyObjects.get().add(newObj);
        if (deepCopyObjects.get().get(0) == newObj) {
            deepCopyObjects.set(new ArrayList<>());
            selfReferenceFields.set(new ArrayList<>());
        }
        return newObj;
    }

    private static void getSelfReferences(Object o) {
        //Не гарантирована уникальность хэш-кодов :(
        //Рекурсивно ресурсоёмко
        ArrayList<Integer> toCheckHashCodes = new ArrayList<>();
        ArrayList<Integer> selfReferences = new ArrayList<>();
        Object objToCheck = null;
        for (Field field : o.getClass().getFields()) {
            if (!Modifier.isFinal(field.getModifiers())) {
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
            Collection testCollection = (Collection) testCtor.newInstance();
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
            Constructor<?> testCtorMap = getLeastArgsObjConstructor(field.get(src));
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
            //Поскольку .of методы возвращают Immutable объекты не обязательно делать deepCopy??
            field = Man.class.getDeclaredField("favoriteBooks");
            field.setAccessible(true);
            List<?> tempList = (List<?>) field.get(src);

            List<Object> listOfInts = List.of(tempList.toArray());
            field.set(dst, listOfInts);
            System.out.println();


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

    private static Object createNewObj(Object o, Field fieldData, ArrayList<Integer> selfReferences) {
        boolean possibleSelfRef = selfReferences.size() > 0;
        boolean fillObj = fieldData != null;
        Object newObj = null;

        if (!possibleSelfRef) {
            Class<?> type = o.getClass();
            if (!type.isPrimitive() || !(type == Double.class || type == Float.class || type == Long.class ||
                    type == Integer.class || type == Short.class || type == Character.class ||
                    type == Byte.class || type == Boolean.class)) {
                Constructor<?> ctor = getLeastArgsObjConstructor(o);
                try {
                    newObj = ctor.newInstance(getLeastArgsObjConstructor(o));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (selfReferences.contains(System.identityHashCode(o))) {
                newObj = ;
            } else {

            }
        }

        //fillObj?


        //V  Ссылка на себя
        //V  Проверка на примитивный тип данных -> //Примитивные типы
        //V  Проверка на примитивную упаковку -> //Обертки примитивных типов
        //V  Проверка на массивы -> //Массивы
        //V  Проверка на коллекции -> //Коллекции
        //V  Проверка на интерфейсы без конструктора
        //X  Проверка на сложный тип Man -> deepCopy() else -> deepCopy()

        return newObj;
    }

    private static class ConstructorsNotFoundException extends RuntimeException {
        ConstructorsNotFoundException(String... msg) {
            super(Arrays.toString(msg));
        }
    }

}
