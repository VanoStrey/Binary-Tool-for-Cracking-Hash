package Bytes;

import java.io.FileOutputStream;
import java.io.IOException;

public class BinFileWriter {
    private static final int BLOCK_COUNT = 1_000_000; // 🔹 Количество блоков (пример)
    private static final String FILE_PATH = "output.bin";

    public static void writeBinaryFile() {
        try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
            BinCombinationGenerate generator = new BinCombinationGenerate(-1L); // 🔹 Начинаем с 0

            for (int i = 0; i < BLOCK_COUNT; i++) {
                fos.write(generator.nextCombination()); // 🔥 Записываем 5-байтовую комбинацию
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Файл `" + FILE_PATH + "` успешно записан!");
    }

    public static void main(String[] args) {
        writeBinaryFile(); // 🚀 Запуск генерации файла
    }
}
