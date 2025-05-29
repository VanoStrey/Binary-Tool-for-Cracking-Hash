package org.example;
import java.io.FileWriter;
import java.io.IOException;

public class CharToFile {
    public static void main(String[] args) {
        try (FileWriter writer = new FileWriter("chars.txt")) {
            for (int i = Character.MIN_VALUE; i <= Character.MAX_VALUE; i++) {
                writer.write((char) i);
            }
            System.out.println("Запись завершена!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
