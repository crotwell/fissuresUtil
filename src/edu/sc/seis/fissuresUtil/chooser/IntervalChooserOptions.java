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

    public void setMinimumValue(int value) {

	this.minimumValue = value;

    }
    
    public void setMaximumValue(int value) {

	this.maximumValue = value;
	
    }

    public int getMinimumValue() {

	return this.minimumValue;

    }

    public int getMaximumValue() {

	return this.maximumValue;

    }

    public int getIntervalChooserValue() {

	return option;

    }

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

    public static final IntervalChooserOptions SECOND = new IntervalChooserOptions(0);
    public static final IntervalChooserOptions MINUTE = new IntervalChooserOptions(1);
    public static final IntervalChooserOptions HOUR = new IntervalChooserOptions(2);
    public static final IntervalChooserOptions DAY = new IntervalChooserOptions(3);
    public static final IntervalChooserOptions MONTH = new IntervalChooserOptions(4);
    public static final IntervalChooserOptions YEAR = new IntervalChooserOptions(5);
    
}// IntervalChooserOptions
