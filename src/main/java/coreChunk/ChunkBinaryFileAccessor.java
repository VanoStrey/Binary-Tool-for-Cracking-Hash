package coreChunk;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class ChunkBinaryFileAccessor implements AutoCloseable {
    private static final int ELEMENT_SIZE = 3;
    private static final int BLOCK_SIZE   = 4 * 1024 * 1024; // 4 MB window

    private final byte[]      globalOffset;
    private final int         globalOffsetLen;
    private final long        totalElements;
    private final FileChannel channel;

    private MappedByteBuffer  buffer;
    private long              currentBlockStart = -1;

    public ChunkBinaryFileAccessor(String filePath) throws IOException {
        int chunkIndex = extractChunkIndex(filePath);
        this.globalOffset = encodeChunkOffset(chunkIndex);
        this.globalOffsetLen = globalOffset.length;

        Path path = Path.of(filePath);
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
        long fileSize = channel.size();
        this.totalElements = fileSize / ELEMENT_SIZE;
    }

    private int extractChunkIndex(String path) {
        String fileName = Path.of(path).getFileName().toString();
        try {
            int start = fileName.indexOf("chunk_") + 6;
            int end   = fileName.indexOf('.', start);
            return Integer.parseInt(fileName.substring(start, end));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Не удалось определить индекс чанка из имени файла: " + path, e
            );
        }
    }

    public byte[] getElement(long index) throws IOException {
        if (index < 0 || index >= totalElements) return null;

        long byteOffset = index * ELEMENT_SIZE;
        long blockStart = (byteOffset / BLOCK_SIZE) * BLOCK_SIZE;

        // Пересоздать окно, если выходим за границу текущего
        if (blockStart != currentBlockStart) {
            long remaining = channel.size() - blockStart;
            long mapSize   = Math.min(BLOCK_SIZE, remaining);
            try {
                buffer = channel.map(FileChannel.MapMode.READ_ONLY, blockStart, mapSize);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при мэппинге файла", e);
            }
            currentBlockStart = blockStart;
        }

        int posInBlock = (int)(byteOffset - currentBlockStart);
        byte[] result = new byte[globalOffsetLen + ELEMENT_SIZE];

        // 1) Копируем глобальное смещение
        System.arraycopy(globalOffset, 0, result, 0, globalOffsetLen);

        // 2) Читаем 3 байта значения
        buffer.position(posInBlock);
        buffer.get(result, globalOffsetLen, ELEMENT_SIZE);

        return result;
    }

    private byte[] encodeChunkOffset(int chunkIndex) {
        int shiftBits = 24;
        int totalBytes = (shiftBits + 7) / 8; // минимум 3 байта
        byte[] result = new byte[totalBytes];
        int local = chunkIndex;
        for (int i = totalBytes - 1; i >= 0; i--) {
            result[i] = (byte)(local & 0xFF);
            local >>>= 8;
        }
        return result;
    }

    public long getTotalElements() {
        return totalElements;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
