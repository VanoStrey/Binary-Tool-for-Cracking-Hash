import java.io.IOException;
import java.nio.file.*;

public class DictionaryAddition {
    public void Add(String path1, String path2){
        Path file1 = Path.of(path1);
        Path file2 = Path.of(path2);

        try {
            byte[] data2 = Files.readAllBytes(file2); // Читаем второй файл
            Files.write(file1, data2, StandardOpenOption.APPEND); // Добавляем его в конец первого

            System.out.println("✅ Второй файл успешно добавлен в первый!");
        } catch (IOException e) {
            System.err.println("❌ Ошибка: " + e.getMessage());
        }
    }
}
