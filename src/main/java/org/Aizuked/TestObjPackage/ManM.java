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
public class ManM extends AnimalM {
    private String name;
    private int age;
    private ArrayList<String> favoriteBooks;
    private int[] neighboringRoomNumbers;
    private String[] favoriteIceCreamToppings;
    private ArrayList<AnimalM> animals;
    private ManM self;
    private ArrayList<String> testArrayList;
    private Map<String, String> testMap;
    private HashMap<Integer, Integer> testHashMap;
    private LinkedHashSet<Double> testLinkedHashSet;
    private Set<String> testSet;
    private HashSet<String> testHashSet;
}
