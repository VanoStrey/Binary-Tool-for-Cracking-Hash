package org.example;

public class Main {
    public static void main(String[] args) {
        UnicodeConverter unicodeConverter = new UnicodeConverter("0123456789qwert");

        UnicodeCombinationsGenerator generator = new UnicodeCombinationsGenerator("unicode_combinations.txt", 2);
        //generator.generateFile();
        String filePath = "unicode_combinations.txt";
        int symbolsPerElement = 2;

        SymbolFileAccessor accessor = new SymbolFileAccessor(filePath, symbolsPerElement);
        long elementIndex = accessor.getTotalElements() / 2;
        String element = accessor.getElement(elementIndex);
        if (element != null ) {
            System.out.println("Элемент с индексом " + elementIndex + ":");
            System.out.println(unicodeConverter.unicodeToRangeString(element));
        } else {
            System.out.println("Элемент с индексом " + elementIndex + " не найден (индекс вне диапазона).");
        }
    }
}