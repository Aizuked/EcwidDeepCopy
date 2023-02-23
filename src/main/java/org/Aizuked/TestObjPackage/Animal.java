package org.Aizuked.TestObjPackage;

import java.util.List;

public abstract class Animal {
    protected String sound = "Unknown animal sound";
    private short numberOfLegs = 128;
    private boolean ableToSpeak;
    private List<String> parentNames;
    private Animal owner;

    public Animal getOwner() {
        return owner;
    }

    public void setOwner(Animal owner) {
        this.owner = owner;
    }

    public String speak() {
        return "Making sound...";
    }

    public short getNumberOfLegs() {
        return numberOfLegs;
    }

    public void setNumberOfLegs(short numberOfLegs) {
        this.numberOfLegs = numberOfLegs;
    }

    public boolean isAbleToSpeak() {
        return ableToSpeak;
    }

    public void setAbleToSpeak(boolean canSpeak) {
        this.ableToSpeak = canSpeak;
    }

    public List<String> getParentNames() {
        return parentNames;
    }

    public void setParentNames(List<String> parentNames) {
        this.parentNames = parentNames;
    }
}
