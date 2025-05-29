package org.example;

public class Main {
    public static void main(String[] args) {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789qwerty");
        String chars = "";
        for (int i = 0; i < (int) Character.MAX_VALUE; i++) {
            for (int j = 0; j < (int) Character.MAX_VALUE; j++) {
                System.out.print(unicodeConverter.unicodeToRangeString(String.valueOf((char) i + "" +  (char) j)) + " ");

            }
            System.out.println();
        }
    }
}