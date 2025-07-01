package coreChunk;

import hashFunc.Hasher;
import hashFunc.HasherFactory;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class DictionarySearch {
    private ArrayList<HashBinarySearch> hashBinarySearch = new ArrayList<>();
    private String dictionaryDir;
    private ChunkValueEncoding converter;
    private Hasher hasher;


    public DictionarySearch(String dictionaryDir) throws IOException {
        Path metadataPath = Paths.get(dictionaryDir, "metadata.json");
        String metadataContent = Files.readString(metadataPath);
        JSONObject meta = new JSONObject(metadataContent);

        String symbols = meta.getString("dictionary_symbols");
        String hashAlg = meta.getString("hash_algorithm");

        this.converter = new ChunkValueEncoding(symbols);
        this.hasher = HasherFactory.getHasher(hashAlg);
        this.dictionaryDir = dictionaryDir;

        initChunkSearch();
    }



    public String search(String hash) throws InterruptedException {
        AtomicReference<String> foundResult = new AtomicReference<>();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        for (HashBinarySearch searcher : hashBinarySearch) {
            futures.add(executor.submit(() -> {
                if (foundResult.get() != null) return;
                String result = null;
                try {
                    result = searcher.search(hash);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (!result.isEmpty()) foundResult.compareAndSet(null, result);
            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get(); // 🔥 Именно тут может быть ExecutionException
            } catch (ExecutionException e) {
                e.printStackTrace(); // или залогируй по-своему
            }
        }

        executor.shutdown();

        return foundResult.get() != null ? foundResult.get() : "hash not found";
    }

    private void initChunkSearch() throws IOException {
        System.out.println("🧠 Инициализация и прогрев чанков...");

        for (Path chunkPath : findAllChunkPaths()) {
            String path = chunkPath.toString();

            ChunkBinaryFileAccessor accessor = new ChunkBinaryFileAccessor(path);
            HashBinarySearch search = new HashBinarySearch(accessor, converter, hasher);
            hashBinarySearch.add(search);

            long total = accessor.getTotalElements();
            long[] offsets = {
                    0,
                    total / 4,
                    total / 2,
                    (3 * total) / 4,
                    Math.max(0, total - 1)
            };

            for (long offset : offsets) {
                byte[] el = accessor.getElement(offset);
                if (el == null) continue;
                String encoded = converter.convertToBaseString(el);
                byte[] hash = hasher.getBinHash(encoded);
                hash[0] ^= el[0];
            }
        }

        System.gc();
        System.out.println("✅ Система прогрета и готова к работе\n");
    }


    private List<Path> findAllChunkPaths() throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(dictionaryDir))) {
            return stream
                    .filter(path -> path.getFileName().toString().matches("chunk_\\d+\\.bin"))
                    .sorted(Comparator.comparingInt(p ->
                            Integer.parseInt(p.getFileName().toString()
                                    .replace("chunk_", "")
                                    .replace(".bin", "")))
                    )
                    .toList();
        }
    }

}
