package ru.test.keyGeneration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class KeyCreater {


    private ArrayList<String> arrayList = new ArrayList<>((Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i",
            "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3",
            "4", "5", "6", "7", "8", "9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S",
            "T","U","V","W","X","Y","Z")));


    public String generateKey() {
        Random random = new Random();
        int size = 7;
        StringBuilder builder = new StringBuilder();
        String result=null;
        for (int i = 0; i < size; i++) {
            builder.append(arrayList.get(random.nextInt(62)));
            result = builder.toString();
        }
        return result;
    }
}
