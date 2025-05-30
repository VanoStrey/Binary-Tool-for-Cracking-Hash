package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class UnicodeCombinationsGenerator {

    public void generateFile(String filePath, int lenthCombination) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_16BE)) {
            if (lenthCombination == 1){
                for (int i = 0; i < (int) Character.MAX_VALUE; i++) {
                    writer.write((char) i);
                }
            } else if (lenthCombination == 2){
                for (int i = 0; i < (int) Character.MAX_VALUE; i++) {
                    for (int j = 0; j < (int) Character.MAX_VALUE; j++) {
                        writer.write((char) i + "" + (char) j);
                    }
                    if (i % 100 == 0){
                        System.out.println(i);
                    }
                }
            } else{
                return;
            }

            System.out.println("Файл успешно создан: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

