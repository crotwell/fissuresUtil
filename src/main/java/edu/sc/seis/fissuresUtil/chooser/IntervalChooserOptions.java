package edu.sc.seis.fissuresUtil.chooser;

/**
 * IntervalChooserOptions.java
 *
 *
 * Created: Fri Feb 15 14:36:51 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class IntervalChooserOptions {
    
    private IntervalChooserOptions (int i){
	
	option = i;
	this.minimumValue = 0;
	this.maximumValue = 10;
    }

    /**
     * sets the minimum value for this particular option.
     *
     * @param value an <code>int</code> value
     */
    public void setMinimumValue(int value) {

	this.minimumValue = value;

    }
    
    /**
     * sets the maximum value for this particular option.
     *
     * @param value an <code>int</code> value
     */
    public void setMaximumValue(int value) {

	this.maximumValue = value;
	
    }

    /**
     * returns the minimum value for this particular option.
     *
     * @return an <code>int</code> value
     */
    public int getMinimumValue() {

	return this.minimumValue;

    }

    /**
     * returns the  maximum value for this particular option.
     *
     * @return an <code>int</code> value
     */
    public int getMaximumValue() {

	return this.maximumValue;

    }

    /**
     * returns the integer value of this option.
     *
     * @return an <code>int</code> value
     */
    public int getIntervalChooserValue() {

	return option;

    }

    /**
     * returns the string representation of this option.
     *
     * @return a <code>String</code> value
     */
    public String toString() {

	switch(option) {

	case 0: return "seconds"; 
	case 1: return "minutes";
	case 2: return "hours";
	case 3: return "days";
	case 4: return "months";
	case 5: return "years";


	}
	return "error";

    }

    private int option, minimumValue, maximumValue;

   
    /**
     *  <code>SECOND</code>.
     *
     */
    public static final IntervalChooserOptions SECOND = new IntervalChooserOptions(0);
    /**
     * <code>MINUTE</code>.
     *
     */
    public static final IntervalChooserOptions MINUTE = new IntervalChooserOptions(1);
    /**
     * <code>HOUR</code>.
     *
     */
    public static final IntervalChooserOptions HOUR = new IntervalChooserOptions(2);
    /**
     * <code>DAY</code>.
     *
     */
    public static final IntervalChooserOptions DAY = new IntervalChooserOptions(3);
    /**
     * <code>MONTH</code>.
     *
     */
    public static final IntervalChooserOptions MONTH = new IntervalChooserOptions(4);
    /**
     * <code>YEAR</code>.
     *
     */
    public static final IntervalChooserOptions YEAR = new IntervalChooserOptions(5);
    
}// IntervalChooserOptions
