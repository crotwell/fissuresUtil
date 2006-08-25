package edu.sc.seis.fissuresUtil.display;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Univ of South Carolina</p>
 * @author unascribed
 * @version 1.0
 */

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import edu.sc.seis.fissuresUtil.display.borders.TimeBorder;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class SeismogramPDFBuilder {
    public static void createPDF(SeismogramDisplay disp, String fileName) throws FileNotFoundException {
        createPDF(disp, fileName, 1);
    }

    public static void createPDF(SeismogramDisplay disp, String fileName, boolean landscape) throws FileNotFoundException {
        createPDF(disp, fileName, 1, landscape);
    }

    public static  void createPDF(SeismogramDisplay disp, String fileName,int dispPerPage) throws FileNotFoundException {
        createPDF(disp, new File(fileName), dispPerPage);
    }

    public static  void createPDF(SeismogramDisplay disp, String fileName,int dispPerPage, boolean landscape) throws FileNotFoundException {
        createPDF(disp, new File(fileName), dispPerPage, landscape);
    }

    public static void createPDF(SeismogramDisplay disp, File f, int dispPerPage) throws FileNotFoundException{
        createPDF(disp, f, dispPerPage, false);
    }

    public static void createPDF(SeismogramDisplay disp, File f, int dispPerPage, boolean landscape) throws FileNotFoundException{
        if(f.getParentFile() != null){ f.getParentFile().mkdirs();}
        createPDF(disp, new FileOutputStream(f), dispPerPage, landscape);
    }
    public static void createPDF(SeismogramDisplay disp, OutputStream fos, int dispPerPage) {
        createPDF(disp, fos, dispPerPage, false);
    }

    public static void createPDF(SeismogramDisplay disp, OutputStream fos, int dispPerPage, boolean landscape) {
        List displays = new ArrayList();
        if(disp instanceof VerticalSeismogramDisplay){
            displays =  ((VerticalSeismogramDisplay)disp).getDisplays();
            Iterator it = displays.iterator();
            while(it.hasNext()){
                BasicSeismogramDisplay cur = (BasicSeismogramDisplay)it.next();
                cur.clear(BorderedDisplay.BOTTOM_CENTER);
                if(!cur.isFilled(BorderedDisplay.TOP_CENTER)){
                    cur.add(new TimeBorder(cur), BorderedDisplay.TOP_CENTER);
                }
            }
        }else{
            displays.add(disp);
        }
        Document document = new Document(PageSize.LETTER);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            int pageW = (int)PageSize.LETTER.width();
            int pageH = (int)PageSize.LETTER.height();
            // note H and W here refer to seismogram not paper, so for
            // landscape H and W are reversed in pixelsPerDisplay
            int pixelsPerDisplayH;
            int pixelsPerDisplayW;
            if (landscape) {
                pixelsPerDisplayH = (int)Math.floor((pageW - horizMargins)/(double)dispPerPage);
                pixelsPerDisplayW = (int)Math.floor((pageH - vertMargins));
            } else {
                pixelsPerDisplayH = (int)Math.floor((pageH - vertMargins)/(double)dispPerPage);
                pixelsPerDisplayW = (int)Math.floor(pageW - horizMargins);///(double)dispPerPage);
            }
            PdfContentByte cb = writer.getDirectContent();
            // layer for SeismogramDisplay
            PdfTemplate tpTraces = cb.createTemplate(pageW, pageH);
            Graphics2D g2Traces = tpTraces.createGraphics(pageW, pageH);
            if (landscape) {
                g2Traces.translate(pageW, 0);
                g2Traces.rotate(Math.toRadians(90));
            }
            g2Traces.translate(rightMargin, topMargin);
            int seisOnCurPage = 0;
            Iterator it = displays.iterator();
            while (it.hasNext()) {
                // loop over all traces
                SeismogramDisplay sd = (SeismogramDisplay)it.next();
                sd.renderToGraphics(g2Traces, new Dimension(pixelsPerDisplayW,
                                                            pixelsPerDisplayH));
                if (++seisOnCurPage == dispPerPage) {
                    // page is full, finish page and create a new page.
                    cb.addTemplate(tpTraces, 0, 0);
                    g2Traces.dispose();
                    document.newPage();
                    tpTraces = cb.createTemplate(pageW, pageH);
                    g2Traces = tpTraces.createGraphics(pageW, pageH);
                    if (landscape) {
                        g2Traces.translate(pageW, 0);
                        g2Traces.rotate(Math.toRadians(90));
                    }
                    g2Traces.translate(rightMargin, topMargin);
                    // reset current count
                    seisOnCurPage = 0;
                } else {
                    // step down the page
                    g2Traces.translate(0,pixelsPerDisplayH);
                }
            }
            if (seisOnCurPage != 0) {
                // finish writing to the Graphics2D
                cb.addTemplate(tpTraces, 0, 0);
                g2Traces.dispose();
            }
        }catch (DocumentException ex) {
            GlobalExceptionHandler.handle("problem saving to pdf", ex);
        }
        // step 5: we close the document
        document.close();
        if(disp instanceof VerticalSeismogramDisplay){
            ((VerticalSeismogramDisplay)disp).setBorders();
        }
    }

    private static int leftMargin = 50, rightMargin = 50,
        topMargin = 50, bottomMargin = 50;

    private static int horizMargins = leftMargin + rightMargin,
        vertMargins = topMargin + bottomMargin;
}
