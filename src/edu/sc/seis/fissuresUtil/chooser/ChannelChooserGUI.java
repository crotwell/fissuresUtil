
package edu.sc.seis.fissuresUtil.chooser;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfPlottable.*;
import edu.iris.Fissures.display.*;
import edu.iris.Fissures.model.*;
import edu.iris.Fissures.utility.*;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.TauP.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;



/**
 * ChannelChooserGUI.java
 * 
 * Description: This class creates a list of networks and their respective stations and channels. A non-null NetworkDC reference must be supplied in the constructor, then use the get methods to obtain the necessary information that the user clicked on with the mouse. It takes care of action listeners and single click mouse button.
Use as the follwoing:
 * ChannelChooserGUI channelChooser = new ChannelChooserGUI(netDC);

 *
 * @author <a href="mailto:georginamc@prodigy.net">Georgina Coleman</a>
 * @version
 * Date: 12/04/2001
 */


public class ChannelChooserGUI extends JPanel{


    public ChannelChooserGUI(NetworkDC netdcgiven){

        mychannelchooser = new ChannelChooser(netdcgiven);	
        initFrame(); 
	addChannelListListener();
    }

    public void initFrame(){

      //Initialize drawing colors, border, opacity.
         //this.setBackground(bg);
         this.setForeground(fg);
	
         //subPane.setBackground(bg);
         subPane.setForeground(fg);	
         /*subPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder())); */        
         subPane.setSize(new java.awt.Dimension (mywidth, myheight));
	 subPane.setPreferredSize(new java.awt.Dimension (mywidth, myheight));
   
	 subPane.setLayout(new GridBagLayout());
	 gbc = new GridBagConstraints();
	 gbc.fill = gbc.BOTH;
	 gbc.weightx = 1.0;
	 gbc.weighty = 1.0;
	 gbc.gridx = 0;
	 gbc.gridy = 0;

	 JLabel netLabel = new JLabel("NETWORK   ");
	 JLabel staLabel = new JLabel("STATIONS   ");
	 JLabel siLabel = new JLabel("SITES   ");
	 JLabel chLabel = new JLabel("CHANNELS");
	 netLabel.setToolTipText(lnettip);
	 staLabel.setToolTipText(lstatip);
	 siLabel.setToolTipText(lsittip);
	 chLabel.setToolTipText(lchatip);

	 subPane.add(netLabel, gbc);
	 gbc.gridx++;
	 subPane.add(staLabel, gbc);
	 gbc.gridx++;
	 subPane.add(siLabel, gbc);
	 gbc.gridx++;
	 subPane.add(chLabel, gbc);
	 gbc.gridx++;
	 gbc.gridy++;

	 gbc.gridx = 0;

	 /*   Obtain list of networks available and display the 4 lists*/

         /*** findChannels Was replaced   ***/
	 //mychannelchooser.findChannels();

	 /* After server code is fixed do the following: */
	 /* setNetworks() then getNetworks(netcode) to obtain the array */

         mychannelchooser.setNetworks();
         networks = mychannelchooser.getNetworks();

         netlist = new JList(networks);
	 netlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scroller1 = new JScrollPane(netlist);
         subPane.add(scroller1, gbc);
	 gbc.gridx++;

         stalist = new JList(stations);
	 stalist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scroller2 = new JScrollPane(stalist);
         subPane.add(scroller2, gbc);
	 gbc.gridx++;
 
         sitlist = new JList(sites);
	 sitlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scroller3 = new JScrollPane(sitlist);
         subPane.add(scroller3, gbc);
	 gbc.gridx++;

         chalist = new JList(channels);
	 chalist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scroller4 = new JScrollPane(chalist);
         subPane.add(scroller4, gbc);
	 gbc.gridx++;

	 add(subPane);



	 /*
            String[] netarray = mychan.getNetworks();
            System.out.println("netarray: ");
		for(int ai=0; ai<netarray.length; ai++) {
		   System.out.print(netarray[ai]+" ");
		}
	 */



    }

     private void populateList(String[] items, JList list) {

	 final DefaultListModel model = new DefaultListModel();
	 for(int i=0; i<items.length; i++){
	     model.addElement(items[i]);
	 }

	 list.setModel(model);

	 if(list.isShowing()){
	     list.revalidate();
	 }

	 if(items.length==1){
	     list.setSelectedIndex(0);
	 }

	 model.addListDataListener(new ListDataListener() {
	     public void contentsChanged(ListDataEvent e) {
		 showStatus("contents changed");
	     }
             public void intervalRemoved(ListDataEvent e) {
		 java.lang.Object[] message = new java.lang.Object[] {
		     "Removed item at index" + e.getIndex0(),
		     "",
		     "There are now" + model.getSize() + "items"
		 };

		 JOptionPane.showMessageDialog(ChannelChooserGUI.this, message, "Items Removed", 
 	                                      JOptionPane.INFORMATION_MESSAGE);//type

	     }

             public void intervalAdded(ListDataEvent e) {
		 showStatus("contents added");
	     }

	 });
        
   
    }

   public void popStations(String networkchosen){
       mychannelchooser.setStations(networkchosen);
       populateList(mychannelchooser.getStations(), stalist);
    }

   public void popSites(String networkchosen, String station){
       mychannelchooser.setChannels(networkchosen, station);   
       populateList(mychannelchooser.getSites(), sitlist);
      
    }

   public void popChannels(String networkchosen, String station){
       mychannelchooser.setChannels(networkchosen, station);   
       populateList(mychannelchooser.getChannels(), chalist);
    }


    public void showStatus(String printtoscreen){
	//	System.out.println(printtoscreen);
    }


     protected void createComponents() {

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.awt.Dimension screen = toolkit.getScreenSize();
        setLocation(screen.width/5 , screen.height/5 );
 
    }

    protected void addChannelListListener(){


      /* Populates the station list according to network selected */
       netlist.addListSelectionListener(
                               new javax.swing.event.ListSelectionListener() {
           public void valueChanged(javax.swing.event.ListSelectionEvent e) {

           try {
 
               String s = ("Netlist: " );
	       if(e.getValueIsAdjusting()){
		   s="\nadjusting...";
	       } else{
		   s= "selection from" + e.getFirstIndex() +
		       "to" + e.getLastIndex();
		   String selected = getNet();
		   clearThreeLists();
		   popStations(selected);
		   //System.out.println("*** User made a selection:" +selected);

	       }

               //System.out.println(s);

              
            }catch (Exception exception) {
               System.out.println(exception);
            }

           } /*close valueChanged */
      }); /*close addList... */

      /* Populates the site, channel lists according to station selected */
      stalist.addListSelectionListener(
                               new javax.swing.event.ListSelectionListener() {
           public void valueChanged(javax.swing.event.ListSelectionEvent e) {

           try {
 
               String s;;
	       if(e.getValueIsAdjusting()){
		   s="\nadjusting...";
	       } else{
		   s= "selection from" + e.getFirstIndex() +
		       "to" + e.getLastIndex();
		   String selected = getStation();
		   popSites(getNet(), selected);
		   popChannels(getNet(), selected);
		   System.out.println("*** User made a selection:" +selected);

	       }

               //System.out.println(s);

              
            }catch (Exception exception) {
               System.out.println(exception);
            }

           } /*close valueChanged */
      }); /*close addList... */


      /* User selects channel and we obtain all query information */
      chalist.addListSelectionListener(
                               new javax.swing.event.ListSelectionListener() {
           public void valueChanged(javax.swing.event.ListSelectionEvent e) {

           try {
 
               String s;;
	       if(e.getValueIsAdjusting()){
		   s="\nadjusting...";
	       } else{
		   s= "selection from" + e.getFirstIndex() +
		       "to" + e.getLastIndex();
		   String mynet= getNet();
		   String mysta= getStation();
		   String mysit= getSite();
		   String mycha= getChannel();

		   System.out.println("*** User finished a selection:\t");
                   System.out.print(mynet + ":"+ mysta+ ":"+mysit+ ":"+mycha);
		  
	       }
	  
               //System.out.println(s);

              
            }catch (Exception exception) {
               System.out.println(exception);
            }

           } /*close valueChanged */
      }); /*close addList... */


	   // clearFourLists();

    }
 

    public String  getNet(){
	String netchosen = (String)netlist.getSelectedValue();
	return netchosen;
    }         

    public String  getStation(){
        String stationchosen = (String)stalist.getSelectedValue();
	return stationchosen;
    }  
   
    public String  getChannel(){
	String channelchosen = (String)chalist.getSelectedValue();
	return channelchosen;
     }     
 
    public String  getSite(){      
	String sitechosen = (String)sitlist.getSelectedValue();
	return sitechosen;
    }  

  
    /**
	 This function returns the ChannelId corresponding to the selected
	 networkName, stationName, siteName, channelName
     **/

    public ChannelId newgetChannelId() {
	//(String networkName, String stationName, String siteName, String channelName) {
	edu.iris.Fissures.Time dummyTime = 
	    new edu.iris.Fissures.Time("19990101T000000.0Z", -1);
	NetworkId netId = 
            new NetworkId(getNet(),
                          dummyTime);
	if(netId != null && getStation()!= null &&
	   getSite()!= null && getChannel()!= null) {

           ChannelId chanId = 
	        new ChannelId(netId,
                          getStation(),
                          getSite(),
                          getChannel(),
                          dummyTime);
           return chanId;
	} else
	    return null;

	
	
    }

    public void  clearThreeLists(){ 

	stalist.clearSelection(); 
	sitlist.clearSelection(); 
	chalist.clearSelection(); 

    }

     public ChannelId getChannelId() {
	String keyStr = new String();
	keyStr = getNet() + "." + getStation() + "." + getSite() + "." + getChannel();
	System.out.println("The key is "+keyStr);
        return mychannelchooser.getChannelId(keyStr);
    }
 


   /*================Class Variables===============*/

    ChannelChooser mychannelchooser;

    protected JPanel contentPane = new JPanel();  
 
    String lnettip = "Source of data";
    String lstatip = "Station";
    String lsittip = "Seismometer site";
    String lchatip = "Seismometer channels"; 
    String gotip = "Searches and retrieves a seismogram";
    String closetip = "Hide this window";
    String thistip = "Select date and location to obtain seismogram";
    String bhztip = "B=Broad Band | H=High Gain Seismometer | Z=Vertical"; 
    String bhetip = "B=Broad Band | H=High Gain Seismometer | E=East-West";
    String bhntip = "B=Broad Band | H=High Gain Seismometer | N=North-South";
 

    /*================Class Variables===============*/


    protected String[] networks;
    protected String[] stations = {"    "};
    protected String[] sites={"    "}; 
    protected String[] channels={"    "};

    protected JList netlist;
    protected JList stalist;
    protected JList sitlist;
    protected JList chalist;


    protected HashMap chanMap = new HashMap();
    
    protected static org.omg.CORBA_2_3.ORB orb;
    private NetworkDC netdc;
    private PlottableDC plottableRef;

    String defaultnameoffile;

    protected JPanel subPane = new JPanel();
    private GridBagConstraints gbc;
    int x_leftcorner=0;
    int y_leftcorner=0;

    final Color bg = Color.white;
    final Color fg = Color.blue;

    int mywidth = 400;
    int myheight = 200;



} // ChannelGUI



