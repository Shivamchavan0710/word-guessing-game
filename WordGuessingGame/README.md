# Word Guessing Game (Java, console)

A Hangman-style game. Guess the hidden word before you run out of lives.

## How to run

1. Install a JDK (version 8 or newer) and check it: `java -version` and `javac -version`
2. Open a terminal **inside this folder** (the one containing `src` and `words.txt`)
3. Compile and run:

```bash
javac -d out src/*.java
java -cp out Main
```

(Run from this folder so the game can find `words.txt` and save `scores.txt` here.)

## How to play

- Pick a difficulty and a category.
- Type a letter to guess it, or type the whole word to guess it at once (a wrong word costs a life).
- `/hint` reveals a letter (costs 1 life and 5 points). `/quit` gives up the round.
- Winning scores are saved to `scores.txt`. See them under "High scores" in the menu.

## Adding your own words

Edit `words.txt`, one word per line, in the format `Category:word`. New categories appear automatically.

## Files

| File | Purpose |
|---|---|
| `Main.java` | Menu, input and display |
| `Game.java` | Rules and state of one round |
| `WordBank.java` | Words by category, loads `words.txt` |
| `ScoreBoard.java` | Saves and reads top scores (file I/O, streams) |
| `Difficulty.java` | Enum: lives, word length, score multiplier |

## Java concepts used

Classes and OOP, encapsulation, enums with fields, nested classes, collections (`Map`, `List`, `Set`), `Optional`, lambdas and streams, `Comparator`, file I/O with `java.nio.file`, exception handling, and `Scanner` input.
