package edu.sc.seis.fissuresUtil.chooser;

/** DateChooserOptions serves as an enumeration for DateChooser 
 * 
 * @author Georgina Coleman
 * @created: 12 Nov 2001
 *
 */

public class DateChooserOptions{

    private int option; 
    public static final DateChooserOptions YEAR = new DateChooserOptions(0);
    public static final DateChooserOptions MONTH = new DateChooserOptions(1);
    public static final DateChooserOptions DAY = new DateChooserOptions(2);
    public static final DateChooserOptions HOUR = new DateChooserOptions(3);
    public static final DateChooserOptions MINUTES = new DateChooserOptions(4); 
    public static final DateChooserOptions SECONDS = new DateChooserOptions(5);
    public static final DateChooserOptions MILLIS = new DateChooserOptions(6);
    public static final DateChooserOptions JULIANDAY = new DateChooserOptions(7);
    public static final DateChooserOptions TODAY = new DateChooserOptions(8);
    public static final DateChooserOptions RADIOBUTTON = new DateChooserOptions(9);
    public static final DateChooserOptions WEEKAGO = new DateChooserOptions(10);
    public static final DateChooserOptions INTERVAL = new DateChooserOptions(11);

    private DateChooserOptions(int i){
	option=i;
    }

    public int getDateFormatValue(){
	return option;
    }

    public void setDateFormat(String d){

	//initialize my date

    } 

}

