package edu.sc.seis.fissuresUtil.chooser;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;

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
     * constructor for the intervalchooser.
     *
     */
    public IntervalChooser (){
    initFrame();
    setEditable(true);
    }

    /**
     * constructor which takes an array of IntervalChooserOptions[]
     * Based on the IntervalChooserOptions the IntervalChooser is configured.
     * @param options an <code>IntervalChooserOptions[]</code> value
     */
    public IntervalChooser(IntervalChooserOptions[] options) {

    initFrame();
    populateUnits(options);
    setEditable(true);
    }

    /**
     * this function is used to create the GUI for the intervalChooser.
     *
     */
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

    /*
     * this method is used to populate the values of the valueBox of the intervalchooser.
     */

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

    for(int counter = -100; counter <= -10; counter = counter+10) {

        valueBox.addItem(new String(Integer.toString(counter)));
    }
    for(int counter = -9; counter <= 0; counter = counter + 1) {

        valueBox.addItem(new String(Integer.toString(counter)));
    }

    for(int counter = 1; counter <= 10; counter++) {

        valueBox.addItem(new String(Integer.toString(counter)));
    }
    for(int counter = 20; counter <= 100; counter = counter + 10) {

        valueBox.addItem(new String(Integer.toString(counter)));
    }
    }

    /*
     * this method is used to populate the units of the unitBox of the interval choooser.
     */

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
            // populateValues(newSelection);
              
        }

        });

    }
    /**
     * this method is used to set if the value displayed in the interval chooser is
     * editable or not.
     *
     * @param bool a <code>boolean</code> value
     */
    public void setEditable(boolean bool) {
    valueBox.setEditable(bool);
    }

    /**
     * sets the default value for the inteval chooser.
     *
     * @param index an <code>int</code> value
     * @param option an <code>IntervalChooserOptions</code> value
     */
    public void setDefault(int index, IntervalChooserOptions option) {
    
    unitBox.setSelectedItem(option);
    valueBox.setSelectedIndex(index + 1);

    }

    
    
    public void setSelectedValue(int value) {

    valueBox.setSelectedItem(String.valueOf(value));
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
     * This method adds the current value displayed in the intervalchooser, to the date passed to it
     * and returns the resultant date.
     *
     * @param date a <code>Date</code> value
     * @return a <code>Date</code> value
     */
    public Date addTo(Date date) {
    
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
