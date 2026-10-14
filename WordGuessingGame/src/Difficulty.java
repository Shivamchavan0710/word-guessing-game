/** Difficulty levels: number of lives, allowed word length and score multiplier. */
public enum Difficulty {
    EASY(8, 3, 5, 1),
    MEDIUM(6, 5, 7, 2),
    HARD(5, 7, 20, 3);

    private final int maxAttempts;
    private final int minLength;
    private final int maxLength;
    private final int multiplier;

    Difficulty(int maxAttempts, int minLength, int maxLength, int multiplier) {
        this.maxAttempts = maxAttempts;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.multiplier = multiplier;
    }

    public int getMaxAttempts() { return maxAttempts; }
    public int getMultiplier() { return multiplier; }

    /** True if the word length suits this difficulty. */
    public boolean accepts(String word) {
        return word.length() >= minLength && word.length() <= maxLength;
    }

    public String describe() {
        return String.format("%-6s - %d lives, score x%d", name(), maxAttempts, multiplier);
    }
}
