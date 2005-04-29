package edu.sc.seis.fissuresUtil.gmt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * @author oliverpa Created on Apr 28, 2005
 */
public class ImageAugmenter {

    public ImageAugmenter(String imgFileLoc) {
        this(imgFileLoc, false);
    }

    public ImageAugmenter(String imgFileLoc, boolean yFromBottom) {
        this.yFromBottom = yFromBottom;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = toolkit.getImage(imgFileLoc);
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
                                  BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        g.drawImage(img, 0, 0, null);
    }

    private void drawShape(Shape shape,
                           Paint fill,
                           Paint strokePaint,
                           int strokeWidth) {
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        g2d.setPaint(fill);
        g2d.fill(shape);
        g2d.setPaint(strokePaint);
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.draw(shape);
    }

    public void drawTriangle(int x,
                             int y,
                             int diameter,
                             Paint fill,
                             Paint strokePaint,
                             int strokeWidth) {
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
                           int strokeWidth) {
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

    public void outputToPNG(String filename) throws IOException {
        outputToPNG(new File(filename));
    }

    public void outputToPNG(File file) throws IOException {
        file.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(file.getName(),
                                        null,
                                        file.getParentFile());
        outputToPNG(new FileOutputStream(temp));
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

    public static int[] getTriangleCoords(int[] centerCoords,
                                          int staDiameter,
                                          int staStrokeWidth) {
        int[] triCoords = new int[6];
        double staRadius = staDiameter / 2 + staStrokeWidth;
        for(int i = 0; i < 3; i++) {
            double rads = i * TWO_THIRDS_PI - ONE_HALF_PI;
            triCoords[i * 2] = (int)(centerCoords[0] + staRadius
                    * Math.cos(rads));
            triCoords[i * 2 + 1] = (int)(centerCoords[1] + staRadius
                    * Math.sin(rads));
        }
        //System.out.println("triCoords: " + printIntArray(triCoords));
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
        ImageAugmenter imgAug = new ImageAugmenter(args[0], true);
        imgAug.drawCircle(300, 300, 20, new Color(0, 0, 0, 0), Color.RED, 2);
        imgAug.drawTriangle(350, 345, 10, new Color(0, 0, 255), Color.WHITE, 1);
        try {
            imgAug.outputToPNG(args[0] + "augmented.png");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static double TWO_THIRDS_PI = Math.PI * (2 / 3d);

    private static double ONE_HALF_PI = Math.PI * (1 / 2d);

    private boolean yFromBottom = false;

    private BufferedImage image;
}