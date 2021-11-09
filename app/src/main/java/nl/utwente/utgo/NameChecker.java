package nl.utwente.utgo;

/**
 * This final class checks whether a given username or group name is correct.
 */
public final class NameChecker {

    protected final static int MIN_CHARS_NAME = 3;
    protected final static int MAX_CHARS_NAME = 15;

    public static boolean isNameCorrect(String newName) {
        return newName.length() >= MIN_CHARS_NAME && newName.length() <= MAX_CHARS_NAME && !newName.equals("Jaap");
    }
}
