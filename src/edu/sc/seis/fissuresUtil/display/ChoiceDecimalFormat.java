package edu.sc.seis.fissuresUtil.display;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * @author groves Created on Mar 30, 2005
 */
public class ChoiceDecimalFormat extends NumberFormat {

    /**
     * for each double in limits, if a number is passed into format that is less
     * than it the corresponding format from formats is used. if the number is
     * greater than the last value in limits, the last format is used.
     */
    public ChoiceDecimalFormat(double[] limits, DecimalFormat[] formats) {
        if(limits.length != formats.length) { throw new IllegalArgumentException("Must be an equal number of limits and formats"); }
        this.limits = limits;
        this.formats = formats;
    }

    public Number parse(String source, ParsePosition parsePosition) {
        throw new UnsupportedOperationException("ChoiceDecimalFormat aint your number parser.  Use one of the java classes DecimalFormat or ChoiceFormat");
    }

    public StringBuffer format(double number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        for(int i = 0; i < limits.length; i++) {
            if(number < limits[i]) { return formats[i].format(number,
                                                              toAppendTo,
                                                              pos); }
        }
        return formats[formats.length - 1].format(number, toAppendTo, pos);
    }

    public StringBuffer format(long number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        for(int i = 0; i < limits.length; i++) {
            if(number < limits[i]) { return formats[i].format(number,
                                                              toAppendTo,
                                                              pos); }
        }
        return formats[formats.length - 1].format(number, toAppendTo, pos);
    }

    /**
     * This creates a ChoiceDecimalFormat where numbers < 100 have a single decimal, and numbers >=100
     *  have none
     */
    public static ChoiceDecimalFormat createTomStyleA() {
        return new ChoiceDecimalFormat(new double[] {100, 100},
                                       new DecimalFormat[] {new DecimalFormat("0.0"),
                                                            new DecimalFormat("0")});
    }

    /**
     * This creates a ChoiceDecimalFormat where numbers < 10 have a single decimal, and numbers >=10
     * have none
     */
    public static ChoiceDecimalFormat createTomStyleB() {
        return new ChoiceDecimalFormat(new double[] {10, 10},
                                       new DecimalFormat[] {new DecimalFormat("0.0"),
                                                            new DecimalFormat("0")});
    }

    private double[] limits;

    private DecimalFormat[] formats;
}