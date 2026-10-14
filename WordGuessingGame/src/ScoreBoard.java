import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Saves winning scores to a text file and reads back the best ones. */
public class ScoreBoard {

    public static class Entry {
        private final String name;
        private final int score;
        private final Difficulty difficulty;

        public Entry(String name, int score, Difficulty difficulty) {
            this.name = name.replace("|", "");
            this.score = score;
            this.difficulty = difficulty;
        }

        public String getName() { return name; }
        public int getScore() { return score; }
        public Difficulty getDifficulty() { return difficulty; }
    }

    private final Path file;

    public ScoreBoard(Path file) {
        this.file = file;
    }

    public void add(Entry e) throws IOException {
        String line = e.getName() + "|" + e.getScore() + "|" + e.getDifficulty() + System.lineSeparator();
        Files.write(file, line.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<Entry> top(int n) throws IOException {
        if (!Files.exists(file)) return Collections.emptyList();
        return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                .map(this::parse)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingInt((Entry e) -> e.getScore()).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    private Optional<Entry> parse(String line) {
        try {
            String[] p = line.split("\\|");
            return Optional.of(new Entry(p[0], Integer.parseInt(p[1]), Difficulty.valueOf(p[2])));
        } catch (RuntimeException ex) {   // skips corrupt lines
            return Optional.empty();
        }
    }
}
