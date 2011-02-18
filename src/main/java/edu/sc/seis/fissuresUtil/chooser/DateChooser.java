package edu.sc.seis.fissuresUtil.chooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/** JPanel Utility that creates year, month, days, etc to be placed on a JFrame.
 *  It return only a Date object, from which you may obtain all the necessary information.
 *  DateChooser can be initialized by using the DateChooserOptions singleton:
 DateChooserOptions[] dateformat = new DateChooserOptions[6];
 dateformat[0] = DateChooserOptions.YEAR;
 dateformat[1] = DateChooserOptions.MONTH;
 dateformat[2] = DateChooserOptions.DAY;
 dateformat[3] = DateChooserOptions.HOUR;
 dateformat[4] = DateChooserOptions.MINUTES;
 dateformat[5] = DateChooserOptions.SECONDS;
 final DateChooser startDate = new DateChooser(dateformat);
 final DateChooser endDate = new DateChooser(dateformat);

 *
 * @author <a href="mailto:georginamc@prodigy.net">Georgina Coleman</a>
 * @created: 12 Nov 2001
 *
 */


public class DateChooser extends JPanel {


    public DateChooser() {
        this(DEFAULT_OPTIONS);
    } // constructor


    private static final DateChooserOptions[] DEFAULT_OPTIONS = {DateChooserOptions.YEAR, DateChooserOptions.MONTH, DateChooserOptions.DAY};


    public DateChooser(DateChooserOptions[] dateOptions ) {
        this(dateOptions, ClockUtil.now());
        currentTime = true;
    } // constructor

    public DateChooser(DateChooserOptions[] dateOptions, Date givendate) {
        initFrame();

        today = givendate;
        yesterday = new Date(today.getTime() - ONE_DAY);
        dateFormat = "dd MMMMM yyyy 'at' hh:mm:ss z";
        int option=0;

        calendar.setTime(today);
        todayCalendar.setTime(today);
        createComponents();

        for ( int i=0; i<dateOptions.length; i++) {
            option = dateOptions[i].getDateFormatValue();
            switch (option) {
                case 0:  yearOption(); break;
                case 1:  monthOption(); break;
                case 2:  dayOption(); break;
                case 3:  hourOption(); break;
                case 4:  minuteOption(); break;
                case 5:  secondOption(); break;
                //case 6:  millisOption(); break;
                case 7:  julianOption(); break;
                case 8:  todayOption(); break;
                case 9:  radioButtonOption(); break;
                case 10: weekagoOption(); break;
                case 11: intervalOption(); break;
            }
        }

    } // constructor

    protected void initFrame(){
        subPane.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
    }

    public void setHour(int value) {
        calendar.set(Calendar.HOUR_OF_DAY, value);
    }

    public void setMinute(int value) {
        calendar.set(Calendar.MINUTE, value);
    }

    private void radioButtonOption(){
        // Create the radio buttons.
        todayButton.setActionCommand("Today");
        yesButton.setActionCommand("Yesterday");
        ButtonGroup group = new ButtonGroup();
        group.add(todayButton);
        group.add(yesButton);
        group.add(otherButton);

        // Register a listener for the radio buttons.
        todayButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateToday();
                        calendar.setTime(today);
                        dateChanged();
                    }
                });
        yesButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateToday();
                        calendar.setTime(yesterday);
                        dateChanged();
                    }
                });

        gbc.gridx = upperLeftX;
        gbc.gridy = upperLeftY;
        subPane.add(todayButton,gbc);
        gbc.gridx++;
        subPane.add(yesButton,gbc);
        upperLeftY+=1;
    }

    public void setNumberOfYears(int totalyears){
        numberOfYears = totalyears;
        int addedyear = todayCalendar.get(Calendar.YEAR);
        String[] yearst = new String[numberOfYears];
        for(int i=0; i<numberOfYears ; i++) {
            yearst[i]= ""+addedyear;
            addedyear--;
        }
        yearBox.setModel(new DefaultComboBoxModel(yearst));
    }

    private void yearOption(){
        int todayYear = todayCalendar.get(Calendar.YEAR);
        String[] years = new String[numberOfYears];
        for(int i=0; i<numberOfYears ; i++) {
            years[i]= String.valueOf(todayYear--);
        }
        yearBox= new JComboBox(years);
        yearBox.setSelectedIndex(0);
        yearBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        yearBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        calendar.set(Calendar.YEAR,
                                     Integer.parseInt((String)cb.getSelectedItem()));
                        dateChanged();
                    }
                });
        gbc.gridx = upperLeftX++;
        gbc.gridy = upperLeftY;

        subPane.add(new JLabel("Year"), gbc);
        gbc.gridheight = 2;
        gbc.gridy++;
        subPane.add(yearBox, gbc);
    }

    private void monthOption(){
        monthBox= new JComboBox(MONTHS);
        monthBox.setSelectedIndex(calendar.get(Calendar.MONTH));
        monthBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        monthBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        calendar.set(Calendar.MONTH, cb.getSelectedIndex());
                        dateChanged();
                    }
                });

        gbc.gridx = upperLeftX++;
        gbc.gridy = upperLeftY;

        subPane.add(new JLabel("Month"), gbc);
        gbc.gridy++;
        subPane.add(monthBox, gbc);
    }

    private static final String[] MONTHS = {"January", "February", "March", "April", "May",
            "June", "July", "August", "September", "October", "November",
            "December"};

    private void dayOption(){
        String[] dayStrings = {"1","2","3","4","5","6","7","8","9","10",
                "11","12","13","14","15","16","17","18","19","20",
                "21","22","23","24","25","26","27","28","29","30","31"};

        dayBox= new JComboBox(dayStrings);
        dayBox.setSelectedIndex(calendar.get(Calendar.DAY_OF_MONTH) - 1);
        dayBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        dayBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        calendar.set(Calendar.DAY_OF_MONTH,
                                     cb.getSelectedIndex() + 1);
                        dateChanged();
                    }
                });

        gbc.gridx =upperLeftX++;
        gbc.gridy = upperLeftY;

        subPane.add(new JLabel("Day"), gbc);
        gbc.gridy++;
        subPane.add(dayBox, gbc);
    }

    private void hourOption(){
        int hoursInDay = 24;
        String[] hourStringArray = new String[hoursInDay];
        for(int i=0; i<hoursInDay ; i++) {
            hourStringArray[i]= String.valueOf(i);
        }

        hourBox= new JComboBox(hourStringArray);
        hourBox.setSelectedIndex(calendar.get(Calendar.HOUR_OF_DAY));
        hourBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hourBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        calendar.set(Calendar.HOUR_OF_DAY, cb.getSelectedIndex());
                        dateChanged();
                    }
                });

        gbc.gridx = upperLeftX++;
        gbc.gridy = upperLeftY;
        subPane.add(new JLabel("Hour"), gbc);
        gbc.gridy++;
        subPane.add(hourBox, gbc);
    }

    private void minuteOption(){
        int minutesInHour = 60;
        String[] minuteStringArray = new String[minutesInHour];
        for(int i=0; i<minutesInHour ; i++) {
            minuteStringArray[i]= String.valueOf(i);
        }

        minuteBox= new JComboBox(minuteStringArray);
        minuteBox.setSelectedIndex(calendar.get(Calendar.MINUTE));
        minuteBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        minuteBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        calendar.set(Calendar.MINUTE, cb.getSelectedIndex());
                        dateChanged();
                    }
                });
        gbc.gridx = ++upperLeftX;
        gbc.gridy = upperLeftY;
        subPane.add(new JLabel("Minute"), gbc);
        gbc.gridy++;
        subPane.add(minuteBox, gbc);
    }


    private void secondOption(){
        int secondsInMinute = 60;
        String[] secst = new String[secondsInMinute];
        for(int i=0; i<secondsInMinute ; i++) {
            secst[i]= String.valueOf(i);
        }
        secondBox= new JComboBox(secst);
        secondBox.setSelectedIndex(calendar.get(Calendar.SECOND));
        secondBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        secondBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        calendar.set(Calendar.SECOND, cb.getSelectedIndex());
                        dateChanged();
                    }
                });
        gbc.gridx = ++upperLeftX;
        gbc.gridy = upperLeftY;
        subPane.add(new JLabel("Second"), gbc);
        gbc.gridy++;
        subPane.add(secondBox, gbc);
    }

    private void julianOption() {
        Integer dayint = new Integer(calendar.get(Calendar.DAY_OF_YEAR));
        String dayString= dayint.toString();
        julianDay = new JTextField(dayString);
        julianDay.setAlignmentX(Component.LEFT_ALIGNMENT);
        julianDay.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        calendar.set(Calendar.DAY_OF_YEAR, Integer.parseInt(julianDay.getText()));
                        dateChanged();
                    }
                });
        gbc.gridy = upperLeftY;
        gbc.gridx++;
        subPane.add(new JLabel("JulianDay"), gbc);
        gbc.gridy++;
        subPane.add(julianDay, gbc);
    }

    private void todayOption(){
        // Create the UI for displaying result
        JLabel resultLabel = new JLabel("Current Date/Time", JLabel.LEFT);
        result = new JLabel(" ");
        result.setForeground(Color.black);
        result.setBorder(BorderFactory.createCompoundBorder( BorderFactory.createLineBorder(Color.black),
                                                            BorderFactory.createEmptyBorder(5,5,5,5)));
        JPanel resultPanel = new JPanel(new GridLayout(0, 1));
        resultPanel.add(resultLabel);
        resultPanel.add(result);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(resultPanel);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        reformat(dateFormat);
    }


    private void intervalOption() {
        ButtonGroup buttonGroup = new ButtonGroup();
        final JRadioButton weekButton = new JRadioButton("weekAgo");
        final JRadioButton monthButton = new JRadioButton("monthAgo");
        final JRadioButton yearButton = new JRadioButton("yearAgo");
        final JComboBox valueBox = new JComboBox();


        weekButton.setSelected(true);
        buttonGroup.add(weekButton);
        buttonGroup.add(monthButton);
        buttonGroup.add(yearButton);

        gbc.gridx = upperLeftX;
        gbc.gridy = upperLeftY;

        gbc.gridx++;gbc.gridx++;
        subPane.add(new JLabel("Number"));
        gbc.gridx++;
        populateComboBox(valueBox, 1, 3);
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        tempCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        calendar = tempCalendar;

        subPane.add(valueBox);

        gbc.gridx++;

        subPane.add(weekButton);
        gbc.gridx++;
        subPane.add(monthButton);
        gbc.gridx++;
        subPane.add(yearButton);


        weekButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        populateComboBox(valueBox, 1, 3);
                    }
                });
        monthButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        populateComboBox(valueBox, 1, 11);

                    }
                });
        yearButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        populateComboBox(valueBox, 1, 4);
                    }
                });

        valueBox.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        JComboBox cb = (JComboBox)e.getSource();
                        String newSelection = (String)cb.getSelectedItem();
                        int value;
                        try {
                            value = Integer.parseInt(newSelection);
                        } catch(NumberFormatException nfe) {


                            value = -1;

                        }
                        if(value == -1) return;
                        Calendar tempCalendar = Calendar.getInstance();
                        tempCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                        if( weekButton.isSelected()) {
                            tempCalendar.add(Calendar.WEEK_OF_YEAR, -value);
                        }
                        else if( monthButton.isSelected()) {

                            tempCalendar.add(Calendar.MONTH, -value);
                        }
                        else if( yearButton.isSelected()) {

                            tempCalendar.add(Calendar.YEAR, -value);

                        }


                        calendar = tempCalendar;
                    }

                });



    }

    private void populateComboBox(JComboBox comboBox, int start , int end) {
        comboBox.removeAllItems();
        for(int counter = start; counter <= end; counter++) {
            comboBox.addItem(new String(new Integer(counter).toString()));

        }
        comboBox.setSelectedIndex(0);
    }

    private void weekagoOption() {
        int numberofweeks = 3;
        weekAgoBox= new JComboBox();
        for(int counter = 1; counter <= numberofweeks; counter++) {
            weekAgoBox.addItem( new String(new Integer(counter).toString()));
        }
        weekAgoBox.setSelectedIndex(0);
        weekAgoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        weekAgoBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox)e.getSource();
                        String newSelection = (String)cb.getSelectedItem();
                        //min = Integer.parseInt(newSelection);
                        //calendar.set(Calendar.MINUTE, min);
                        //dateChanged();
                    }
                });
        gbc.gridx = ++upperLeftX;
        gbc.gridy = upperLeftY;
        subPane.add(new JLabel("Week  Ago"), gbc);
        gbc.gridy++;
        subPane.add(weekAgoBox, gbc);
    }

    protected void createComponents() {
        //Initialize drawing colors, border, opacity.
        subPane.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = upperLeftX=0;
        gbc.gridy = upperLeftY=0;

        add(subPane);
        add(Box.createRigidArea(new Dimension(0, 10)));

        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    }

    private void updateToday(){
        if(currentTime){
            today = ClockUtil.now();
            yesterday = new Date(today.getTime() - ONE_DAY);
            todayCalendar.setTime(today);
        }
    }

    public boolean isToday() {
        dateChanged();
        return todayButton.isSelected();
    }

    private void dateChanged(){
        updateToday();
        int  todaysYear = todayCalendar.get(Calendar.YEAR);
        int calJulianDay = calendar.get(Calendar.DAY_OF_YEAR);
        int  todaysJulianDay = todayCalendar.get(Calendar.DAY_OF_YEAR);
        int calYear = calendar.get(Calendar.YEAR);
        otherButton.setSelected(true);
        if(calYear == todaysYear){
            if(yearBox != null)
                yearBox.setSelectedIndex(0);
            if(todaysJulianDay == calJulianDay){
                todayButton.setSelected(true);
            }
            if(calJulianDay ==  todaysJulianDay-1 ){
                yesButton.setSelected(true);
            }
        } else if(yearBox != null) {
            int indexofyear = todaysYear - calYear;
            yearBox.setSelectedIndex(indexofyear);
        }
        if(monthBox != null)
            monthBox.setSelectedIndex(calendar.get(Calendar.MONTH));
        if(dayBox != null)
            dayBox.setSelectedIndex(calendar.get(Calendar.DAY_OF_MONTH) - 1);
        if(hourBox != null)
            hourBox.setSelectedIndex(calendar.get(Calendar.HOUR_OF_DAY));
        if(minuteBox != null)
            minuteBox.setSelectedIndex(calendar.get(Calendar.MINUTE));
        if(secondBox != null)
            secondBox.setSelectedIndex(calendar.get(Calendar.SECOND));
        repaint();
    }


    /** Formats and displays today's date. */
    public void reformat() {
        Date today = new Date();
        SimpleDateFormat formatter =
            new SimpleDateFormat(dateFormat);
        try {
            String dateString = formatter.format(today);
            result.setForeground(Color.black);
            result.setText(dateString);
        } catch (IllegalArgumentException iae) {
            result.setForeground(Color.red);
            result.setText("Error: " + iae.getMessage());
        }
    }

    /** Formats and displays today's date. */
    public void reformat(String newDateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(newDateFormat);
        try {
            String dateString = formatter.format(ClockUtil.now());
            result.setForeground(Color.black);
            result.setText(dateString);
        } catch (IllegalArgumentException iae) {
            result.setForeground(Color.red);
            result.setText("Error: " + iae.getMessage());
        }
    }

    public void setDate(Date newDate ){
        calendar.setTime(newDate);
        dateChanged();
    }

    public Date getDate(){
        dateChanged();
        return calendar.getTime();
    }

    private JPanel subPane = new JPanel();

    private GridBagConstraints gbc;
    private int upperLeftX=0;
    private int upperLeftY=0;

    private static final long ONE_DAY = 24*60*60*1000;

    //today is something of a misnomer.  If the boolean current time is true,
    //today should always represent the current day.  However, in the other
    //state date chooser works off the assumption that today is some date
    //other than today
    private Date today;
    private Date yesterday;

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    private Calendar todayCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private JRadioButton todayButton = new JRadioButton("Today");
    private JRadioButton yesButton = new JRadioButton("Yesterday");
    private JRadioButton otherButton = new JRadioButton("Other");


    private JTextField julianDay;
    private JComboBox yearBox, monthBox, dayBox, hourBox, minuteBox, secondBox;

    private JComboBox weekAgoBox;

    private JLabel result;
    private String dateFormat;

    private int numberOfYears=5;

    //if current time is true, the value of today is updated every time the day
    //changes to insure that today rolls over as time progresses
    private boolean currentTime = false;
}
