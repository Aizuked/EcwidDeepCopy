package org.Aizuked.TestObjPackage;

public class Cat extends Animal {
    public Cat(Animal owner) {
        this.setOwner(owner);
        this.sound = "Meow!";
    }
}
