package edu.sc.seis.fissuresUtil.display;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

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

    public SeismogramPDFBuilder() {
        this(true, 1, true);
    }

    public SeismogramPDFBuilder(boolean landscape,
                                int dispPerPage,
                                boolean separateDisplays) {
        this(landscape, PAGE_SIZE, MARGIN, dispPerPage, separateDisplays);
    }

    public SeismogramPDFBuilder(boolean landscape,
                                Rectangle pageSize,
                                int margin,
                                int dispPerPage,
                                boolean separateDisplays) {
        this(landscape,
             pageSize,
             margin,
             margin,
             margin,
             margin,
             dispPerPage,
             separateDisplays);
    }

    public SeismogramPDFBuilder(boolean landscape,
                                Rectangle pageSize,
                                int topMargin,
                                int rightMargin,
                                int bottomMargin,
                                int leftMargin,
                                int dispPerPage,
                                boolean separateDisplays) {
        setPageSize(landscape ? pageSize.rotate() : pageSize);
        setMargins(topMargin, rightMargin, bottomMargin, leftMargin);
        setDispPerPage(dispPerPage);
    }

    public void setPageSize(Rectangle pageSize) {
        this.pageSize = pageSize;
    }

    public Rectangle getPageSize() {
        return pageSize;
    }

    public void setMargins(int margin) {
        setMargins(margin, margin, margin, margin);
    }

    public void setMargins(int topMargin,
                           int rightMargin,
                           int bottomMargin,
                           int leftMargin) {
        setTopMargin(topMargin);
        setRightMargin(rightMargin);
        setBottomMargin(bottomMargin);
        setLeftMargin(leftMargin);
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
        recalculateVertMargins();
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setRightMargin(int rightMargin) {
        this.rightMargin = rightMargin;
        recalculateHorizMargins();
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public void setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
        recalculateVertMargins();
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
        recalculateHorizMargins();
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setDispPerPage(int dispPerPage) {
        this.dispPerPage = dispPerPage;
    }

    public int getDispPerPage() {
        return dispPerPage;
    }

    public void setSeparateDisplays(boolean separateDisplays) {
        this.separateDisplays = separateDisplays;
    }

    public boolean getSeparateDisplays() {
        return separateDisplays;
    }

    public void setHeader(TitleBorder header) {
        this.header = header;
    }

    public TitleBorder getHeader() {
        return header;
    }

    public Dimension getPrintableSize() {
        return new Dimension((int)(pageSize.getWidth() - leftMargin - rightMargin),
                             (int)(pageSize.getHeight() - topMargin - bottomMargin));
    }

    public void createPDF(JComponent disp, File file) throws IOException {
        file.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(file.getName(),
                                        null,
                                        file.getParentFile());
        createPDF(disp, new FileOutputStream(temp));
        file.delete();
        temp.renameTo(file);
    }

    public void createPDF(JComponent comp, OutputStream out) {
        List displays = new ArrayList();
        if(separateDisplays && comp instanceof VerticalSeismogramDisplay) {
            displays.addAll(breakOutSeparateDisplays((VerticalSeismogramDisplay)comp));
        } else {
            displays.add(comp);
        }
        createPDF((JComponent[])displays.toArray(new JComponent[0]), out);
    }

    public void createPDF(JComponent[] comps, OutputStream out) {
        Document document = new Document(pageSize);
        try {
            int headerHeight = 0;
            if(header != null) {
                header.setSize(header.getPreferredSize());
                headerHeight = header.getHeight();
            }
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();
            int pageW = (int)pageSize.getWidth();
            int pageH = (int)pageSize.getHeight();
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
            for(int i = 0; i < comps.length; i++) {
                // loop over all traces
                boolean bufferingStatus = comps[i].isDoubleBuffered();
                comps[i].setDoubleBuffered(false);
                if(comps[i] instanceof Graphics2DRenderer) {
                    ((Graphics2DRenderer)comps[i]).renderToGraphics(g2Traces,
                                                                    new Dimension(pixelsPerDisplayW,
                                                                                  pixelsPerDisplayH));
                } else {
                    comps[i].paint(g2Traces);
                }
                comps[i].setDoubleBuffered(bufferingStatus);
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
            }
            g2Traces.dispose();
        } catch(DocumentException ex) {
            GlobalExceptionHandler.handle("problem saving to pdf", ex);
        }
        // step 5: we close the document
        document.close();
    }

    private List breakOutSeparateDisplays(VerticalSeismogramDisplay disp) {
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

    private void recalculateHorizMargins() {
        horizMargins = leftMargin + rightMargin;
    }

    private void recalculateVertMargins() {
        vertMargins = topMargin + bottomMargin;
    }

    private int topMargin, rightMargin, bottomMargin, leftMargin;

    private int horizMargins, vertMargins;

    private Rectangle pageSize;

    private int dispPerPage;

    private boolean separateDisplays;

    private TitleBorder header;

    public static final Rectangle PAGE_SIZE = PageSize.LETTER;

    public static final int MARGIN = 50;
}
