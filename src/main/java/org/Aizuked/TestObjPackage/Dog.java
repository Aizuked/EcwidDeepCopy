package org.Aizuked.TestObjPackage;

public class Dog extends Animal {
    public Dog(Animal owner) {
        this.setOwner(owner);
        this.sound = "Woof!";
    }
}
