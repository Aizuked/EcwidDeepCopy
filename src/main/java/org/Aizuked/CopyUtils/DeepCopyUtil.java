package org.Aizuked.CopyUtils;

import org.Aizuked.TestObjPackage.Man;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DeepCopyUtil {
    public static <T> T deepCopy(T obj) {
        if (obj == null)
            return null;

        T newObj = instantiateNewCopyObj(obj);
        //Заполнить T;

        return newObj;
    }

    private static <T> T instantiateNewCopyObj(T original) {
        T copy;

        try {
            Man src = new Man("ro", 2004, List.of("asd", "bfe"));
            Man dst = new Man("as", 1, List.of("123"));

            //Массивы
            src.setNeighboringRoomNumbers(new int[] {1, 2});
            Field field = Man.class.getDeclaredField("neighboringRoomNumbers");
            field.setAccessible(true);
            Object srcArr = field.get(src);
            int len = Array.getLength(srcArr);
            Object array = Array.newInstance(field.getType().getComponentType(), len);
            for (int i = 0; i < len; i++) {
                Array.set(array, i, Array.get(srcArr, i));
            }
            field.set(dst, array);

            //Примитивные типы
            field = Man.class.getDeclaredField("age");
            field.setAccessible(true);
            field.set(dst, (Integer)field.get(src));

            //Обертки примитивных типов
            field = Man.class.getDeclaredField("age");
            field.setAccessible(true);
            Object obj = field.get(src);
            field.set(dst, (int) obj);

            //Коллекции


            //Интерфейсы без конструктора, Set.of, List.of, Map.of
            //Set.of, List.of -> toArray
            //Map.of -> entrySet -> ofEntries
            field = Man.class.getDeclaredField("favoriteBooks");
            field.setAccessible(true);
            List<?> tempList = (List<?>) field.get(src);

            List<Object> listOfInts = List.of(tempList.toArray());
            field.set(dst, listOfInts);
            System.out.println();


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //V  Проверка на примитивный тип данных -> //Примитивные типы
        //V  Проверка на примитивную упаковку -> //Обертки примитивных типов
        //V  Проверка на массивы -> //Массивы
        //X  Проверка на коллекции -> //Коллекции
        //V  Проверка на интерфейсы без конструктора
        //X  Проверка на сложный тип Man или (List.of(List.of("1"), List.of("2")) -> deepCopy()
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
        }

        return blankArgs;
    }

    private static Object createNewObj(boolean isNull, Object o) {
        return null;
    }

    private static class ConstructorsNotFoundException extends RuntimeException {
        ConstructorsNotFoundException(String... msg) {
            super(Arrays.toString(msg));
        }
    }

}
