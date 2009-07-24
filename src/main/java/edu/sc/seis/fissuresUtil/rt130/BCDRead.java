package edu.sc.seis.fissuresUtil.rt130;

public class BCDRead {

    public static int toInt(byte input) {
        int inputInt = input;
        // Take care of signed bytes
        inputInt = (inputInt & 255);
        int firstFourBits = inputInt;
        firstFourBits = (firstFourBits >>> 4);
        int secondFourBits = inputInt;
        secondFourBits = (secondFourBits & 15);
        return ((firstFourBits * 10) + secondFourBits);
    }

    public static int toInt(byte[] input) {
        int value = 0;
        int j = 1;
        // This loop runs reverse of HexRead loop because the next value is
        // concatinated to the left of the beginning
        // value instead of to the right, as in the HexRead loop using Strings.
        for(int i = input.length - 1; i >= 0; i--) {
            value = (BCDRead.toInt(input[i]) * j) + value;
            j = j * 100;
        }
        return value;
    }

    // Code stolen from C-based refpacket and modified for java
    public static String toString(byte[] input) {
        int[] inputInt = new int[input.length];
        for(int i = 0; i < input.length; i++) {
            inputInt[i] = input[i];
            // Take care of signed bytes
            inputInt[i] = (inputInt[i] & 255);
        }
        char[] dest = new char[input.length * 2];
        for(int i = 0; i < inputInt.length; i++) {
            if((inputInt[i] >>> 4) < 10) {
                dest[i * 2] = (char)((inputInt[i] >>> 4) + 48);
            } else {
                dest[i * 2] = (char)((inputInt[i] >>> 4) + 55);
            }
            if((inputInt[i] & 15) < 10) {
                dest[i * 2 + 1] = (char)((inputInt[i] & 15) + 48);
            } else {
                dest[i * 2 + 1] = (char)((inputInt[i] & 15) + 55);
            }
        }
        return new String(dest);
    }
}
