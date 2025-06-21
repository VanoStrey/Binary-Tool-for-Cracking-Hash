package threeByteChunkOffset;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.file.Path;

public class ThreeByteFileAccessor {
    private final String filePath;
    private final int elementSize = 3;
    private final int chunkIndex;

    public ThreeByteFileAccessor(String filePath) {
        this.filePath = filePath;
        this.chunkIndex = extractChunkIndex(filePath);
    }

    private int extractChunkIndex(String path) {
        // Ищем шаблон "chunk_N" в имени файла
        String fileName = Path.of(path).getFileName().toString();
        try {
            int start = fileName.indexOf("chunk_") + 6;
            int end = fileName.indexOf('.', start); // до ".bin"
            return Integer.parseInt(fileName.substring(start, end));
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось определить индекс чанка из имени файла: " + path);
        }
    }

    public BigInteger getElement(long index) {
        long byteOffset = index * elementSize;
        byte[] buffer = new byte[elementSize];

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            if (byteOffset >= raf.length()) return null;

            raf.seek(byteOffset);
            raf.readFully(buffer);

            BigInteger localValue = new BigInteger(1, buffer);
            BigInteger globalOffset = BigInteger.valueOf(chunkIndex).shiftLeft(24);
            return localValue.add(globalOffset);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getTotalElements() {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            return raf.length() / elementSize;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
