package org.Aizuked.TestObjPackage;

import lombok.Data;

import java.util.ArrayList;

@Data
public abstract class AnimalM {
    protected String sound = "Unknown animal sound";
    private Short numberOfLegs = 128;
    private boolean ableToSpeak;
    private ArrayList<String> parentNames;
    private AnimalM owner;

    public AnimalM getOwner() {
        return owner;
    }

    public void setOwner(AnimalM owner) {
        this.owner = owner;
    }

    public String speak() {
        return "Making sound...";
    }
}
