package coreChunk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class ChunkBinaryFileAccessor implements AutoCloseable {
    private final String filePath;
    private final int elementSize = 3;
    private final int chunkIndex;
    private final RandomAccessFile raf;
    private final long totalElements;

    public ChunkBinaryFileAccessor(String filePath) throws IOException {
        this.filePath = filePath;
        this.chunkIndex = extractChunkIndex(filePath);
        this.raf = new RandomAccessFile(filePath, "r");
        this.totalElements = raf.length() / elementSize;
    }

    private int extractChunkIndex(String path) {
        String fileName = Path.of(path).getFileName().toString();
        try {
            int start = fileName.indexOf("chunk_") + 6;
            int end = fileName.indexOf('.', start);
            return Integer.parseInt(fileName.substring(start, end));
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось определить индекс чанка из имени файла: " + path);
        }
    }

    public byte[] getElement(long index) {
        if (index < 0 || index >= totalElements) return null;

        long byteOffset = index * elementSize;
        byte[] local = new byte[elementSize];

        try {
            raf.seek(byteOffset);
            raf.readFully(local);

            // Преобразуем chunkIndex в байтовый массив (только нужное число байт)
            byte[] globalOffset = encodeChunkOffset(chunkIndex);

            // Склеиваем смещение + локальное значение → результат
            byte[] result = new byte[globalOffset.length + local.length];
            System.arraycopy(globalOffset, 0, result, 0, globalOffset.length);
            System.arraycopy(local, 0, result, globalOffset.length, local.length);

            return result;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] encodeChunkOffset(int chunkIndex) {
        int shiftBits = 24;
        int totalBytes = (shiftBits + 7) / 8; // минимум 3 байта
        byte[] result = new byte[totalBytes];

        for (int i = totalBytes - 1; i >= 0; i--) {
            result[i] = (byte) (chunkIndex & 0xFF);
            chunkIndex >>>= 8;
        }
        return result;
    }


    public long getTotalElements() {
        return totalElements;
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }
}
