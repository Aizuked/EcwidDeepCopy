import org.Aizuked.CopyUtils.CopyUtils;
import org.Aizuked.CopyUtils.DeepCopyUtil;
import org.Aizuked.TestObjPackage.Cat;
import org.Aizuked.TestObjPackage.Dog;
import org.Aizuked.TestObjPackage.Man;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class CopyUtilsTests {
    @Test
    public void shallowCopyTest() {
        Man man1 = new Man("Robert", 18, List.of("Book1", "Book2", "Book3"));
        man1.setNeighboringRoomNumbers(new int[]{10, 20});
        man1.setNumberOfLegs((short) 2);
        man1.setParentNames(List.of("John", "Diane"));
        man1.setAbleToSpeak(false);
        man1.setFavoriteIceCreamToppings(new String[]{"None", "Chocolate"});
        man1.setSelf(man1);
        man1.setAnimals(new ArrayList<>(List.of(new Dog(man1))));

        Man man2 = CopyUtils.deepCopy(man1);

 /*       man2.setName("Rufus");
        man2.setParentNames(List.of("Rudolf", "Julie"));
        man2.setNumberOfLegs((short)4);
        man2.setAbleToSpeak(true);
        man2.setNeighboringRoomNumbers(new int[]{40, 50});
        man2.setAge(20);
        man2.setFavoriteBooks(List.of("One"));
        man2.setFavoriteIceCreamToppings(new String[] {"Vanilla", "Banana"});
        man2.setSelf(man2);
        man2.setAnimals(new ArrayList<>(List.of(new Cat(man2))));*/


        Assert.assertNotSame(man1.getName(), man2.getName());
        Assert.assertNotSame(man1.getAge(), man2.getAge());
        Assert.assertNotSame(man1.getFavoriteBooks(), man2.getFavoriteBooks());
        Assert.assertFalse(Arrays.equals(man1.getNeighboringRoomNumbers(), man2.getNeighboringRoomNumbers()));
        Assert.assertFalse(Arrays.equals(man1.getFavoriteIceCreamToppings(), man2.getFavoriteIceCreamToppings()));
        Assert.assertNotSame(man1.getAnimals(), man2.getAnimals());
        Assert.assertNotSame(man1.getSelf(), man2.getSelf());
        Assert.assertNotSame(man1.getNumberOfLegs(), man2.getNumberOfLegs());
        Assert.assertNotSame(man1.isAbleToSpeak(), man2.isAbleToSpeak());
        Assert.assertNotSame(man1.getParentNames(), man2.getParentNames());
        Assert.assertNotSame(man1.getAnimals(), man2.getAnimals());
    }

    @Test
    public void copySuccessTest() {
        Man man1 = new Man("Robert", 18, List.of("Book1", "Book2", "Book3"));
        man1.setNeighboringRoomNumbers(new int[]{10, 20});
        man1.setNumberOfLegs((short) 2);
        man1.setParentNames(List.of("John", "Diane"));
        man1.setAbleToSpeak(true);

        Man man2 = CopyUtils.deepCopy(man1);

        Assert.assertEquals(man1.getName(), man2.getName());
        Assert.assertEquals(man1.getAge(), man2.getAge());
        Assert.assertSame(man1.getFavoriteBooks(), man2.getFavoriteBooks());
        Assert.assertArrayEquals(man1.getNeighboringRoomNumbers(), man2.getNeighboringRoomNumbers());
        Assert.assertArrayEquals(man1.getFavoriteIceCreamToppings(), man2.getFavoriteIceCreamToppings());
        Assert.assertSame(man1.getAnimals(), man2.getAnimals());
        Assert.assertSame(man1.getSelf(), man2.getSelf());
        Assert.assertEquals(man1.getNumberOfLegs(), man2.getNumberOfLegs());
        Assert.assertEquals(man1.isAbleToSpeak(), man2.isAbleToSpeak());
        Assert.assertSame(man1.getParentNames(), man2.getParentNames());

    }

}
