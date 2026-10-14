import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/** Holds the words, grouped by category. Can also load extra words from a text file. */
public final class WordBank {
    public static final String RANDOM = "Random";

    private final Map<String, List<String>> categories = new TreeMap<>();
    private final Random random = new Random();

    public WordBank() {
        add("Animals", "cat", "dog", "cow", "bat", "tiger", "zebra", "horse", "sheep", "panda",
                "monkey", "giraffe", "dolphin", "penguin", "leopard", "elephant", "kangaroo",
                "crocodile", "chimpanzee", "hippopotamus", "rhinoceros");
        add("Fruits", "fig", "plum", "pear", "apple", "grape", "mango", "lemon", "banana",
                "orange", "cherry", "papaya", "strawberry", "pineapple", "watermelon",
                "blueberry", "pomegranate");
        add("Countries", "india", "china", "japan", "nepal", "spain", "brazil", "canada",
                "germany", "sweden", "australia", "argentina", "indonesia", "switzerland",
                "bangladesh");
        add("Programming", "java", "code", "byte", "array", "loop", "class", "object", "string",
                "method", "thread", "compiler", "variable", "inheritance", "polymorphism",
                "encapsulation", "interface", "exception", "algorithm", "recursion");
    }

    private void add(String category, String... words) {
        for (String w : words) addWord(category, w);
    }

    /** Adds a word if it contains only letters and is not already present. */
    public void addWord(String category, String word) {
        String w = word.trim().toLowerCase();
        String c = category.trim();
        if (c.isEmpty() || !w.matches("[a-z]+")) return;
        c = c.substring(0, 1).toUpperCase() + c.substring(1).toLowerCase();
        List<String> list = categories.computeIfAbsent(c, k -> new ArrayList<>());
        if (!list.contains(w)) list.add(w);
    }

    /** Reads lines like "Space:galaxy". Blank lines and lines starting with # are ignored. */
    public int loadFromFile(Path file) throws IOException {
        int before = totalWords();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split(":", 2);
            if (parts.length == 2) addWord(parts[0], parts[1]);
        }
        return totalWords() - before;
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories.keySet());
    }

    private int totalWords() {
        return categories.values().stream().mapToInt(List::size).sum();
    }

    /** Picks a random word of suitable length; falls back to any word in the category. */
    public Optional<String> pickWord(String category, Difficulty difficulty) {
        List<String> pool;
        if (RANDOM.equalsIgnoreCase(category)) {
            pool = categories.values().stream().flatMap(List::stream).collect(Collectors.toList());
        } else {
            pool = categories.getOrDefault(category, Collections.<String>emptyList());
        }
        List<String> fitting = pool.stream().filter(difficulty::accepts).collect(Collectors.toList());
        if (fitting.isEmpty()) fitting = pool;
        if (fitting.isEmpty()) return Optional.empty();
        return Optional.of(fitting.get(random.nextInt(fitting.size())));
    }
}
