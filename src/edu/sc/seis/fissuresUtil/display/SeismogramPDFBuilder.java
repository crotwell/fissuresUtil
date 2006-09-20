package edu.sc.seis.fissuresUtil.display;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: Univ of South Carolina
 * </p>
 * 
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import edu.sc.seis.fissuresUtil.display.borders.TimeBorder;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

public class SeismogramPDFBuilder {

    public static void createPDF(SeismogramDisplay disp, String fileName)
            throws FileNotFoundException {
        createPDF(disp, fileName, 1);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 String fileName,
                                 boolean landscape)
            throws FileNotFoundException {
        createPDF(disp, fileName, 1, landscape);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 String fileName,
                                 int dispPerPage) throws FileNotFoundException {
        createPDF(disp, new File(fileName), dispPerPage);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 String fileName,
                                 int dispPerPage,
                                 boolean landscape)
            throws FileNotFoundException {
        createPDF(disp, new File(fileName), dispPerPage, landscape);
    }

    public static void createPDF(SeismogramDisplay disp, File f, int dispPerPage)
            throws FileNotFoundException {
        createPDF(disp, f, dispPerPage, false);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 File f,
                                 int dispPerPage,
                                 boolean landscape)
            throws FileNotFoundException {
        if(f.getParentFile() != null) {
            f.getParentFile().mkdirs();
        }
        createPDF(disp, new FileOutputStream(f), dispPerPage, landscape);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 OutputStream out,
                                 int dispPerPage) {
        createPDF(disp, out, dispPerPage, false);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 OutputStream out,
                                 int dispPerPage,
                                 boolean landscape) {
        createPDF(disp, out, dispPerPage, landscape, true);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 OutputStream out,
                                 int dispPerPage,
                                 boolean landscape,
                                 boolean separateDisplays) {
        createPDF(disp, out, dispPerPage, landscape, separateDisplays, null);
    }

    public static void createPDF(SeismogramDisplay disp,
                                 OutputStream out,
                                 int dispPerPage,
                                 boolean landscape,
                                 boolean separateDisplays,
                                 TitleBorder header) {
        List displays = new ArrayList();
        if(separateDisplays && disp instanceof VerticalSeismogramDisplay) {
            displays.addAll(breakOutSeparateDisplays(disp));
        } else {
            displays.add(disp);
        }
        Rectangle pageSize = (landscape ? PageSize.LETTER.rotate()
                : PageSize.LETTER);
        Document document = new Document(pageSize);
        try {
            int headerHeight = 0;
            if(header != null) {
                header.setSize(header.getPreferredSize());
                headerHeight = header.getHeight();
                System.out.println("header height: " + headerHeight);
            }
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            int pageW = (int)pageSize.width();
            int pageH = (int)pageSize.height();
            int pixelsPerDisplayH = (int)Math.floor((pageH - vertMargins - headerHeight)
                    / (double)dispPerPage);
            int pixelsPerDisplayW = (int)Math.floor(pageW - horizMargins);
            PdfContentByte cb = writer.getDirectContent();
            // layer for SeismogramDisplay
            PdfTemplate tpTraces = cb.createTemplate(pageW, pageH);
            Graphics2D g2Traces = tpTraces.createGraphics(pageW, pageH);
            g2Traces.translate(rightMargin, topMargin);
            if(header != null) {
                header.setSize(new Dimension(pixelsPerDisplayW, headerHeight));
                boolean bufferingStatus = header.isDoubleBuffered();
                header.setDoubleBuffered(false);
                header.paint(g2Traces);
                header.setDoubleBuffered(bufferingStatus);
                g2Traces.translate(0, headerHeight);
            }
            int seisOnCurPage = 0;
            Iterator it = displays.iterator();
            while(it.hasNext()) {
                // loop over all traces
                SeismogramDisplay sd = (SeismogramDisplay)it.next();
                boolean bufferingStatus = sd.isDoubleBuffered();
                sd.setDoubleBuffered(false);
                sd.renderToGraphics(g2Traces, new Dimension(pixelsPerDisplayW,
                                                            pixelsPerDisplayH));
                sd.setDoubleBuffered(bufferingStatus);
                if(++seisOnCurPage == dispPerPage) {
                    // page is full, finish page and create a new page.
                    cb.addTemplate(tpTraces, 0, 0);
                    g2Traces.dispose();
                    document.newPage();
                    tpTraces = cb.createTemplate(pageW, pageH);
                    g2Traces = tpTraces.createGraphics(pageW, pageH);
                    g2Traces.translate(rightMargin, topMargin);
                    // reset current count
                    seisOnCurPage = 0;
                } else {
                    // step down the page
                    g2Traces.translate(0, pixelsPerDisplayH);
                }
            }
            if(seisOnCurPage != 0) {
                // finish writing to the Graphics2D
                cb.addTemplate(tpTraces, 0, 0);
                g2Traces.dispose();
            }
        } catch(DocumentException ex) {
            GlobalExceptionHandler.handle("problem saving to pdf", ex);
        }
        // step 5: we close the document
        document.close();
        if(disp instanceof VerticalSeismogramDisplay) {
            ((VerticalSeismogramDisplay)disp).setBorders();
        }
    }

    private static List breakOutSeparateDisplays(SeismogramDisplay disp) {
        List displays = ((VerticalSeismogramDisplay)disp).getDisplays();
        Iterator it = displays.iterator();
        while(it.hasNext()) {
            BasicSeismogramDisplay cur = (BasicSeismogramDisplay)it.next();
            cur.clear(BorderedDisplay.BOTTOM_CENTER);
            if(!cur.isFilled(BorderedDisplay.TOP_CENTER)) {
                cur.add(new TimeBorder(cur), BorderedDisplay.TOP_CENTER);
            }
        }
        return displays;
    }

    private static int leftMargin = 50, rightMargin = 50, topMargin = 50,
            bottomMargin = 50;

    private static int horizMargins = leftMargin + rightMargin,
            vertMargins = topMargin + bottomMargin;
}
