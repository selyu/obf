package org.selyu.obf.core.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public final class StringUtil {
    private static final char[] CHARACTERS = {
            'i',
            'I',
            'l',
    };
    private static final Set<String> SEEN = new HashSet<>();
    private static final SecureRandom RANDOM = new SecureRandom();

    private StringUtil() {
    }

    private static String getRandom(int length) {
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < length; i++) {
            random.append(CHARACTERS[RANDOM.nextInt(CHARACTERS.length)]);
        }
        return random.toString();
    }

    /**
     * @param length The length of the string
     * @return a unique string of characters
     */
    public static String get(int length) {
        String uniqueString = getRandom(length);
        while (SEEN.contains(uniqueString))
            uniqueString = getRandom(length);

        SEEN.add(uniqueString);
        return uniqueString;
    }
}
