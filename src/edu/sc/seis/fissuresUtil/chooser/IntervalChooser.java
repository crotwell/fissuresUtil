package edu.sc.seis.fissuresUtil.chooser;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.lang.*;

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
    public IntervalChooser (){
	initFrame();
	populateUnits();
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

    private void populateValues(String unit) {
	int minValue = 0;
	int maxValue = 0;
	valueBox.removeAllItems();
	if( unit.equals("sec") ) { minValue = 1; maxValue = 60;}
	else if(unit.equals("min")) { minValue = 1; maxValue = 60;}
	else if(unit.equals("hr")) { minValue = 1; maxValue = 24;}
	else if(unit.equals("day")) { minValue = 1; maxValue = 31;}
	else if(unit.equals("month")) { minValue = 1; maxValue = 12;}
	else if(unit.equals("year")) { minValue = 1; maxValue = 4;}

	for(int counter = 1; counter <= maxValue; counter++) {
	
	    valueBox.addItem(new String(new Integer(counter).toString()));

	}
	valueBox.setSelectedIndex(10);
	

    }

    private void populateUnits() {
    
	String[] units = {"sec","min","hr","day","month","year"}; 
	for( int counter = 0; counter < units.length; counter++) {

	    unitBox.addItem(units[counter]);
	}
	unitBox.setSelectedIndex(1);
	populateValues(units[1]);
	unitBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JComboBox cb = (JComboBox)e.getSource();
		    String newSelection = (String)cb.getSelectedItem();
		    System.out.println("The new value selected is "+newSelection);
		    populateValues(newSelection);
              
		}

	    });

    }

    public Date getDate(Date date) {
	
	Calendar calendar = Calendar.getInstance(); 
	calendar.setTime(date);
	int rollBackValue;
	try {
	    
	    rollBackValue = Integer.parseInt((String)valueBox.getSelectedItem());

	} catch(NumberFormatException nfe) {

	    rollBackValue = 0;
	}
	String unit = (String)unitBox.getSelectedItem();

	java.util.Date returnDate = new java.util.Date();
	
	if( unit.equals("sec") ) { 
	    calendar.add(Calendar.SECOND, rollBackValue);
	    //returnDate.setTime(calendar.getTime().getTime() + rollBackValue * 1000);
	}
	else if(unit.equals("min")) { 
	    calendar.add(Calendar.MINUTE, rollBackValue);
	    //returnDate.setTime(calendar.getTime().getTime() + rollBackValue * 1000 *60); 
	}
	else if(unit.equals("hr")) {  
	    calendar.add(Calendar.HOUR, rollBackValue);
	    //returnDate.setTime(calendar.getTime().getTime() + rollBackValue * 1000 * 60 * 60);
	}
	else if(unit.equals("day")) {  
	    calendar.add(Calendar.DAY_OF_YEAR, rollBackValue);
	    //returnDate.setTime(calendar.getTime().getTime() + rollBackValue * 1000 * 60 * 60 * 24);
	}
	else if(unit.equals("month")) {  
	    calendar.add(Calendar.MONTH, rollBackValue);
	    //returnDate.setTime(calendar.getTime().getTime() + rollBackValue * 1000 * 60 * 24 * 31 );
	}
	else if(unit.equals("year")) {  
	    calendar.add(Calendar.YEAR, rollBackValue);
	    //returnDate.setTime(calendar.getTime().getTime() + rollBackValue * 1000 * 60 * 60 * 24 * 31 * 12  );
	}
	
	return calendar.getTime();

    }
    private GridBagConstraints constraints;

    private GridBagLayout bagLayout;

    private JComboBox unitBox;

    private JComboBox valueBox;


}// IntervalChooser
