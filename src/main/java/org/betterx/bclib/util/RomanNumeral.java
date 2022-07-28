package org.betterx.bclib.util;

import java.util.TreeMap;

/**
 * base on <a href="https://stackoverflow.com/questions/12967896/converting-integers-to-roman-numerals-java">...</a>
 */

public class RomanNumeral {
    private final static TreeMap<Integer, String> LITERALS = new TreeMap<>();

    private static void lazyInit() {
        if (LITERALS.isEmpty()) {
            LITERALS.put(1000, "M");
            LITERALS.put(900, "CM");
            LITERALS.put(500, "D");
            LITERALS.put(400, "CD");
            LITERALS.put(100, "C");
            LITERALS.put(90, "XC");
            LITERALS.put(50, "L");
            LITERALS.put(40, "XL");
            LITERALS.put(10, "X");
            LITERALS.put(9, "IX");
            LITERALS.put(5, "V");
            LITERALS.put(4, "IV");
            LITERALS.put(1, "I");
        }
    }

    public static String toRoman(int number) {
        lazyInit();
        return _toRoman(number);
    }

    private static String _toRoman(int number) {
        //there is no 0 in roman, but we need it anyway ;)
        if (number == 0) return "0";
        
        int l = LITERALS.floorKey(number);
        if (number == l) {
            return LITERALS.get(number);
        }
        return LITERALS.get(l) + _toRoman(number - l);
    }
}
