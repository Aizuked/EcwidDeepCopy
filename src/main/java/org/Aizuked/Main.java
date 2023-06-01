package org.Aizuked;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.cloning.Cloner;
import lombok.SneakyThrows;
import org.Aizuked.CopyUtils.DeepCopyUtil;
import org.Aizuked.TestObjPackage.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;


public class Main {
    static Man toCopy = Man.builder().name("Robert").age(18).favoriteBooks(new ArrayList<>(List.of("Book1", "Book2", "Book3"))).build();

    static {
        toCopy.setNeighboringRoomNumbers(new int[]{10, 20});
        toCopy.setFavoriteIceCreamToppings(new String[]{"Chocolate", "Vanilla"});

        ArrayList<String> testArrayList = new ArrayList<>();
        testArrayList.add("first");
        testArrayList.add("second");
        toCopy.setTestArrayList(testArrayList);

        HashMap<Integer, Integer> testHashMap = new HashMap<>();
        testHashMap.put(128, 129);
        testHashMap.put(130, 131);
        toCopy.setTestHashMap(testHashMap);

        HashSet<String> testHashSet = new HashSet<>();
        testHashSet.add("true");
        testHashSet.add("false");
        toCopy.setTestHashSet(testHashSet);

        ArrayList<Animal> animals = new ArrayList<>();
        Cat cat = new Cat();
        cat.setAbleToSpeak(false);
        cat.setNumberOfLegs((short) 4);
        cat.setParentNames(new ArrayList<>(List.of("Puss in boots 1", "Puss in boots 2")));
        animals.add(cat);

        Dog brian = new Dog();
        brian.setAbleToSpeak(true);
        brian.setNumberOfLegs((short) 3);
        brian.setParentNames(new ArrayList<>(Arrays.asList("Isabella", "Quqly")));
        animals.add(brian);

        toCopy.setAnimals(animals);
    }

    @SneakyThrows
    public static void main(String[] args) {
        Options opt = new OptionsBuilder().include(Main.class.getSimpleName()).forks(5).measurementIterations(100).warmupIterations(1).mode(Mode.Throughput).timeUnit(TimeUnit.SECONDS).resultFormat(ResultFormatType.JSON).result("C:\\Users\\Aizuked\\Documents\\result.json").build();
        new Runner(opt).run();
    }

    @SneakyThrows
    @Benchmark
    public void deepCopyMy() {
        Man deepCopyMy = null;
        deepCopyMy = DeepCopyUtil.deepCopy(toCopy);
    }

    @SneakyThrows
    @Benchmark
    public void deepCopyJackson() {
        Man deepCopyJackson = null;
        ObjectMapper objectMapper = new ObjectMapper();
        deepCopyJackson = objectMapper.readValue(objectMapper.writeValueAsString(toCopy), Man.class);
    }

    @Benchmark
    public void deepCopyKostaskougios() {
        Man deepCopyKostaskougios = null;
        Cloner cloner = new Cloner();
        deepCopyKostaskougios = cloner.deepClone(toCopy);
    }
}