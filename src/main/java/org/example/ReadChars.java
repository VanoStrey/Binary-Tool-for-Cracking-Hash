package org.example;

import java.io.FileReader;
import java.io.IOException;

public class ReadChars {
    public static void main(String[] args) {
        try (FileReader reader = new FileReader("chars1.txt")) {
            int character;
            while ((character = reader.read()) != -1) {
                System.out.println("Символ: " + (char) character + " -> Код: " + character);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

