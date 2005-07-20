package edu.sc.seis.fissuresUtil.rt130;

public class HexRead {

    public static String toString(byte input)
            throws OneHundredThirtyFormatException {
        int inputInt = input;
        // Take care of negatives.
        inputInt = (inputInt & 255);
        int firstFourBitsInt = inputInt;
        firstFourBitsInt = (firstFourBitsInt >>> 4);
        int secondFourBitsInt = inputInt;
        secondFourBitsInt = (secondFourBitsInt & 15);
        String firstFourBits = String.valueOf(firstFourBitsInt);
        String secondFourBits = String.valueOf(secondFourBitsInt);
        if(firstFourBitsInt >= 10) {
            if(firstFourBitsInt == 10) {
                firstFourBits = "A";
            } else if(firstFourBitsInt == 11) {
                firstFourBits = "B";
            } else if(firstFourBitsInt == 12) {
                firstFourBits = "C";
            } else if(firstFourBitsInt == 13) {
                firstFourBits = "D";
            } else if(firstFourBitsInt == 14) {
                firstFourBits = "E";
            } else if(firstFourBitsInt == 15) {
                firstFourBits = "F";
            } else {
                System.err.println("The fifth and sixth bytes of the Packet Header were not formatted correctly, and do not refer to a valid hexadecimal.");
                throw new OneHundredThirtyFormatException();
            }
        }
        if(secondFourBitsInt >= 10) {
            if(secondFourBitsInt == 10) {
                secondFourBits = "A";
            } else if(secondFourBitsInt == 11) {
                secondFourBits = "B";
            } else if(secondFourBitsInt == 12) {
                secondFourBits = "C";
            } else if(secondFourBitsInt == 13) {
                secondFourBits = "D";
            } else if(secondFourBitsInt == 14) {
                secondFourBits = "E";
            } else if(secondFourBitsInt == 15) {
                secondFourBits = "F";
            } else {
                System.err.println("The fifth and sixth bytes of the Packet Header were not formatted correctly, and do not refer to a valid hexadecimal.");
                throw new OneHundredThirtyFormatException();
            }
        }
        return firstFourBits + secondFourBits;
    }

    public static String toString(byte[] input)
            throws OneHundredThirtyFormatException {
        String value = "";
        for(int i = 0; i < input.length; i++) {
            value = value.concat(HexRead.toString(input[i]));
        }
        return value;
    }
}
