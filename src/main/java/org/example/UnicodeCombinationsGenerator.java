package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class UnicodeCombinationsGenerator {
    private String filePath;
    private int combinationLength;

    public UnicodeCombinationsGenerator(String filePath, int combinationLength) {
        this.filePath = filePath;
        this.combinationLength = combinationLength;
    }

    public void generateFile() {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_16BE)) {

            generateCombinations("", 0, writer);

            System.out.println("Файл успешно создан: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateCombinations(String prefix, int depth, OutputStreamWriter writer) throws IOException {
        if (depth == combinationLength) {
            writer.write(prefix);
            return;
        }

        for (char ch = 0; ch < Character.MAX_VALUE; ch++) {
            generateCombinations(prefix + ch, depth + 1, writer);
        }
    }
}

