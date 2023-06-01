package org.Aizuked.TestObjPackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Man {
    private String name;
    private int age;
    private ArrayList<String> favoriteBooks;
    private int[] neighboringRoomNumbers;
    private String[] favoriteIceCreamToppings;
    private ArrayList<Animal> animals;
    private ArrayList<String> testArrayList;
    private HashMap<Integer, Integer> testHashMap;
    private LinkedHashSet<Double> testLinkedHashSet;
    private HashSet<String> testHashSet;
}
