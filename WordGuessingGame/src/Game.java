import java.util.*;
import java.util.stream.Collectors;

/** The state and rules of a single round. No console code here. */
public class Game {

    public enum GuessResult { CORRECT, WRONG, ALREADY_GUESSED, INVALID, WON, LOST }

    private final String secret;
    private final String category;
    private final Difficulty difficulty;
    private final Set<Character> guessed = new LinkedHashSet<>();
    private final Set<Character> wrongLetters = new LinkedHashSet<>();
    private final Random random = new Random();

    private int attemptsLeft;
    private int hintsUsed;
    private boolean won;

    public Game(String secret, String category, Difficulty difficulty) {
        this.secret = secret.toLowerCase();
        this.category = category;
        this.difficulty = difficulty;
        this.attemptsLeft = difficulty.getMaxAttempts();
    }

    public GuessResult guessLetter(char c) {
        c = Character.toLowerCase(c);
        if (c < 'a' || c > 'z') return GuessResult.INVALID;
        if (guessed.contains(c)) return GuessResult.ALREADY_GUESSED;
        guessed.add(c);
        if (secret.indexOf(c) >= 0) {
            if (allRevealed()) {
                won = true;
                return GuessResult.WON;
            }
            return GuessResult.CORRECT;
        }
        wrongLetters.add(c);
        return loseAttempt();
    }

    public GuessResult guessWord(String word) {
        word = word.trim().toLowerCase();
        if (!word.matches("[a-z]+")) return GuessResult.INVALID;
        if (word.equals(secret)) {
            for (char c : secret.toCharArray()) guessed.add(c);
            won = true;
            return GuessResult.WON;
        }
        return loseAttempt();
    }

    /** Reveals one hidden letter at the cost of one life. Empty if a hint isn't allowed. */
    public Optional<Character> useHint() {
        List<Character> hidden = secret.chars()
                .mapToObj(ch -> (char) ch)
                .filter(ch -> !guessed.contains(ch))
                .distinct()
                .collect(Collectors.toList());
        if (hidden.size() <= 1 || attemptsLeft <= 1) return Optional.empty();
        char letter = hidden.get(random.nextInt(hidden.size()));
        guessed.add(letter);
        hintsUsed++;
        attemptsLeft--;
        return Optional.of(letter);
    }

    private GuessResult loseAttempt() {
        attemptsLeft--;
        return attemptsLeft <= 0 ? GuessResult.LOST : GuessResult.WRONG;
    }

    private boolean allRevealed() {
        for (char c : secret.toCharArray()) {
            if (!guessed.contains(c)) return false;
        }
        return true;
    }

    /** e.g. "J _ V A" */
    public String getMaskedWord() {
        StringBuilder sb = new StringBuilder();
        for (char c : secret.toCharArray()) {
            sb.append(guessed.contains(c) ? Character.toUpperCase(c) : '_').append(' ');
        }
        return sb.toString().trim();
    }

    /** Score only for a win: more lives left and longer words score more, hints cost points. */
    public int calculateScore() {
        if (!won) return 0;
        int base = attemptsLeft * 10 + secret.length() * 5;
        return Math.max(0, base * difficulty.getMultiplier() - hintsUsed * 5);
    }

    public boolean isWon() { return won; }
    public boolean isLost() { return attemptsLeft <= 0; }
    public boolean isOver() { return won || isLost(); }

    public String getSecret() { return secret; }
    public String getCategory() { return category; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getAttemptsLeft() { return attemptsLeft; }
    public int getHintsUsed() { return hintsUsed; }
    public int getWrongCount() { return difficulty.getMaxAttempts() - attemptsLeft; }
    public Set<Character> getWrongLetters() { return Collections.unmodifiableSet(wrongLetters); }
}
