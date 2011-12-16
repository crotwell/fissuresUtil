package edu.sc.seis.fissuresUtil.gmt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author oliverpa Created on Apr 28, 2005
 */
public class ImageAugmenter {

    public ImageAugmenter(String imgFileLoc) throws IOException {
        this(imgFileLoc, false);
    }

    public ImageAugmenter(String imgFileLoc, boolean yFromBottom)
            throws IOException {
        this.yFromBottom = yFromBottom;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = null;
        if(imgFileLoc.startsWith("jar:")) {
            img = toolkit.getImage(getClass().getClassLoader()
                    .getResource(imgFileLoc.substring(4)));
        } else {
            img = toolkit.getImage(imgFileLoc);
        }
        JPanel panel = new JPanel();
        MediaTracker tracker = new MediaTracker(panel);
        tracker.addImage(img, 0);
        try {
            tracker.waitForAll();
        } catch(InterruptedException e) {
            GlobalExceptionHandler.handle("problem occurred while waiting for image to load",
                                          e);
        }
        image = new BufferedImage(img.getWidth(null),
                                  img.getHeight(null),
                                  BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = image.createGraphics();
        setAntialiasingOn(g2d);
        g2d.drawImage(img, 0, 0, null);
    }

    public void drawShape(Shape shape,
                          Paint fill,
                          Paint strokePaint,
                          float strokeWidth) {
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        setAntialiasingOn(g2d);
        g2d.setPaint(fill);
        g2d.fill(shape);
        g2d.setPaint(strokePaint);
        Stroke stroke = new BasicStroke(strokeWidth);
        g2d.setStroke(stroke);
        g2d.draw(shape);
    }

    public void drawTriangle(int x,
                             int y,
                             int diameter,
                             Paint fill,
                             Paint strokePaint,
                             float strokeWidth) {
        int[] triCoords = getTriangleCoords(new int[] {x, translateY(y)},
                                            diameter,
                                            strokeWidth);
        Polygon poly = new Polygon(new int[] {triCoords[0],
                                              triCoords[2],
                                              triCoords[4]},
                                   new int[] {triCoords[1],
                                              triCoords[3],
                                              triCoords[5]},
                                   3);
        drawShape(poly, fill, strokePaint, strokeWidth);
    }

    public void drawCircle(int x,
                           int y,
                           int diameter,
                           Paint fill,
                           Paint strokePaint,
                           float strokeWidth) {
        int realX = translateCoord(x, diameter);
        int realY = translateCoord(translateY(y), diameter);
        Shape shape = new Arc2D.Double(realX,
                                       realY,
                                       diameter,
                                       diameter,
                                       0,
                                       360,
                                       Arc2D.OPEN);
        drawShape(shape, fill, strokePaint, strokeWidth);
    }

    public int translateCoord(int center, int length) {
        return center - (length / 2);
    }

    public int translateY(int y) {
        if(yFromBottom) {
            return image.getHeight() - y;
        } else {
            return y;
        }
    }

    public void cropImage(int newWidth, int newHeight, int left, int top) {
        BufferedImage oldImage = image;
        image = new BufferedImage(newWidth,
                                  newHeight,
                                  BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = image.createGraphics();
        setAntialiasingOn(g2d);
        g2d.drawImage(oldImage, null, -left, -top);
        oldImage = null;
    }

    public void outputToPNG(String filename) throws IOException {
        outputToPNG(new File(filename));
    }

    public void outputToPNG(File file) throws IOException {
        file.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(file.getName(),
                                        null,
                                        file.getParentFile());
        outputToPNG(new BufferedOutputStream(new FileOutputStream(temp)));
        file.delete();
        temp.renameTo(file);
    }

    public void outputToPNG(OutputStream out) throws IOException {
        ImageIO.write(image, "png", out);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public boolean getYFromBottom() {
        return yFromBottom;
    }

    public void setYFromBottom(boolean b) {
        yFromBottom = b;
    }
    
    public static void setAntialiasingOn(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static int[] getTriangleCoords(int[] centerCoords,
                                          int staDiameter,
                                          float staStrokeWidth) {
        int[] triCoords = new int[6];
        double staRadius = staDiameter / 2 + staStrokeWidth;
        for(int i = 0; i < 3; i++) {
            triCoords[i * 2] = (int)(centerCoords[0] + staRadius
                    * cos[i]);
            triCoords[i * 2 + 1] = (int)(centerCoords[1] + staRadius
                    * sin[i]);
        }
        return triCoords;
    }

    public static String printIntArray(int[] ints) {
        StringBuffer buf = new StringBuffer();
        buf.append("[ ");
        for(int i = 0; i < ints.length; i++) {
            buf.append(ints[i]);
            if(i < ints.length - 1) {
                buf.append(", ");
            }
        }
        buf.append(" ]");
        return buf.toString();
    }

    public static void main(String[] args) {
        try {
            ImageAugmenter imgAug = new ImageAugmenter(args[0], true);
            imgAug.drawCircle(300, 300, 5, Color.YELLOW, Color.BLACK, 1);
            imgAug.drawCircle(300,
                              300,
                              50,
                              new Color(0, 0, 0, 0),
                              Color.YELLOW,
                              2);
            imgAug.drawTriangle(350,
                                345,
                                10,
                                new Color(0, 0, 255),
                                Color.WHITE,
                                1);
            imgAug.cropImage(790, 426, 5, 169);
            imgAug.outputToPNG(args[1]);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static double TWO_THIRDS_PI = Math.PI * (2 / 3d);

    private static double ONE_HALF_PI = Math.PI * (1 / 2d);
    
    private static double[] cos;
    private static double[] sin;
    
    static {
        sin = new double[3];
        cos = new double[3];
        for(int i = 0; i < 3; i++) {
            double rads = i * TWO_THIRDS_PI - ONE_HALF_PI;
            cos[i] =  Math.cos(rads);
            sin[i] = Math.sin(rads);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(ImageAugmenter.class);

    private boolean yFromBottom = false;

    private BufferedImage image;
}
