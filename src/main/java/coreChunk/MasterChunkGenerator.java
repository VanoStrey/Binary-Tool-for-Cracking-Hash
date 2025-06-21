package coreChunk;

import java.io.FileOutputStream;
import java.io.IOException;

public class MasterChunkGenerator {
    private static final int TOTAL_COMBINATIONS = 1 << 24; // 2^24
    private static final String FILE_NAME = "master_chunk.bin";

    public static void main(String[] args) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(FILE_NAME)) {
            for (int i = 0; i < TOTAL_COMBINATIONS; i++) {
                fos.write((i >> 16) & 0xFF);
                fos.write((i >> 8) & 0xFF);
                fos.write(i & 0xFF);
            }
        }
        System.out.println("✅ Мастер чанк создан: " + FILE_NAME);
    }
}
