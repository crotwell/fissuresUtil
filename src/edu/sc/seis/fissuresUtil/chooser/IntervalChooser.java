package edu.sc.seis.fissuresUtil.chooser;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.lang.*;
import edu.iris.Fissures.model.*;

/**
 * IntervalChooser.java
 *
 *
 * Created: Thu Feb  7 10:14:27 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class IntervalChooser extends JPanel{
    /**
     * Creates a new <code>IntervalChooser</code> instance.
     *
     */
    public IntervalChooser (){
	initFrame();
	setEditable(true);
    }

    /**
     * Creates a new <code>IntervalChooser</code> instance.
     *
     * @param options an <code>IntervalChooserOptions[]</code> value
     */
    public IntervalChooser(IntervalChooserOptions[] options) {

	initFrame();
	populateUnits(options);
	setEditable(true);
    }

    private void initFrame() {
    
	bagLayout = new GridBagLayout();
	constraints = new GridBagConstraints();
	valueBox = new JComboBox();
	unitBox = new JComboBox();

	constraints.weightx = 1.0;
	constraints.weighty = 1.0;
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.fill = constraints.BOTH;
	this.setLayout(bagLayout);

	bagLayout.setConstraints(valueBox, constraints);
	this.add(valueBox);

	constraints.gridx = constraints.gridx + 1;
	bagLayout.setConstraints(unitBox, constraints);
	this.add(unitBox);

    }

    private void populateValues(IntervalChooserOptions option) {
	/*int tempValue = valueBox.getSelectedIndex();
	valueBox.removeAllItems();
	int minValue = option.getMinimumValue();
	int maxValue = option.getMaximumValue();
	for(int counter = minValue; counter <= maxValue; counter++) {
	
	    valueBox.addItem(new String(new Integer(counter).toString()));

	}
	if(tempValue > maxValue) tempValue = maxValue;
	valueBox.setSelectedIndex(tempValue);
	*/
	for(int counter = 1; counter <= 10; counter++) {

	    valueBox.addItem(new String(Integer.toString(counter)));
	}
	for(int counter = 20; counter <= 100; counter = counter + 10) {

	    valueBox.addItem(new String(Integer.toString(counter)));
	}
    }

    private void populateUnits(IntervalChooserOptions[] options) {
    
	for( int counter = 0; counter < options.length; counter++) {

	    unitBox.addItem(options[counter]);

	}
	unitBox.setSelectedIndex(1);
	populateValues(options[1]);
	valueBox.setSelectedIndex(9);
	unitBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JComboBox cb = (JComboBox)e.getSource();
		    IntervalChooserOptions newSelection = (IntervalChooserOptions)cb.getSelectedItem();
		    System.out.println("The new value selected is "+newSelection.getIntervalChooserValue());
		    // populateValues(newSelection);
              
		}

	    });

    }
    /**
     * Describe <code>setEditable</code> method here.
     *
     * @param bool a <code>boolean</code> value
     */
    public void setEditable(boolean bool) {
	valueBox.setEditable(bool);
    }

    /**
     * Describe <code>setDefault</code> method here.
     *
     * @param index an <code>int</code> value
     * @param option an <code>IntervalChooserOptions</code> value
     */
    public void setDefault(int index, IntervalChooserOptions option) {
	
	unitBox.setSelectedItem(option);
	valueBox.setSelectedIndex(index + 1);

    }

    /**
     * This method returns TimeInterval. This method considers  1 MONTH = 30 DAYS and
     * 1 YEAR = 365 DAYS. For accurate values use the addTo(Date date).
     * @return a <code>TimeInterval</code> value
     */
    public TimeInterval getInterval() {
	
	int intervalValue;
	TimeInterval interval = null;
	try {
	    
	    intervalValue = Integer.parseInt((String)valueBox.getSelectedItem());

	} catch(NumberFormatException nfe) {
	    intervalValue = 0;
	}
	IntervalChooserOptions option = (IntervalChooserOptions)unitBox.getSelectedItem();
	int unitValue = option.getIntervalChooserValue();
	
	if( unitValue == 0 ) { 
	    interval = new TimeInterval(intervalValue, UnitImpl.SECOND);
	}
	else if(unitValue == 1 ) {
	    interval = new TimeInterval(intervalValue, UnitImpl.MINUTE);
	}
	else if(unitValue == 2 ) {
	    interval = new TimeInterval(intervalValue, UnitImpl.HOUR);
	}
	else if(unitValue == 3) {
	    interval = new TimeInterval(intervalValue, UnitImpl.DAY);
	}
	else if(unitValue == 4){
	    interval = new TimeInterval(intervalValue * 30, UnitImpl.DAY);
	}
	else if(unitValue == 5) {
	    interval = new TimeInterval(intervalValue * 365, UnitImpl.DAY);
	}
	
	return interval;
    }

    /**
     * Describe <code>getDate</code> method here.
     *
     * @param date a <code>Date</code> value
     * @return a <code>Date</code> value
     */
    public Date getDate(Date date) {
	
	Calendar calendar = Calendar.getInstance(); 
	calendar.setTime(date);
	int rollBackValue;
	try {
	    
	    rollBackValue = Integer.parseInt((String)valueBox.getSelectedItem());

	} catch(NumberFormatException nfe) {

	    rollBackValue = 0;
	}
	IntervalChooserOptions option = (IntervalChooserOptions)unitBox.getSelectedItem();
	
	int unitValue = option.getIntervalChooserValue();
	
	if( unitValue == 0 ) { 
	    calendar.add(Calendar.SECOND, rollBackValue);
	}
	else if(unitValue == 1 ) {
	    calendar.add(Calendar.MINUTE, rollBackValue);
	}
	else if(unitValue == 2 ) {
	    calendar.add(Calendar.HOUR, rollBackValue);
	}
	else if(unitValue == 3) {
	    calendar.add(Calendar.DAY_OF_YEAR, rollBackValue);
	}
	else if(unitValue == 4){
	    calendar.add(Calendar.MONTH, rollBackValue);
	}
	else if(unitValue == 5) {
	    calendar.add(Calendar.YEAR, rollBackValue);
	}
	
	return calendar.getTime();

    }
    private GridBagConstraints constraints;

    private GridBagLayout bagLayout;

    private JComboBox unitBox;

    private JComboBox valueBox;
   }// IntervalChooser
