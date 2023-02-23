package org.Aizuked.TestObjPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Man extends Animal {
    private String name;
    private Integer age;
    private List<String> favoriteBooks;
    private int[] neighboringRoomNumbers;
    private String[] favoriteIceCreamToppings;
    private ArrayList<Animal> animals;
    private Man self;

    public Man(String name, int age, List<String> favoriteBooks) {
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

    @Override
    public String toString() {
        return "Man{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", favoriteBooks=" + favoriteBooks +
                ", neighboringRoomNumbers=" + Arrays.toString(neighboringRoomNumbers) +
                ", favoriteIceCreamToppings=" + Arrays.toString(favoriteIceCreamToppings) +
                '}';
    }
}
