package edu.sc.seis.fissuresUtil.display;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Univ of South Carolina</p>
 * @author unascribed
 * @version 1.0
 */

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import edu.sc.seis.fissuresUtil.display.*;
import edu.sc.seis.fissuresUtil.chooser.FileNameFilter;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class SeismogramPDFBuilder {
  LinkedList verticleDisplays = null;

  SeismogramDisplay display = null;

  // default number of images to print per page
  int imagesPerPage = 6;

  // an extra amount to inset entire printed page
  int extraInset = 50;

  // these numbers are wrt the scale layers.
  int leftMargin = 50;


  public SeismogramPDFBuilder(SeismogramDisplay disp) {
    display = disp;
  }

  public SeismogramPDFBuilder() {

  }

  public void createPDF() {
    if (display != null) {
      if (display instanceof VerticalSeismogramDisplay) {
        verticleDisplays = ((VerticalSeismogramDisplay)display).getDisplays();

        if (verticleDisplays == null)
          return;

        if (verticleDisplays.size() == 0) {
          return;
        }

      }

      init(verticleDisplays.size());
      String tmp = this.chooseOutputFile();
      if (tmp == null) {
        createPDF("default.pdf");
      }
      else
      {
        createPDF(tmp);
      }
    }
  }


  public void createPDF(String fileName) {

    try {
      FileOutputStream fos = new FileOutputStream(fileName);
      createPDF(fos);
    }
    catch (FileNotFoundException ex) {
      System.err.println("problem opening " + fileName);
      ex.printStackTrace();
    }

  }

  public void createPDF(FileOutputStream fos) {

    if (display != null) {
      if (display instanceof VerticalSeismogramDisplay) {
        verticleDisplays = ((VerticalSeismogramDisplay)display).getDisplays();

        if (verticleDisplays == null)
          return;

        if (verticleDisplays.size() == 0) {
          return;
        }

      }

      int totalPageHeight = 0;
      float zoomFactor = 4.0f;
      int topMargin = 50;
      int bottomMargin = 50;

      // this variable is used to scale the scales up and down.....
      // just a display resolution item.
      float scaleZoomFactor = 2.0f;

      // step 1: creation of a document-object
      Rectangle rect = PageSize.LETTER;
      Document document = new Document(rect);

      try {
        // step 2:
        // we create a writer that listens to the document
        // and directs a PDF-stream to a file
        PdfWriter writer = null;

        writer = PdfWriter.getInstance(document, fos);

        // step 3: we open the document
        document.open();

        // step 4: we grab the ContentByte and do some stuff with it

        // we create a fontMapper and read all the fonts in the font directory
        DefaultFontMapper mapper = new DefaultFontMapper();
        //          mapper.insertDirectory("c:\\winnt\\fonts");

        // we create a template and a Graphics2D object that corresponds with it

        int scaleW = (int)rect.width();
        int scaleH = (int)rect.height();

        float ys = 0;
        float xs = 0;

        int w = (int)( (float)scaleW * zoomFactor);
        int h = (int)( (float)scaleH * zoomFactor);

        int availableH = h;
        int widthForTraces = w;

        int heightForTraces = 0;

        PdfContentByte cb = writer.getDirectContent();

        // layer for drawing traces
        PdfTemplate tpTraces = cb.createTemplate(w, h);
        Graphics2D g2Traces = tpTraces.createGraphics(w, h);

        // layer for drawing Amplitude labels
        PdfTemplate tpScales = cb.createTemplate(scaleW*scaleZoomFactor,scaleH*scaleZoomFactor);
        Graphics2D g2Scales = tpScales.createGraphics(scaleW*scaleZoomFactor,scaleH*scaleZoomFactor);

        // layer for drawing time labels
        PdfTemplate frameScales = cb.createTemplate(scaleW*scaleZoomFactor,scaleH*scaleZoomFactor);
        Graphics2D g2FrameScales = frameScales.createGraphics(scaleW*scaleZoomFactor,scaleH*scaleZoomFactor);

        // labey for drawing last page bottom label
        PdfTemplate lastPageScales = cb.createTemplate(scaleW*scaleZoomFactor,scaleH*scaleZoomFactor);
        Graphics2D g2lastPageScales = lastPageScales.createGraphics(scaleW*scaleZoomFactor,scaleH*scaleZoomFactor);

        int imagesOnPageCount = 0;

        Iterator it = verticleDisplays.iterator();
        BasicSeismogramDisplay bsd = null;

        Dimension scaleSize = null;
        Dimension scaleScaleSize = null;
        Dimension timeScaleSize = null;

        boolean initedScaleSize = false;

        BasicSeismogramDisplay tbsd = (BasicSeismogramDisplay)verticleDisplays.getLast();

        while (it.hasNext()) {
          // loop over all traces

          bsd = (BasicSeismogramDisplay)(it.next());

          if (!initedScaleSize) {
            // first time through setup some things

            // get true margins
//            leftMargin = bsd.getAmplitudeLabelWidth();
            topMargin = bsd.getAmplitudeLabelHeight();

            // mover over around insets
            g2Traces.translate(extraInset*zoomFactor,extraInset*zoomFactor);
            g2Scales.translate((int)(extraInset*scaleZoomFactor), (int)(extraInset*scaleZoomFactor));

            // mover over around margins
            g2Traces.translate(leftMargin*zoomFactor,topMargin*zoomFactor);
            g2Scales.translate(0,(int)(topMargin*scaleZoomFactor));

            // calculate spacing
            availableH = availableH - (int)(zoomFactor * (topMargin+ bottomMargin + extraInset + extraInset));
            heightForTraces = availableH/(imagesPerPage);
            widthForTraces -= leftMargin*zoomFactor;
            widthForTraces -= 2*extraInset*zoomFactor;

            initedScaleSize = true;

            // draw to time scale layers.
            g2lastPageScales.translate(leftMargin*scaleZoomFactor,0);
            g2lastPageScales.translate(extraInset*scaleZoomFactor, extraInset*scaleZoomFactor);

            timeScaleSize = bsd.drawTopTimeBorders(g2lastPageScales, (int)(widthForTraces*scaleZoomFactor/zoomFactor),
                (int)(topMargin*scaleZoomFactor));

            g2FrameScales.translate(extraInset*scaleZoomFactor, extraInset*scaleZoomFactor);
            g2FrameScales.translate(leftMargin*scaleZoomFactor,0);
            timeScaleSize = bsd.drawTopTimeBorders(g2FrameScales, (int)(widthForTraces*scaleZoomFactor/zoomFactor),
                (int)(topMargin*scaleZoomFactor));

            g2FrameScales.translate(0,availableH*scaleZoomFactor/zoomFactor);

            int ttcount = verticleDisplays.size();
            int tracesLastPage = ttcount%imagesPerPage;

            g2lastPageScales.translate(0,(heightForTraces*tracesLastPage*scaleZoomFactor)/(zoomFactor));

            g2FrameScales.translate(0,topMargin);
            g2lastPageScales.translate(0,topMargin);

            timeScaleSize = tbsd.drawBottomTimeBorders(g2lastPageScales, (int)(widthForTraces*scaleZoomFactor/zoomFactor),
                (int)(topMargin*scaleZoomFactor));

            timeScaleSize = tbsd.drawBottomTimeBorders(g2FrameScales, (int)(widthForTraces*scaleZoomFactor/zoomFactor),
                (int)(topMargin*scaleZoomFactor));

            g2lastPageScales.dispose();
            g2FrameScales.dispose();

          }


          // draw amplitude scale
          scaleScaleSize = bsd.drawAmpBorders(g2Scales,(int)(leftMargin*scaleZoomFactor),
              (int)(heightForTraces*scaleZoomFactor/zoomFactor));

          // draw a box around this region.
          drawBox(g2Scales, (int)(scaleScaleSize.width), (int)(scaleScaleSize.height));

          bsd.drawSeismograms(g2Traces,new Dimension(widthForTraces,heightForTraces));

          drawBox(g2Traces, widthForTraces, heightForTraces);


          if (++imagesOnPageCount == imagesPerPage) {
            // page is full, finish page and create a new page.

            g2Traces.dispose();
            g2Scales.dispose();

            cb.addTemplate(tpTraces, 1.0f/zoomFactor, 0, 0, 1.0f/zoomFactor, 0, 0);

            xs = 1.0f/scaleZoomFactor;
            ys = 1.0f/scaleZoomFactor;

            cb.addTemplate(tpScales,xs,0,0, ys,0,0);

            cb.addTemplate(frameScales,xs,0,0, ys,0,0);

            // create a new page
            document.newPage();

            // create 2 new templates for drawing to.
            tpTraces = cb.createTemplate(w, h);
            g2Traces = tpTraces.createGraphics(w, h);

            tpScales = cb.createTemplate(scaleW*scaleZoomFactor, scaleH*scaleZoomFactor);
            g2Scales = tpScales.createGraphics(scaleW*scaleZoomFactor, scaleH*scaleZoomFactor);

            g2Traces.translate(extraInset*zoomFactor, extraInset*zoomFactor);
            g2Scales.translate(extraInset*scaleZoomFactor, extraInset*scaleZoomFactor);

            // translate over to avoid drawing new page in top left corner.
            g2Traces.translate(leftMargin*zoomFactor,topMargin*zoomFactor);
            g2Scales.translate(0,(int)(topMargin*scaleZoomFactor));


            // reset current count
            imagesOnPageCount = 0;

          } else {
            // step down the page
            g2Traces.translate(0,heightForTraces);
            g2Scales.translate(0,heightForTraces*scaleZoomFactor/zoomFactor);

          }
        }

        if (imagesOnPageCount != 0) {
          // if we put any images on this last page, make sure we draw them.

          // finish writing to the Graphics2D
          g2Traces.dispose();
          g2Scales.dispose();

          cb.addTemplate(tpTraces, 1.0f/zoomFactor, 0, 0, 1.0f/zoomFactor, 0, 0);


          xs = 1.0f/scaleZoomFactor;
          ys = 1.0f/scaleZoomFactor;


          cb.addTemplate(tpScales,xs,0,0, ys,0,0);

          // on the last page, add the partial page template instead.
          cb.addTemplate(lastPageScales,xs,0,0, ys,0,0);


          // cb.addTemplate(tpScales,1.0f, 0, 0, 1.0f, 0, 0);
        }
      }
      catch (DocumentException ex) {
      }


      // step 5: we close the document
      document.close();

    }

  }


  private void doMyDraw(Graphics2D g2, int offset, int width, int height, int count) {

    //    g2.drawString(new String(count + " - " + width +"," + height),0, offset + height);
    g2.drawRect(0,offset,width, offset + height);

  }

  private void drawBox(Graphics2D g2, int width, int height) {

    g2.drawRect(0,0,width,height);

  }



  /**
   *<code>initialize</code> pops up a dialog that gets the number of seismograms the user wants
   * on each page, sets the value, and sets the number of pages
   * @return the number of seismograms per page
   */
  private int init(int numSeis){
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
        //pageCount = displays.length/imagesPerPage + (displays.length%imagesPerPage > 0 ? 1 : 0);
        return imagesPerPage;
    }

    private static EmptyBorder emptyBorder = new EmptyBorder(5, 5, 5, 5);

    private void setSeisPerPage(int num) {

      imagesPerPage = num;

    }

    private String chooseOutputFile() {
      final JFileChooser fc = new JFileChooser();
      String extensions[] = new String[1];
      extensions[0] = "pdf";
      fc.setFileFilter(new FileNameFilter(extensions));

      fc.setDialogTitle("SAVE as PDF");
      fc.setSelectedFile(new File("output.pdf"));

      int returnVal = fc.showSaveDialog(null);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        String fileName = file.getAbsolutePath();

        return file.getAbsolutePath();
      } else {
        //log.append("Open command cancelled by user.");
      }
      return null;
    }


}