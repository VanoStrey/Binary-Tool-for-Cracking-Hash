package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class MappedFileSorter {
    // Каждая запись – 4 байта (2 символа в UTF-16BE)
    private static final int RECORD_SIZE = 4;
    // Размер чанка, гарантированно кратный RECORD_SIZE (например, 100 МБ)
    private static final long CHUNK_SIZE = 100L * 1024 * 1024;

    public void sortFile(String inputFile, String outputFile) throws IOException {
        List<Path> tempChunkFiles = new ArrayList<>();

        try (FileChannel inputChannel = FileChannel.open(Paths.get(inputFile), StandardOpenOption.READ)) {
            long fileSize = inputChannel.size();
            // Общее число чанков
            long numChunks = (fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE;

            for (long chunkIndex = 0; chunkIndex < numChunks; chunkIndex++) {
                long position = chunkIndex * CHUNK_SIZE;
                long size = Math.min(CHUNK_SIZE, fileSize - position);
                // Маппим чанк
                MappedByteBuffer buffer = inputChannel.map(FileChannel.MapMode.READ_ONLY, position, size);

                // Количество записей в чанке
                int numRecords = (int) (size / RECORD_SIZE);
                byte[][] records = new byte[numRecords][RECORD_SIZE];

                for (int i = 0; i < numRecords; i++) {
                    buffer.position(i * RECORD_SIZE);
                    buffer.get(records[i]);
                }

                // Сортировка записей, сравнение по байтам (лексикографически)
                Arrays.sort(records, new Comparator<byte[]>() {
                    @Override
                    public int compare(byte[] a, byte[] b) {
                        for (int i = 0; i < RECORD_SIZE; i++) {
                            int diff = Byte.compare(a[i], b[i]);
                            if (diff != 0) return diff;
                        }
                        return 0;
                    }
                });

                // Запись отсортированного чанка во временный файл
                Path tempFile = Paths.get("chunk_" + chunkIndex + ".tmp");
                try (FileOutputStream fos = new FileOutputStream(tempFile.toFile());
                     FileChannel outChannel = fos.getChannel()) {
                    ByteBuffer outBuffer = ByteBuffer.allocate(numRecords * RECORD_SIZE);
                    for (byte[] record : records) {
                        outBuffer.put(record);
                    }
                    outBuffer.flip();
                    outChannel.write(outBuffer);
                }
                tempChunkFiles.add(tempFile);
            }
        }

        // k-way merge отсортированных чанков
        kWayMerge(tempChunkFiles, Paths.get(outputFile));

        // Удаляем временные файлы
        for (Path tempFile : tempChunkFiles) {
            Files.deleteIfExists(tempFile);
        }
    }

    // Элемент для очереди при слиянии чанков
    private static class QueueEntry {
        byte[] record;
        int chunkIndex;
        FileChannel channel;
        ByteBuffer buffer;

        public QueueEntry(byte[] record, int chunkIndex, FileChannel channel, ByteBuffer buffer) {
            this.record = record;
            this.chunkIndex = chunkIndex;
            this.channel = channel;
            this.buffer = buffer;
        }
    }

    // k-way merge с использованием PriorityQueue
    private void kWayMerge(List<Path> sortedChunkFiles, Path outputFile) throws IOException {
        List<FileChannel> chunkChannels = new ArrayList<>();
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>(new Comparator<QueueEntry>() {
            @Override
            public int compare(QueueEntry e1, QueueEntry e2) {
                for (int i = 0; i < RECORD_SIZE; i++) {
                    int diff = Byte.compare(e1.record[i], e2.record[i]);
                    if (diff != 0) return diff;
                }
                return 0;
            }
        });

        // Инициализация: читаем первую запись из каждого чанка
        for (int i = 0; i < sortedChunkFiles.size(); i++) {
            FileChannel ch = FileChannel.open(sortedChunkFiles.get(i), StandardOpenOption.READ);
            chunkChannels.add(ch);
            ByteBuffer buf = ByteBuffer.allocate(RECORD_SIZE);
            if (ch.read(buf) == RECORD_SIZE) {
                buf.flip();
                byte[] record = new byte[RECORD_SIZE];
                buf.get(record);
                queue.add(new QueueEntry(record, i, ch, buf));
            }
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile.toFile());
             FileChannel outChannel = fos.getChannel()) {
            ByteBuffer outBuffer = ByteBuffer.allocate(4 * 1024 * 1024); // например, 4 МБ
            while (!queue.isEmpty()) {
                QueueEntry entry = queue.poll();
                outBuffer.put(entry.record);
                if (outBuffer.remaining() < RECORD_SIZE) { // если буфер заполнен, записываем на диск
                    outBuffer.flip();
                    outChannel.write(outBuffer);
                    outBuffer.clear();
                }
                // Читаем следующую запись из того же чанка
                entry.buffer.clear();
                if (entry.channel.read(entry.buffer) == RECORD_SIZE) {
                    entry.buffer.flip();
                    byte[] newRecord = new byte[RECORD_SIZE];
                    entry.buffer.get(newRecord);
                    queue.add(new QueueEntry(newRecord, entry.chunkIndex, entry.channel, entry.buffer));
                }
            }
            outBuffer.flip();
            while (outBuffer.hasRemaining()) {
                outChannel.write(outBuffer);
            }
        }

        // Закрываем все каналы
        for (FileChannel ch : chunkChannels) {
            ch.close();
        }
    }

    // Точка входа для тестирования MappedFileSorter
    public static void main(String[] args) {
        try {
            MappedFileSorter sorter = new MappedFileSorter();
            sorter.sortFile("unicode_combinations2.txt", "sorted_unicode_combinations2.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
