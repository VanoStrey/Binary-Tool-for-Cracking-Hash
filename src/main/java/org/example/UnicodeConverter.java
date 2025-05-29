package org.example;

public class UnicodeConverter {

    public static String rangeCharacter = "";
    private Integer base;

    public UnicodeConverter(String rangeCharacter) {
        this.rangeCharacter = rangeCharacter;
        base = rangeCharacter.length();
    }

    public String unicodeToBase(String unicode) {
        unicode = new StringBuilder(unicode).reverse().toString();
        Long valuetsChars = 0l;
        for (int i = 0; i < unicode.length(); i++) {
            valuetsChars += ((long) unicode.charAt(i) * (long) Math.pow((double) (int) Character.MAX_VALUE, (double) i));
        }
        return Long.toString(valuetsChars, base);
    }

    public String unicodeToRangeString(String unicode) {
        return longToUnicode(unicodeToBase(unicode));
    }

    public String longToUnicode(String encoded) {
        long unicodeValue = Long.parseLong(encoded, base);
        StringBuilder result = new StringBuilder();

        while (unicodeValue > 0) {
            int index = (int) (unicodeValue % base);
            result.insert(0, rangeCharacter.charAt(index));
            unicodeValue /= base;
        }

        return result.toString();
    }
}

