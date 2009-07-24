package edu.sc.seis.fissuresUtil.display;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * SeismogramPrinter prints an array of BasicSeismogramDisplays
 *
 *
 * Created: Sun Oct 13 17:06:40 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class SeismogramPrinter implements Printable{


    /**
     * <code>print</code> instantiates a new SeismogramPrinter and uses it to print
     * the passed array of seismogram displays
     *
     * @param displays an array of BSD's to print
     */
    public static void print(BasicSeismogramDisplay[] displays){
        new SeismogramPrinter(displays);
    }

    private SeismogramPrinter(BasicSeismogramDisplay[] displays){
        this.displays = displays;
        if (displays.length == 0)
            return;

        // turn off global double buffering
        // SBH - efficiency code
        //        RepaintManager currentManager = null;
        //
        //        if (displays[0] instanceof JComponent) {
        //          currentManager = RepaintManager.currentManager((JComponent)displays[0]);
        //          currentManager.setDoubleBufferingEnabled(false);
        //        }

        //creates arrays for storing all of the displays initial conditions and stores them
        sizes = new Dimension[displays.length];
        bottomBorder = new boolean[displays.length];
        topBorder = new boolean[displays.length];
        for(int i = 0; i < displays.length; i++){
            displays[i].setDoubleBuffered(false);
            sizes[i] = displays[i].getSize();
            //bottomBorder[i] = displays[i].hasBottomBorder();
            //topBorder[i] = displays[i].hasTopBorder();
            //displays[i].addBottomBorder(new TimeBorder(displays[i], TimeBorder.BOTTOM));
            //displays[i].addTopBorder(new TimeBorder(displays[i], TimeBorder.TOP));
        }
        //handles the actual printing
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this);
        if(initialize() != CANCELLED && pj.printDialog()){
            try { pj.print(); }
            catch(Exception e){ e.printStackTrace(); }
        }
        //restores the displays back to their original conditions
        for(int i = 0; i < displays.length; i++){
            displays[i].setDoubleBuffered(true);
            if(!bottomBorder[i]){
                //displays[i].removeBottomBorder();
            }
            if(!topBorder[i]){
                //displays[i].removeTopBorder();
            }
            displays[i].setSize(sizes[i]);
            //displays[i].resize();
        }

        // SBH - efficiency code
        //        if (currentManager != null) {
        //          // turn global double buffering back on.
        //          currentManager.setDoubleBufferingEnabled(true);
        //        }
    }

    /**
     * Implementation of java.awt.print.Printable's <code>print</code> method.
     * Allows this object to be printed by java's print api
     * @param g the Graphics the current page prints to
     * @param pageFormat this page's format
     * @param pageIndex the current page index
     * @return an <code>int</code> specifying if this page exists
     */
    public int print(Graphics g, PageFormat pageFormat, int pageIndex){
        if(pageIndex >= pageCount)  return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D)g;
        g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        Dimension imageableSize = new Dimension();
        imageableSize.setSize(pageFormat.getImageableWidth(), pageFormat.getImageableHeight()/seisPerPage);
        for(int i = pageIndex * seisPerPage; i < displays.length && i < (pageIndex + 1) *  seisPerPage; i++){
            displays[i].setSize(imageableSize);
            //displays[i].resize();
            displays[i].paint(g2);
            g2.translate(0, pageFormat.getImageableHeight()/seisPerPage);
        }
        return PAGE_EXISTS;
    }

    /**
     *<code>initialize</code> pops up a dialog that gets the number of seismograms the user wants
     * on each page, sets the value, and sets the number of pages
     * @return the number of seismograms per page
     */
    private int initialize(){
        final int numOfSeis = displays.length;
        final JDialog dialog = new JDialog();
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.setTitle("Printing Options");
        dialog.setModal(true);
        JLabel information = new JLabel("Seismograms per page:  ");
        Integer[] numbers = new Integer[numOfSeis];//to initialize the combo box with correct values
        for(int i = 0; i < numOfSeis; i++){
            numbers[i] = new Integer(i + 1);
        }
        final JComboBox options = new JComboBox(numbers);
        options.setEditable(true);
        options.setMaximumSize(options.getMinimumSize());
        options.setPreferredSize(options.getMinimumSize());
        //this code makes the dialog advance if enter is hit in the combo box, but it
        //felt rather confusing.  Left it for my indecisiveness
        /*options.addActionListener(new ActionListener(){
         boolean selectedPreviously = false;
         public void actionPerformed(ActionEvent e){
         if(!selectedPreviously){
         int currentNumber = ((Integer)options.getSelectedItem()).intValue();
         if(currentNumber < 0){
         JOptionPane.showMessageDialog(null,
         "The number of seismograms selected must be greater than 0",
         "Selected Too Few",
         JOptionPane.WARNING_MESSAGE);
         }else if(currentNumber > numOfSeis){
         JOptionPane.showMessageDialog(null,
         "The number of seismograms selected must less than " + (numOfSeis + 1),
         "Selected Too Many",
         JOptionPane.WARNING_MESSAGE);
         }else{
         setSeisPerPage(currentNumber);
         dialog.dispose();
         }
         selectedPreviously = true;
         }else{
         selectedPreviously = false;
         }
         }
         });*/
        JButton next = new JButton("Next");
        next.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        int currentNumber = ((Integer)options.getSelectedItem()).intValue();
                        if(currentNumber < 0){
                            JOptionPane.showMessageDialog(null,
                                                          "The number of seismograms selected must be greater than 0",
                                                          "Selected Too Few",
                                                          JOptionPane.WARNING_MESSAGE);
                        }else if(currentNumber > numOfSeis){
                            JOptionPane.showMessageDialog(null,
                                                          "The number of seismograms selected must less than " + (numOfSeis + 1),
                                                          "Selected Too Many",
                                                          JOptionPane.WARNING_MESSAGE);
                        }else{
                            setSeisPerPage(currentNumber);
                            dialog.dispose();
                        }
                    }
                });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        setSeisPerPage(-1);
                        dialog.dispose();
                    }
                });
        JPanel north = new JPanel(new BorderLayout());
        north.setBorder(emptyBorder);
        north.add(information, BorderLayout.WEST);
        north.add(options, BorderLayout.EAST);
        JPanel south = new JPanel(new BorderLayout());
        south.setBorder(emptyBorder);
        south.add(cancel, BorderLayout.WEST);
        south.add(next, BorderLayout.EAST);
        dialog.getContentPane().add(north, BorderLayout.NORTH);
        dialog.getContentPane().add(south, BorderLayout.SOUTH);
        Toolkit tk = Toolkit.getDefaultToolkit();
        dialog.setLocation(tk.getScreenSize().width/2,
                           tk.getScreenSize().height/2);
        dialog.pack();
        dialog.show();
        pageCount = displays.length/seisPerPage + (displays.length%seisPerPage > 0 ? 1 : 0);
        return seisPerPage;
    }

    /** used by intiialize's anonymous classes to set the seisPerPage value
     */
    private void setSeisPerPage(int seisPerPage){ this.seisPerPage = seisPerPage; }

    private final static int CANCELLED = -1;

    private Dimension[] sizes;

    private boolean[] topBorder;

    private boolean[] bottomBorder;

    private BasicSeismogramDisplay[] displays;

    private int pageCount;

    private boolean initialized = false;

    private int seisPerPage = -1;

    private static EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);

}// SeismogramPrinter
