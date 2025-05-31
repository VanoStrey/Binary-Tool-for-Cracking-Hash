import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class DictionarySplitter {

    private SymbolFileAccessor accessor;

    public DictionarySplitter(SymbolFileAccessor accessor) {
        this.accessor = accessor;
    }

    public void split(String outPath, long minValuet, long maxValuet) {
        try (FileOutputStream fos = new FileOutputStream(outPath);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_16BE)) {
            for (long i = minValuet; i < maxValuet; i++) {
                writer.write(accessor.getElement(i));
            }
            System.out.println("Файл успешно создан: " + outPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
