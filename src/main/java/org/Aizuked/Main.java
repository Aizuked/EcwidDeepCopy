package org.Aizuked;

import org.Aizuked.CopyUtils.DeepCopyUtil;
import org.Aizuked.TestObjPackage.Animal;
import org.Aizuked.TestObjPackage.Cat;
import org.Aizuked.TestObjPackage.Dog;
import org.Aizuked.TestObjPackage.Man;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        Man man1 = new Man("Robert", 18, List.of("Book1", "Book2", "Book3"));

        man1.setNeighboringRoomNumbers(new int[]{10, 20});
        man1.setFavoriteIceCreamToppings(new String[]{"Chocolate", "Vanilla"});

        ArrayList<String> testArrayList = new ArrayList<>();
        testArrayList.add("first");
        testArrayList.add("second");
        man1.setTestArrayList(testArrayList);

        man1.setTestMap(Map.of("qwe", "ewq", "asd", "dsa"));

        HashMap<Integer, Integer> testHashMap = new HashMap<>();
        testHashMap.put(128, 129);
        testHashMap.put(130, 131);
        man1.setTestHashMap(testHashMap);

        Set<String> testSet = Set.of("true", "false");
        man1.setTestSet(testSet);

        HashSet<String> testHashSet = new HashSet<>();
        testHashSet.add("true");
        testHashSet.add("false");
        man1.setTestHashSet(testHashSet);

        man1.setNumberOfLegs((short)2);
        man1.setAbleToSpeak(true);
        man1.setParentNames(List.of("Emma", "John"));
        man1.setNumberOfLegs((short)129);
        man1.setOwner(man1);

        ArrayList<Animal> animals = new ArrayList<>();
        Cat cat = new Cat();
        cat.setAbleToSpeak(false);
        cat.setOwner(man1);
        cat.setNumberOfLegs((short)4);
        cat.setParentNames(List.of("Puss in boots 1", "Puss in boots 2"));
        animals.add(cat);

        Dog brian = new Dog();
        brian.setAbleToSpeak(true);
        brian.setOwner(cat);
        brian.setNumberOfLegs((short) 3);
        brian.setParentNames(new ArrayList<>(Arrays.asList("Isabella", "Quqly")));
        animals.add(brian);

        man1.setAnimals(animals);

        Man man2 = null;

        try {
            man2 = DeepCopyUtil.deepCopy(man1);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        System.out.println();
    }
}