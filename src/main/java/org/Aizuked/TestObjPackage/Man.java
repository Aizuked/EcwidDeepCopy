package org.Aizuked.TestObjPackage;

import java.util.*;

public class Man extends Animal {
    private String name;
    private int age;
    private List<String> favoriteBooks;
    private int[] neighboringRoomNumbers;
    private String[] favoriteIceCreamToppings;
    private ArrayList<Animal> animals;
    private Man self;
    private ArrayList<String> testArrayList;
    private Map<String, String> testMap;
    private HashMap<Integer, Integer> testHashMap;
    private LinkedHashSet<Double> testLinkedHashSet;
    private Set<String> testSet;
    private HashSet<String> testHashSet;

    public Set<String> getTestSet() {
        return testSet;
    }

    public void setTestSet(Set<String> testSet) {
        this.testSet = testSet;
    }

    public HashSet<String> getTestHashSet() {
        return testHashSet;
    }

    public void setTestHashSet(HashSet<String> testHashSet) {
        this.testHashSet = testHashSet;
    }

    public Map<String, String> getTestMap() {
        return testMap;
    }

    public void setTestMap(Map<String, String> testMap) {
        this.testMap = testMap;
    }

    public ArrayList<String> getTestArrayList() {
        return testArrayList;
    }

    public void setTestArrayList(ArrayList<String> testArrayList) {
        this.testArrayList = testArrayList;
    }

    public HashMap<Integer, Integer> getTestHashMap() {
        return testHashMap;
    }

    public void setTestHashMap(HashMap<Integer, Integer> testHashMap) {
        this.testHashMap = testHashMap;
    }

    public LinkedHashSet<Double> getTestLinkedHashSet() {
        return testLinkedHashSet;
    }

    public void setTestLinkedHashSet(LinkedHashSet<Double> testLinkedHashSet) {
        this.testLinkedHashSet = testLinkedHashSet;
    }

    public Man(String name, int age, List<String> favoriteBooks) {
        this.sound = "human sounds";
        this.name = name;
        this.age = age;
        this.favoriteBooks = favoriteBooks;
        this.self = this;
    }

    public int[] getNeighboringRoomNumbers() {
        return neighboringRoomNumbers;
    }

    public void setNeighboringRoomNumbers(int[] neighboringRoomNumbers) {
        this.neighboringRoomNumbers = neighboringRoomNumbers;
    }

    public String[] getFavoriteIceCreamToppings() {
        return favoriteIceCreamToppings;
    }

    public void setFavoriteIceCreamToppings(String[] favoriteIceCreamToppings) {
        this.favoriteIceCreamToppings = favoriteIceCreamToppings;
    }

    public ArrayList<Animal> getAnimals() {
        return animals;
    }

    public void setAnimals(ArrayList<Animal> animals) {
        this.animals = animals;
    }

    public Man getSelf() {
        return self;
    }

    public void setSelf(Man self) {
        this.self = self;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<String> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }

}
