import java.nio.charset.StandardCharsets;
import java.io.*;

public class UnicodeCombinationsGenerator {

    public void generateFile(String filePath, int lengthCombination) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_16BE)) {
            if (lengthCombination == 1) {
                for (int i = 0; i <= (int) Character.MAX_VALUE; i++) {
                    writer.write((char) i);
                }
            } else if (lengthCombination == 2) {
                for (int i = 0; i <= (int) Character.MAX_VALUE; i++) {
                    for (int j = 0; j <= (int) Character.MAX_VALUE; j++) {
                        writer.write((char) i + "" + (char) j);
                    }
                    if (i % 100 == 0) {
                        System.out.println(i);
                    }
                }
            } else {
                return;
            }

            System.out.println("Файл успешно создан: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

