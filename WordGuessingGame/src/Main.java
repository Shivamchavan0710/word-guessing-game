import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Console interface for the Word Guessing Game (Hangman style). */
public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static int roundsPlayed = 0;
    private static int roundsWon = 0;
    private static int totalPoints = 0;

    private static final String[] STAGES = {
        "  +---+\n  |   |\n      |\n      |\n      |\n      |\n=========",
        "  +---+\n  |   |\n  O   |\n      |\n      |\n      |\n=========",
        "  +---+\n  |   |\n  O   |\n  |   |\n      |\n      |\n=========",
        "  +---+\n  |   |\n  O   |\n /|   |\n      |\n      |\n=========",
        "  +---+\n  |   |\n  O   |\n /|\\  |\n      |\n      |\n=========",
        "  +---+\n  |   |\n  O   |\n /|\\  |\n /    |\n      |\n=========",
        "  +---+\n  |   |\n  O   |\n /|\\  |\n / \\  |\n      |\n========="
    };

    public static void main(String[] args) {
        WordBank bank = new WordBank();
        loadCustomWords(bank);
        ScoreBoard board = new ScoreBoard(Paths.get("scores.txt"));

        System.out.println("=====================================");
        System.out.println("        WORD GUESSING GAME");
        System.out.println("=====================================");
        String player = read("Enter your name: ");
        if (player.isEmpty()) player = "Player";
        System.out.println("Welcome, " + player + "!");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("  1) Play");
            System.out.println("  2) High scores");
            System.out.println("  3) How to play");
            System.out.println("  4) Exit");
            switch (readChoice("> ", 1, 4)) {
                case 1: playRound(bank, board, player); break;
                case 2: showScores(board); break;
                case 3: showHelp(); break;
                default: running = false;
            }
        }
        System.out.printf("%nSession: %d round(s) played, %d won, %d points. Goodbye, %s!%n",
                roundsPlayed, roundsWon, totalPoints, player);
    }

    // ------------------------------------------------------------- a round

    private static void playRound(WordBank bank, ScoreBoard board, String player) {
        Difficulty[] levels = Difficulty.values();
        System.out.println("\nChoose difficulty:");
        for (int i = 0; i < levels.length; i++) {
            System.out.printf("  %d) %s%n", i + 1, levels[i].describe());
        }
        Difficulty difficulty = levels[readChoice("> ", 1, levels.length) - 1];

        List<String> categories = new ArrayList<>(bank.getCategories());
        categories.add(WordBank.RANDOM);
        System.out.println("\nChoose category:");
        for (int i = 0; i < categories.size(); i++) {
            System.out.printf("  %d) %s%n", i + 1, categories.get(i));
        }
        String category = categories.get(readChoice("> ", 1, categories.size()) - 1);

        Optional<String> word = bank.pickWord(category, difficulty);
        if (!word.isPresent()) {
            System.out.println("No words available for that category.");
            return;
        }

        Game game = new Game(word.get(), category, difficulty);
        while (!game.isOver()) {
            render(game);
            String input = read("Guess a letter or the whole word (/hint, /quit): ").toLowerCase();
            if (input.isEmpty()) continue;

            if (input.equals("/quit")) {
                System.out.println("You gave up. The word was: " + game.getSecret().toUpperCase());
                roundsPlayed++;
                return;
            }
            if (input.equals("/hint")) {
                Optional<Character> hint = game.useHint();
                if (hint.isPresent()) {
                    System.out.println("Hint: the word contains '" + Character.toUpperCase(hint.get())
                            + "'. (-1 life)");
                } else {
                    System.out.println("No hint available right now.");
                }
                continue;
            }

            Game.GuessResult result = input.length() == 1
                    ? game.guessLetter(input.charAt(0))
                    : game.guessWord(input);
            printResult(result);
        }
        finishRound(game, board, player);
    }

    private static void printResult(Game.GuessResult result) {
        switch (result) {
            case CORRECT:         System.out.println("Nice! That letter is in the word."); break;
            case WRONG:           System.out.println("Nope, wrong guess."); break;
            case ALREADY_GUESSED: System.out.println("You already tried that letter."); break;
            case INVALID:         System.out.println("Please use letters a-z only."); break;
            default:              break;   // WON / LOST are handled after the loop
        }
    }

    private static void finishRound(Game game, ScoreBoard board, String player) {
        render(game);
        roundsPlayed++;
        if (game.isWon()) {
            int score = game.calculateScore();
            roundsWon++;
            totalPoints += score;
            System.out.println("*** YOU WON! The word was " + game.getSecret().toUpperCase() + " ***");
            System.out.println("Score: " + score);
            try {
                board.add(new ScoreBoard.Entry(player, score, game.getDifficulty()));
            } catch (IOException e) {
                System.out.println("(Could not save score: " + e.getMessage() + ")");
            }
        } else {
            System.out.println("*** GAME OVER! The word was " + game.getSecret().toUpperCase() + " ***");
        }
    }

    private static void render(Game game) {
        int max = game.getDifficulty().getMaxAttempts();
        int stage = (int) Math.ceil(game.getWrongCount() * (STAGES.length - 1.0) / max);
        System.out.println();
        System.out.println(STAGES[stage]);
        System.out.println("Category: " + game.getCategory() + "   Difficulty: " + game.getDifficulty());
        System.out.println("Word: " + game.getMaskedWord() + "   (" + game.getSecret().length() + " letters)");
        StringBuilder wrong = new StringBuilder();
        for (char c : game.getWrongLetters()) {
            if (wrong.length() > 0) wrong.append(", ");
            wrong.append(Character.toUpperCase(c));
        }
        System.out.println("Wrong letters: " + (wrong.length() == 0 ? "-" : wrong));
        System.out.println("Lives left: " + game.getAttemptsLeft() + "   Hints used: " + game.getHintsUsed());
    }

    // ------------------------------------------------------------ other screens

    private static void showScores(ScoreBoard board) {
        try {
            List<ScoreBoard.Entry> top = board.top(5);
            if (top.isEmpty()) {
                System.out.println("\nNo scores yet. Win a round to get on the board!");
                return;
            }
            System.out.println("\n--- TOP 5 ---");
            int rank = 1;
            for (ScoreBoard.Entry e : top) {
                System.out.printf("%d. %-12s %5d  (%s)%n", rank++, e.getName(), e.getScore(), e.getDifficulty());
            }
        } catch (IOException e) {
            System.out.println("Could not read scores: " + e.getMessage());
        }
    }

    private static void showHelp() {
        System.out.println("\nHOW TO PLAY");
        System.out.println(" - A secret word is hidden as underscores.");
        System.out.println(" - Type one letter to guess it, or type the whole word to guess it all at once.");
        System.out.println(" - Every wrong guess costs a life and adds a piece to the hangman.");
        System.out.println(" - /hint reveals one letter but costs a life and 5 points.");
        System.out.println(" - /quit gives up the current round.");
        System.out.println(" - Win before you run out of lives. Harder levels give higher scores.");
    }

    private static void loadCustomWords(WordBank bank) {
        Path file = Paths.get("words.txt");
        if (!Files.exists(file)) return;
        try {
            int added = bank.loadFromFile(file);
            System.out.println("(Loaded " + added + " extra word(s) from words.txt)");
        } catch (IOException e) {
            System.out.println("(Could not read words.txt: " + e.getMessage() + ")");
        }
    }

    // ---------------------------------------------------------------- input

    private static String read(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {          // input closed (Ctrl+D / Ctrl+Z)
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
        return in.nextLine().trim();
    }

    private static int readChoice(String prompt, int min, int max) {
        while (true) {
            String s = read(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }
}
