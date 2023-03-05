package org.Aizuked.CopyUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Класс утилиты создания глубокой копии объекта, целиком основывающийся на средствах рефлексии языка Java.
 * <p>
 * Глубокая копия объекта подразумевает, что полученная копия не содержит ссылок на объект копирования.
 * Однако некоторые объекты, в силу внутреннего устройства JVM, при создании возвращают ссылку вместо создания нового
 * объекта. Примерами таких объектов являются: числа в диапазоне [-128; 127], строка "", объекты Class и т. д.
 * <p>
 * Важно отметить, что объекты, следующие шаблону проектирования Singleton, имеют смысл лишь при следовании
 * программистом парадигмы данного паттерна, следовательно не должны быть копированы данным способом.
 * <p>
 * При наличии в приложении действующего SecurityManager, рекомендуется ознакомиться с
 * <a href="https://openjdk.org/jeps/411">JEP 411: Deprecate the Security Manager for Removal</a>.
 * Иначе в файле конфигурации требуется предоставить разрешение на установку флага доступа объектам класса Field
 * во избежание <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/SecurityException.html">
 * SecurityException</a>.
 *
 * @author Aizuked
 * @version 1.0
 */


public class DeepCopyUtil {

    /**
     * Служит входной точкой для запуска процесса глубокого копирования объекта.
     * Метод нужен для сокрытия реализации хранения и обработки рекурсивно создаваемых референсов.
     *
     * @param o                               объект для копирования
     * @return newObj     полученный в результате глубокого копирования объект
     */
    public static <T> T deepCopy(T o)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return deepCopy(o, new HashMap<>());
    }

    /**
     * Возвращает глубокую копию передеанного в метод объекта, полученную средствами рефлексии языка.
     * <p>
     * T o является экземпляром: указателя на данные; примитивного типа данных; обёртки примитивного типа данных;
     * строки; массива произвольных элементов; объекта, произведённого одной из фабрик интерфейсов List, Set, Map;
     * наследника интерфейса Collection; наследника интерфейса Map; произвольного объекта, состоящим из приведённых
     * выше примитивов и структур хранения данных.
     * <p>
     * Приведённый статический метод моментально возвращает результат только в следующих случаях, когда T o является:
     * экземпляром указателя на уже скопированные данные (при рекурсином исполнении), примитивным типом данных,
     * обёрткой примитивного типа данных, строкой или наследником класса Object, не имеющим декларированных полей,
     * хранит null. Иначе код рекурсивно создаёт глубокую копию объекта и всех его полей.
     *
     * @param o                               объект для копирования
     * @param originallySelfReferencedObjects карта ссылок на уже созданные объекты
     * @return newObj     полученный в результате глубокого копирования объект
     * @throws InvocationTargetException s
     * @throws InstantiationException    s
     * @throws IllegalAccessException    s
     */

    private static <T> T deepCopy(T o, HashMap<Integer, Object> originallySelfReferencedObjects)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (o == null)
            return null;

        T newObj;
        Class<?> cls = o.getClass();
        int currentHashCode = System.identityHashCode(o);

        if (originallySelfReferencedObjects.containsKey(currentHashCode)) {
            newObj = (T) originallySelfReferencedObjects.get(currentHashCode);
        } else if (isWrapper(o) || cls.isPrimitive()) {
            newObj = (T) createFilledPrimitiveOrWrapper(o);
        } else if (cls == String.class) {
            newObj = o;
        } else if (cls.isArray()) {
            newObj = (T) createFilledArray(o, originallySelfReferencedObjects);
        } else if (cls.getName().contains("java.util.ImmutableCollections$List") ||
                cls.getName().contains("java.util.ImmutableCollections$Set") ||
                cls.getName().contains("java.util.ImmutableCollections$Map")) {
            newObj = (T) createFilledImmutableCollection(o, originallySelfReferencedObjects);
        } else if (o instanceof Collection<?>) {
            newObj = (T) createFilledCollection(o, originallySelfReferencedObjects);
        } else if (o instanceof Map<?, ?>) {
            newObj = (T) createFilledMap(o, originallySelfReferencedObjects);
        } else {
            newObj = createBlankObjCopy(o);
            originallySelfReferencedObjects.putIfAbsent(currentHashCode, newObj);
            for (Field field : cls.getDeclaredFields()) {
                boolean isInitiallyPrivate = Modifier.isPrivate(field.getModifiers());
                field.setAccessible(true);
                field.set(newObj, deepCopy(field.get(o), originallySelfReferencedObjects));

                if (isInitiallyPrivate)
                    field.setAccessible(false);
            }
            setSuperClassFields(newObj, o, originallySelfReferencedObjects);
        }

        originallySelfReferencedObjects.putIfAbsent(System.identityHashCode(o), newObj);

        return newObj;
    }

    /**
     * В случае наличия суперкласса(-ов) у объекта копии Object o, рекурсивно копирует значения их полей класса
     * копирования Object src при помощи DeepCopyUtil::deepCopy() и устанавливает полученные значения в
     * соотстветствующие поля объекта копии Object o.
     *
     * @param o                               объект копии
     * @param src                             объект объекта копирования
     * @param originallySelfReferencedObjects карта ссылок на уже созданные объекты
     */
    private static void setSuperClassFields(Object o, Object src, HashMap<Integer, Object> originallySelfReferencedObjects)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> leveledSuperClass = o.getClass().getSuperclass();
        while (leveledSuperClass != Object.class) {
            for (Field field : leveledSuperClass.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(o, deepCopy(field.get(src), originallySelfReferencedObjects));
                field.setAccessible(false);
            }
            leveledSuperClass = leveledSuperClass.getSuperclass();
        }
    }

    /**
     * Возвращает конструктор объекта Object o, принимающий наименьшее количество аргументов.
     *
     * @param o объект копирования
     * @return конструктор объекта копирования
     */
    private static <T> Constructor<?> getLeastArgsObjConstructor(T o) {
        return Arrays.stream(o.getClass().getConstructors())
                .min(Comparator.comparing(obj -> obj.getParameterTypes().length))
                .orElseThrow(RuntimeException::new);
    }

    /**
     * Возвращает массив объектов-пустышек для данного конструктора Constructor<?> ctor.
     * Объекты принимают значения null, примитивы исходя из логики, заложенной в
     * DeepCopyUtil::createPlaceHolderPrimitive().
     *
     * @param ctor конструктор объекта
     * @return массив объектов пустышек
     */
    private static Object[] createBlankArgsForGivenConstructor(Constructor<?> ctor) {
        return Arrays.stream(ctor.getParameterTypes())
                .map(i -> i.isPrimitive() ? createPlaceHolderPrimitive(i) : null)
                .toArray();
    }

    /**
     * Возвращает копию объекта T o, полученную использованием конструктора с наименьшим количеством элементов
     * данного объекта, полученную DeepCopyUtil::getLeastArgsObjConstructor(). Аргументы конструктора -
     * пустышки, получаемые в DeepCopyUtil::createBlankArgsForGivenConstructor().
     *
     * @param o объект копирования
     * @return пустая копия объекта o
     */
    private static <T> T createBlankObjCopy(T o)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<T> ctor = (Constructor<T>) getLeastArgsObjConstructor(o);
        return ctor.newInstance(createBlankArgsForGivenConstructor(ctor));
    }

    /**
     * Проверяет является ли переданный объект упаковкой примитивного типа.
     *
     * @param o объект для проверки
     * @return является ли объект o упаковкой примитивного типа
     */
    private static boolean isWrapper(Object o) {
        Class<?> type = o.getClass();
        return type == Double.class || type == Float.class ||
                type == Long.class || type == Integer.class || type == Short.class ||
                type == Character.class || type == Byte.class || type == Boolean.class;
    }

    /**
     * Возвращает обертку примитивного типа данных с заданным начальным значением.
     *
     * @param cls класс примитивного типа данных или обертки на примитивный тип данных
     * @return экземпляр обертки примитивного типа данных
     */
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

    /**
     * Возвращает копию массива Object o, элементы которого были глубоко скопированы посредством
     * DeepCopyUtil::deepCopy().
     *
     * @param o                               копируемый массив
     * @param originallySelfReferencedObjects карта ссылок на уже созданные объекты
     * @return глубокая копия копируемого массива o
     */
    private static Object createFilledArray(Object o, HashMap<Integer, Object> originallySelfReferencedObjects)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int arrLen = Array.getLength(o);
        Object array = Array.newInstance(o.getClass().getComponentType(), arrLen);

        for (int i = 0; i < arrLen; i++) {
            Array.set(array, i, deepCopy(Array.get(o, i), originallySelfReferencedObjects));
        }

        return array;
    }

    /**
     * Вовзращает копию коллекции Object o, элементы которой были глубоко скопированы посредством
     * DeepCopyUtil::deepCopy().
     *
     * @param o                               копируемая коллеция
     * @param originallySelfReferencedObjects карта ссылок на уже созданные объекты
     * @return глубокая копия копируемой коллекции
     */
    private static Object createFilledCollection(Object o, HashMap<Integer, Object> originallySelfReferencedObjects)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Collection copy = (Collection) getLeastArgsObjConstructor(o).newInstance();

        for (var entry : (Collection) o) {
            copy.add(deepCopy(entry, originallySelfReferencedObjects));
        }

        return copy;
    }

    /**
     * Вовзращает копию хэш-карты Object o, элементы которой были глубоко скопированы посредством
     * DeepCopyUtil::deepCopy().
     *
     * @param o                               копирумая хэш-карта
     * @param originallySelfReferencedObjects карта ссылок на уже созданные объекты
     * @return глубокая копия копируемой хэш-карты
     */
    private static Object createFilledMap(Object o, HashMap<Integer, Object> originallySelfReferencedObjects)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Map copy = (Map) getLeastArgsObjConstructor(o).newInstance();

        for (var entry : ((Map<?, ?>) o).entrySet()) {
            copy.put(deepCopy(entry.getKey(), originallySelfReferencedObjects), deepCopy(entry.getValue(), originallySelfReferencedObjects));
        }

        return copy;
    }

    /**
     * Возвращает копию неизменяемой коллекции или неизменяемой хэш-карты Object o, элементы которой
     * были глубоко скопированы посредством DeepCopyUtil::deepCopy().
     *
     * @param o                               копируемая неизменяемая коллеция
     * @param originallySelfReferencedObjects карта ссылок на уже созданные объекты
     * @return глубокая копия неизменяемой коллекции или неизменяемой хэш-карты
     */
    private static Object createFilledImmutableCollection(Object o, HashMap<Integer, Object> originallySelfReferencedObjects)
            throws InvocationTargetException, InstantiationException, IllegalAccessException {
        //Поскольку .of методы возвращают Immutable объекты не обязательно делать deepCopy?
        if (o instanceof List<?> copy) {
            return List.of(deepCopy(copy.toArray(), originallySelfReferencedObjects));
        } else if (o instanceof Set<?> copy) {
            return Set.of(deepCopy(copy.toArray(), originallySelfReferencedObjects));
        } else if (o instanceof Map<?, ?> copy) {
            Object copiedEntries = Array.newInstance(Map.Entry.class, copy.size());
            int i = 0;
            for (var entry : copy.entrySet()) {
                Array.set(copiedEntries, i, Map.entry(deepCopy(entry.getKey(), originallySelfReferencedObjects), deepCopy(entry.getValue(), originallySelfReferencedObjects)));
                i++;
            }
            return Map.ofEntries((Map.Entry<?, ?>[]) copiedEntries);
        } else {
            return null;
        }
    }

    /**
     * Возвращает копию примитивного типа или обёртки примитивного типа Object o освновываясь на
     * свойствах автоупаковки компилятором примитивов и логике их создания.
     *
     * @param o копируемый примитив или обёртка примитива
     * @return новый объект упаковки примитива
     */
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

}
