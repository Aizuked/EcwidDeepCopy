package org.Aizuked;

import org.Aizuked.CopyUtils.CopyUtils;
import org.Aizuked.CopyUtils.DeepCopyUtil;
import org.Aizuked.TestObjPackage.Man;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Man man1 = new Man("Robert", 18, List.of("Book1", "Book2", "Book3"));

        man1.setNeighboringRoomNumbers(new int[]{10, 20});
        man1.setFavoriteIceCreamToppings(new String[]{"Chocolate", "Vanilla"});

        man1.setParentNames(List.of("Emma", "John"));
       /* Man man2 = CopyUtils.deepCopy(man1);

        System.out.println(man1 == man2);*/

        Man man3 = DeepCopyUtil.deepCopy(man1);
/*
        int i = 1;
        int b = CopyUtils.deepCopy(i);
        System.out.println(i == b);*/

/*        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(man1);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        Man copied = (Man) in.readObject();

        System.out.println(man1 == copied);*/
    }
}