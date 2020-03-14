package milkman.utils;

public class StringUtils {

    /**
     * tests, if a string contains all given letterns in the given order but not necessarily next to each other:
     *
     *
     * containsLettersInOrder("test", "tt") = true
     * containsLettersInOrder("test", "se") = false
     *
     * @param string
     * @param letters
     * @return
     */
    public static boolean containsLettersInOrder(String string, String letters) {
        if (letters == null || letters.isEmpty())
            return true;

        int index = 0;
        for (char character : letters.toCharArray()) {
            index = string.indexOf(character, index);
            if (index == -1)
                return false;
            index += 1;
        }

        return true;
    }
}
