package edu.sc.seis.fissuresUtil.chooser;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.text.*;
import java.lang.*;

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
    
        initFrame(); 
	DateChooserOptions[] dateformat = new DateChooserOptions[3];
        dateformat[0] = DateChooserOptions.YEAR;
        dateformat[1] = DateChooserOptions.MONTH;
        dateformat[2] = DateChooserOptions.DAY;
    
      currentDate = "dd MMMMM yyyy 'at' hh:mm:ss z";      
      int option=0;
      calendar = Calendar.getInstance();
      calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      // calendar.set(Calendar.HOUR_OF_DAY, 0);
      
      //  calendar.setTime(today);
      //       todaycalendar.setTime(today);
      todaycalendar = Calendar.getInstance();
      todaycalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      createComponents();

      for ( int arrayi=0; arrayi<dateformat.length; arrayi++) {

          option = dateformat[arrayi].getDateFormatValue();

	  switch (option) {
	      case 0:  yearOption(); break;
              case 1:  monthOption(); break;
              case 2:  dayOption(); break;
        
          }
      }
 
   } // constructor

    
      
  public DateChooser(DateChooserOptions[] dateformat ) {
      initFrame();    
      currentDate = "dd MMMMM yyyy 'at' hh:mm:ss z";      
      int option=0;
     
      //      calendar.setTime(today);
      calendar = Calendar.getInstance();
      calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      //todaycalendar.setTime(today);
      todaycalendar = Calendar.getInstance();
      todaycalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      createComponents();

      for ( int arrayi=0; arrayi<dateformat.length; arrayi++) {

          option = dateformat[arrayi].getDateFormatValue();
	  
	  switch (option) {
	  case 0:  yearOption(); break;
	  case 1:  monthOption(); break;
	  case 2:  dayOption(); break;
	  case 3:  hourOption(); break;
	  case 4:  minuteOption(); break;
	  case 5:  secondOption(); break;
	  case 6:  millisOption(); break;
	  case 7:  julianOption(); break;
	  case 8:  todayOption(); break;    
	  case 9:  radioButtonOption(); break;
	  case 10: weekagoOption();break;
	  case 11:intervalOption();break;
          }
      }

   } // constructor

  public DateChooser(DateChooserOptions[] dateformat, Date givendate) {
      initFrame();      
      today = givendate;
      currentDate = "dd MMMMM yyyy 'at' hh:mm:ss z";      
      int option=0;
     
      calendar.setTime(today);
      todaycalendar.setTime(today);
      createComponents();

      for ( int arrayi=0; arrayi<dateformat.length; arrayi++) {

          option = dateformat[arrayi].getDateFormatValue();

	  switch (option) {
	  case 0:  yearOption(); break;
	  case 1:  monthOption(); break;
	  case 2:  dayOption(); break;
	  case 3:  hourOption(); break;
	  case 4:  minuteOption(); break;
	  case 5:  secondOption(); break;
	  case 6:  millisOption(); break;
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
	 gbc.fill = gbc.HORIZONTAL;
	 gbc.weightx = 1.0;
	 gbc.weighty = 1.0;
	 gbc.gridx = 0;
	 gbc.gridy = 0;
    }

    public void initPanel(){
      //Initialize drawing colors, border, opacity.
         //subPane.setBackground(bg);
         //subPane.setForeground(fg);	
         /**subPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));**/ 
    }


    public void setHour(int value) {

	calendar.set(Calendar.HOUR_OF_DAY, value);

    }

    public void setMinute(int value) {

	calendar.set(Calendar.MINUTE, value);
    }

    private void radioButtonOption(){
        // Create the radio buttons.
	JLabel introLabel = new JLabel("Date: ");
     
        //todayButton.setMnemonic(KeyEvent.VK_B);
        todayButton.setActionCommand("Today");
        todayButton.setSelected(true);

        yesButton.setActionCommand("Yesterday");
	otherButton.setActionCommand("Other");
        // Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(todayButton);
	group.add(yesButton);
	group.add(otherButton);

        // Register a listener for the radio buttons.		
        todayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
		calendar.setTime(today);   
                day = calendar.get(Calendar.DAY_OF_MONTH);
	        daybox.setSelectedIndex(--day);   
                dateChanged();
            }
        });

        yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calendar.setTime(today);  
		calendar.roll(Calendar.DAY_OF_YEAR, false);

                //int julianday = calendar.get(Calendar.DAY_OF_YEAR);
                //yesButton.setSelected(false);
	        //int yesterday = julianday-1;
                //calendar.set(Calendar.DAY_OF_YEAR, yesterday);         
                dateChanged();
            }
        });

	gbc.gridx = x_leftcorner;
	gbc.gridy = y_leftcorner;

	gbc.gridx++;	
	subPane.add(introLabel,gbc);	

	//	gbc.gridx++;
	gbc.gridy++;	
	subPane.add(todayButton,gbc);
	gbc.gridx++;	
	subPane.add(yesButton,gbc);

        x_leftcorner+=2;

	return;


    }

    public void setNumberOfYears(int totalyears){   
	numberofyears = totalyears;
	int addedyear = todaycalendar.get(Calendar.YEAR);
	String[] yearst = new String[numberofyears];
        for(int i=0; i<numberofyears ; i++) {
	    yearst[i]= ""+addedyear;
	    addedyear--;
	}
	yearbox.setModel(new DefaultComboBoxModel(yearst));
    }

    private void yearOption(){

        int  todayyear = todaycalendar.get(Calendar.YEAR);   

        //String[] yearst = {"2001","2000","1999","1998"};
	int addedyear=2000;

        if(pastyears == true) {
	    addedyear=todayyear;
	}

	String[] yearst = new String[numberofyears];
        for(int i=0; i<numberofyears ; i++) {
	    if(pastyears == true) {
               yearst[i]= String.valueOf(addedyear);
               addedyear-=1;
	    }
	}

        year =	todaycalendar.get(Calendar.YEAR);
        
	yearbox= new JComboBox(yearst);
        yearbox.setSelectedIndex(0);
        yearbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        yearbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
		if (newSelection == null || newSelection.length() == 0) {
		    return;
		}
		
                year= Integer.parseInt(newSelection);
		calendar.set(Calendar.YEAR, year);
                dateChanged();
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;
	

	subPane.add(new JLabel("Year"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(yearbox, gbc);
  
	return;
    }

    private void monthOption(){
        month =	calendar.get(Calendar.MONTH);
        String[] monthstarray = {"January", "February", "March",
                              "April", "May", "June", "July",
                              "August", "September", "October",
                              "November", "December"};
       
	monthbox= new JComboBox(monthstarray);
        monthbox.setSelectedIndex(month);
        monthbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        monthbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                monthst = newSelection;
		calendar.set(Calendar.MONTH, setMonth(monthst));
		dateChanged();               
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;

	subPane.add(new JLabel("Month"), gbc);
        //gbc.gridx++;
	gbc.gridy++;
	subPane.add(monthbox, gbc);	
    
	return;
    }

    private void dayOption(){
	day = calendar.get(Calendar.DAY_OF_MONTH);
        String[] dayst = {"1","2","3","4","5","6","7","8","9","10",
                  "11","12","13","14","15","16","17","18","19","20",
	          "21","22","23","24","25","26","27","28","29","30","31"};
      
	daybox= new JComboBox(dayst);
        daybox.setSelectedIndex(--day);
	daybox.setAlignmentX(Component.LEFT_ALIGNMENT);
        daybox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                day = Integer.parseInt(newSelection);
                calendar.set(Calendar.DAY_OF_MONTH, day);
	        dateChanged();  
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;

	subPane.add(new JLabel("Day"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(daybox, gbc);

	return;
    }

  private void hourOption(){

        int numberofhours = 24;
	int addedhour=0;
	String[] hourst = new String[numberofhours];

        for(int i=0; i<numberofhours ; i++) {	   
            hourst[i]= String.valueOf(addedhour);
            addedhour++;	  
	}

	hourbox= new JComboBox(hourst);
        hourbox.setSelectedIndex(calendar.HOUR_OF_DAY);
        hourbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        hourbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                hour= Integer.parseInt(newSelection);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
                dateChanged();
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;
	

	subPane.add(new JLabel("Hour"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(hourbox, gbc);

	return;
    
    }

  private void minuteOption(){

        int numberofmins = 60;
	int addedmin=0;
	String[] minst = new String[numberofmins];

        for(int i=0; i<numberofmins ; i++) {	   
            minst[i]= String.valueOf(addedmin);
            addedmin++;	  
	}

	minbox= new JComboBox(minst);
        minbox.setSelectedIndex(calendar.MINUTE);
        minbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        minbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                min= Integer.parseInt(newSelection);
		calendar.set(Calendar.MINUTE, min);
                dateChanged();
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;
	

	subPane.add(new JLabel("Minute"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(minbox, gbc);

	return;
   
    }


  private void secondOption(){

        int numberofsecs = 60;
	int addedsec=0;
	String[] secst = new String[numberofsecs];

        for(int i=0; i<numberofsecs ; i++) {	   
            secst[i]= String.valueOf(addedsec);
            addedsec++;	  
	}

	secbox= new JComboBox(secst);
        secbox.setSelectedIndex(calendar.SECOND);
        secbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        secbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                sec= Integer.parseInt(newSelection);
		calendar.set(Calendar.SECOND, sec);
		dateChanged();
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;
	

	subPane.add(new JLabel("Second"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(secbox, gbc);

	return;
   

    }

   private void millisOption(){

        System.out.println("Millis option is not implemented.");
	return;

    }

    private void julianOption() {

        julianday = calendar.get(Calendar.DAY_OF_YEAR);
        julianyear = calendar.get(Calendar.YEAR);
        System.out.println("TODAY'S Julian Day is: " + julianday);

        Integer yearint = new Integer(julianyear);
        Integer dayint = new Integer(julianday);
	String yearst= yearint.toString();
	String dayst= dayint.toString();

         JTextField year = new JTextField(yearst);
         jday = new JTextField(dayst);

	jday.setAlignmentX(Component.LEFT_ALIGNMENT);

        jday.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                julianday = Integer.parseInt(jday.getText());
                calendar.set(Calendar.DAY_OF_YEAR, julianday);
                System.out.println("Selected Julian Day is: " + julianday);
                dateChanged(); 
             } 
        });


	//gbc.gridx = x_leftcorner;
	gbc.gridy = y_leftcorner;	
	gbc.gridx++;
	subPane.add(new JLabel("JulianDay"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(jday, gbc);       
   
	return;
    }


    private void dateFormatOption(){

	 String[] date = {"dd MMMMM yyyy 'at' hh:mm:ss z",
                 "dd.MM.yy", 
                 "MM/dd/yy",
                 "yyyy.MM.dd G 'at' hh:mm:ss z",
                 "EEE, MMM d, ''yy",
                 "h:mm a",
                 "H:mm:ss:SSS",
                 "K:mm a,z",
                 "yyyy.MMMMM.dd GGG hh:mm aaa"
                 };
	dateExamples=date; 

        currentDate = dateExamples[0];
     
        dateList = new JComboBox(dateExamples);
        dateList.setEditable(true);
        dateList.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                currentDate = newSelection;
                reformat();
            }
        });      
        // Set up the UI for selecting a date
        JLabel dateLabel1 = new JLabel("Select the format of the date.");
        JLabel dateLabel2 = new JLabel("");
        // Lay out everything
        JPanel datePanel = new JPanel();
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        datePanel.add(dateLabel1);
        datePanel.add(dateLabel2);
        datePanel.add(dateList);
        datePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(datePanel);
        add(Box.createRigidArea(new Dimension(0, 10)));

	reformat();

    }

    private void todayOption(){
      
        currentDate = "dd MMMMM yyyy 'at' hh:mm:ss z";        
 
       // Create the UI for displaying result
        JLabel resultLabel = new JLabel("Current Date/Time", JLabel.LEFT);
        result = new JLabel(" ");
        result.setForeground(Color.black);
        result.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(Color.black),
             BorderFactory.createEmptyBorder(5,5,5,5)
        ));
       
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new GridLayout(0, 1));
        resultPanel.add(resultLabel);
        resultPanel.add(result);
    
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        resultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(resultPanel);    
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 	reformat(currentDate);
        
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

	gbc.gridx = x_leftcorner;
	gbc.gridy = y_leftcorner;

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


	weekagobox= new JComboBox();
	for(int counter = 1; counter <= numberofweeks; counter++) {
	    
	    weekagobox.addItem( new String(new Integer(counter).toString()));

	}
        weekagobox.setSelectedIndex(0);
        weekagobox.setAlignmentX(Component.LEFT_ALIGNMENT);
        weekagobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
		System.out.println("The new value selected is "+newSelection);
                //min = Integer.parseInt(newSelection);
		//calendar.set(Calendar.MINUTE, min);
                //dateChanged();
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;
	

	subPane.add(new JLabel("Week  Ago"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(weekagobox, gbc);

	return;
    }
    /*
    private void minuteOption() {

	int numberofmins = 60;
	int addedmin=0;
	String[] minst = new String[numberofmins];

        for(int i=0; i<numberofmins ; i++) {	   
            minst[i]= String.valueOf(addedmin);
            addedmin++;	  
	}

	minbox= new JComboBox(minst);
        minbox.setSelectedIndex(calendar.MINUTE);
        minbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        minbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String newSelection = (String)cb.getSelectedItem();
                min= Integer.parseInt(newSelection);
		calendar.set(Calendar.MINUTE, min);
                //dateChanged();
            }
        });

	gbc.gridx = ++x_leftcorner;
	gbc.gridy = y_leftcorner;
	

	subPane.add(new JLabel("Minute"), gbc);
        //gbc.gridx++;
	gbc.gridy++;	
	subPane.add(minbox, gbc);

	return;
   
    }
    */

    private void monthagoOption() {





    }

    protected void createComponents() {
	 
         final Color bg = Color.darkGray;
         final Color fg = Color.black;
        

         //Initialize drawing colors, border, opacity.
         //subPane.setBackground(bg);
         //subPane.setForeground(fg);	
         /*subPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));         
  	*/ 
	 subPane.setLayout(new GridBagLayout());
	 gbc = new GridBagConstraints();
	 gbc.fill = gbc.HORIZONTAL;
	 gbc.weightx = 1.0;
	 gbc.weighty = 1.0;
	 gbc.gridx = x_leftcorner=0;
	 gbc.gridy = y_leftcorner=0;

        add(subPane);
        add(Box.createRigidArea(new Dimension(0, 10)));
       
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
 	
    }

    private void dateChanged(){

        int  todayyear = todaycalendar.get(Calendar.YEAR);   
        int  todaysday = todaycalendar.get(Calendar.DAY_OF_YEAR);   
        year = calendar.get(Calendar.YEAR);
        julianday = calendar.get(Calendar.DAY_OF_YEAR);


	//todayButton.updateUI();
	//yesButton.updateUI();
        otherButton.setSelected(true);

        if(year == todayyear){        
            yearbox.setSelectedIndex(0);
            if(todaysday == julianday){
		todayButton.setSelected(true);
	    }
            if(julianday ==  todaysday-1 ){
		yesButton.setSelected(true);
	    }
	} else {
            int indexofyear = todayyear - year;
            yearbox.setSelectedIndex(indexofyear);
	}

        month =	calendar.get(Calendar.MONTH);
	if(monthbox != null)
	    monthbox.setSelectedIndex(month);
	      
        day = calendar.get(Calendar.DAY_OF_MONTH);
	if(daybox != null)
	    daybox.setSelectedIndex(--day);

	
	hour = calendar.get(Calendar.HOUR_OF_DAY);
	if(hourbox != null)
	    hourbox.setSelectedIndex(hour);
	min = calendar.get(Calendar.MINUTE); 
	if(minbox != null)
	    minbox.setSelectedIndex(min);
	sec = calendar.get(Calendar.SECOND);
	if(secbox != null)
	    secbox.setSelectedIndex(sec);
	     
	//int monthtemp = month+1;
        //System.out.println("Date changed: "+monthtemp+"/"+day+"/"+year);
        repaint();
    
    }


    /** Formats and displays today's date. */
    public void reformat() {
        Date today = new Date();
        SimpleDateFormat formatter = 
           new SimpleDateFormat(currentDate);
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
    public void reformat(String newFormatofcurrentDate) {
        Date today = new Date();
        SimpleDateFormat formatter = 
           new SimpleDateFormat(newFormatofcurrentDate);
 
       try {           
            String dateString = formatter.format(today);
            result.setForeground(Color.black);
            result.setText(dateString);
        } catch (IllegalArgumentException iae) {
            result.setForeground(Color.red);
            result.setText("Error: " + iae.getMessage());
        }
    }

    public void setDate(Date newDate ){
    
        calendar.setTime(newDate);
      
	int julianyear = calendar.get(Calendar.YEAR);
        int julianday = calendar.get(Calendar.DAY_OF_YEAR);
	dateChanged();
        System.out.println("NewDate Julian Day is: " + julianday+"/"+julianyear);
    

   }

    private void setToday(){
        
        newDate = new Date(year, month, day);
        calendar.setTime(newDate);
        //calendar.set(year, month, day);

	int julianyear = calendar.get(Calendar.YEAR);
        int julianday = calendar.get(Calendar.DAY_OF_YEAR);
        System.out.println("TODAY Julian Day is: " + julianday+"/"+julianyear);
    

   }

    public Date getDate(){
   
       return calendar.getTime();

    }
	
    private int setMonth(String monthst){

	if(monthst.equals("January")) month=0;
	else if(monthst.equals("February")) month=1;
	else if(monthst.equals("March")) month=2;
	else if(monthst.equals("April")) month=3;
	else if(monthst.equals("May")) month=4;
	else if(monthst.equals("June")) month=5;
	else if(monthst.equals("July")) month=6;
	else if(monthst.equals("August")) month=7;
	else if(monthst.equals("September")) month=8;
	else if(monthst.equals("October")) month=9;
	else if(monthst.equals("November")) month=10;
	else if(monthst.equals("December")) month=11;
	else { System.out.println("Invalid Month.");
	       month=0;}
	return month;

    }

    protected JPanel subPane = new JPanel();
    private GridBagConstraints gbc;
    int x_leftcorner=0;
    int y_leftcorner=0;

    java.util.Date today = new  java.util.Date();
    Date newDate = new Date();

    Calendar calendar = new GregorianCalendar();
    Calendar todaycalendar = new GregorianCalendar();
    int julianyear, julianday;
    int year, month, day, hour, min, sec;

    JRadioButton todayButton = new JRadioButton("Today");
    JRadioButton yesButton = new JRadioButton("Yesterday"); 
    JRadioButton otherButton = new JRadioButton("Other"); 

   
    JComboBox yearbox;
    JComboBox monthbox;
    JComboBox daybox;
    JTextField jday;
    JComboBox hourbox;
    JComboBox minbox;
    JComboBox secbox;
    
    JComboBox weekagobox;

    JLabel result;
    String currentDate;
    String[] dateExamples;
    JComboBox dateList;

    String monthst;
    int juliandayint;

    int numberofyears=5;
    boolean pastyears=true;


    final Color bg = Color.darkGray;
    final Color fg = Color.blue;

} /* close class */
