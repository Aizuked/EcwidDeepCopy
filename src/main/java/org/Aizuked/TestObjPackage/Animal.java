package org.Aizuked.TestObjPackage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Animal {
    protected String sound = "Unknown animal sound";
    private Short numberOfLegs = 128;
    private boolean ableToSpeak;
    private ArrayList<String> parentNames;
    public String speak() {
        return sound;
    }
}
