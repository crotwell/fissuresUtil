package edu.sc.seis.fissuresUtil.display;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Univ of South Carolina</p>
 * @author unascribed
 * @version 1.0
 */

import javax.swing.*;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import edu.sc.seis.fissuresUtil.chooser.FileNameFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.border.EmptyBorder;

public class SeismogramPDFBuilder {
    public SeismogramPDFBuilder(SeismogramDisplay disp) {
        if (disp instanceof VerticalSeismogramDisplay) {
            displays = ((VerticalSeismogramDisplay)disp).getDisplays();
        }else displays.add(disp);
    }

    public void createPDF() {
        if (displays.size() == 0)  return;
        getImagesPerPage(displays.size());
        if(seisPerPageSet){
            String tmp = chooseOutputFile("pdf");
            if (tmp != null) {
                createPDF(tmp);
            }
        }
    }

    public void createPDF(String fileName) {
        try {
            createPDF(new FileOutputStream(fileName));
        }catch (FileNotFoundException ex) {
            int choice = JOptionPane.showConfirmDialog(null,
                                                       "Unable to open file " + fileName + ".  Would you like to try another location?",
                                                       "Unable to open file",
                                                       JOptionPane.WARNING_MESSAGE);
            if(choice == JOptionPane.YES_OPTION)
                createPDF();
        }
    }

    public void createPDF(FileOutputStream fos) {
        // step 1: creation of a document-object
        Document document = new Document(PageSize.LETTER);
        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();
            // we create a template and a Graphics2D object that corresponds with it
            int pageW = (int)PageSize.LETTER.width();
            int pageH = (int)PageSize.LETTER.height();
            PdfContentByte cb = writer.getDirectContent();
            // layer for SeismogramDisplay
            PdfTemplate tpTraces = cb.createTemplate(pageW, pageH);
            Graphics2D g2Traces = tpTraces.createGraphics(pageW, pageH);
            g2Traces.translate(rightMargin, topMargin);
            int imagesOnPageCount = 0;
            BasicSeismogramDisplay.PRINTING = true;
            Iterator it = displays.iterator();
            int pixelsPerDisplay = (int)((pageH - vertMargins)/(double)imagesPerPage);
            while (it.hasNext()) {
                // loop over all traces
                SeismogramDisplay sd = (SeismogramDisplay)it.next();
                sd.renderToGraphics(g2Traces, new Dimension((pageW - horizMargins),
                                                            pixelsPerDisplay));
                if (++imagesOnPageCount == imagesPerPage) {
                    // page is full, finish page and create a new page.
                    cb.addTemplate(tpTraces, 0, 0);
                    g2Traces.dispose();
                    tpTraces = cb.createTemplate(pageW, pageH);
                    g2Traces = tpTraces.createGraphics(pageW, pageH);
                    g2Traces.translate(rightMargin, topMargin);
                    // reset current count
                    imagesOnPageCount = 0;
                } else {
                    // step down the page
                    g2Traces.translate(0,pixelsPerDisplay);
                }
            }
            if (imagesOnPageCount != 0) {
                // finish writing to the Graphics2D
                cb.addTemplate(tpTraces, 0, 0);
                g2Traces.dispose();
            }
        }
        catch (DocumentException ex) {}
        // step 5: we close the document
        document.close();
        BasicSeismogramDisplay.PRINTING = false;
    }

    /**
     *<code>getImagesPerPage</code> pops up a dialog that gets the number of seismograms the user wants
     * on each page, sets the value, and sets the number of pages
     * @return the number of seismograms per page
     */
    private void getImagesPerPage(int numSeis){
        if(!seisPerPageSet){
            final int numOfSeis = numSeis;
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
            if(numOfSeis < imagesPerPage){
                imagesPerPage = numOfSeis;
            }
            options.setSelectedIndex(imagesPerPage-1);

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
                                                              "The number of seismograms selected can be at most " + numOfSeis,
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
                            dialog.dispose();
                        }
                    });
            JPanel north = new JPanel(new BorderLayout());
            north.setBorder(EMPTY_BORDER);
            north.add(information, BorderLayout.WEST);
            north.add(options, BorderLayout.EAST);
            JPanel south = new JPanel(new BorderLayout());
            south.setBorder(EMPTY_BORDER);
            south.add(cancel, BorderLayout.WEST);
            south.add(next, BorderLayout.EAST);
            dialog.getContentPane().add(north, BorderLayout.NORTH);
            dialog.getContentPane().add(south, BorderLayout.SOUTH);
            dialog.pack();
            Toolkit tk = Toolkit.getDefaultToolkit();
            dialog.setLocation((tk.getScreenSize().width - dialog.getWidth())/2,
                                   (tk.getScreenSize().height - dialog.getHeight())/2);
            dialog.show();
        }
    }

    private void setSeisPerPage(int num) {
        seisPerPageSet = true;
        imagesPerPage = num;
    }
    private String chooseOutputFile(String extension) {
        final JFileChooser fc;
        if (lastSaveLocation != null) {
            fc = new JFileChooser(lastSaveLocation);
        } else {
            fc = new JFileChooser();
        }
        String extensions[] = new String[1];
        extensions[0] = extension;
        fc.setFileFilter(new FileNameFilter(extensions));

        fc.setDialogTitle("Save to File");
        fc.setSelectedFile(new File("output."+extension));

        int returnVal = fc.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            lastSaveLocation = file.getParentFile();
            String newExtension =
                file.getName().substring(file.getName().lastIndexOf(".")+1);
            if ( ! newExtension.equalsIgnoreCase(extension)) {
                // add extension to end
                file = new File(file.getAbsolutePath()+"."+extension);
            }
            if (file.exists()) {
                String[] options = {"Yes", "No", "Choose again"};
                int n = JOptionPane.showOptionDialog(null,
                                                     "File "+file.getName()+" exists, replace?",
                                                     "File Exists",
                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     options,
                                                     options[0]);
                if (n == JOptionPane.NO_OPTION) {
                    return null;
                } else if (n == JOptionPane.CANCEL_OPTION) {
                    return chooseOutputFile(extension);
                }
                // ok to replace...
            }
            return file.getAbsolutePath();
        }
        return null;
    }

    private static File lastSaveLocation = null;

    private static EmptyBorder EMPTY_BORDER = new EmptyBorder(5, 5, 5, 5);

    private List displays = new ArrayList();

    //used to indicate if setSeisPerPage was successful
    private boolean seisPerPageSet = false;

    // default number of images to print per page
    private int imagesPerPage = 1;
    private int leftMargin = 50, rightMargin = 50,
        topMargin = 50, bottomMargin = 50;

    private int horizMargins = leftMargin + rightMargin,
        vertMargins = topMargin + bottomMargin;


}

