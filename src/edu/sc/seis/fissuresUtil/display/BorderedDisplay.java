package edu.sc.seis.fissuresUtil.display;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class BorderedDisplay extends JPanel {

    public BorderedDisplay() {
        this.setLayout(new GridBagLayout());
    }

    public BorderedDisplay(JComponent centerPanel) {
        this();
        add(centerPanel, CENTER);
    }

    public JComponent get(int position) {
        return comps[position];
    }

    public void outputToPNG(String filename) throws IOException {
        outputToPNG(new File(filename));
    }

    public void outputToPNG(File f) throws IOException {
        outputToPNG(f, getPreferredSize());
    }

    public void outputToPNG(File loc, Dimension size) throws IOException {
        loc.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(loc.getName(),
                                        null,
                                        loc.getParentFile());
        outputToPNG(new BufferedOutputStream(new FileOutputStream(temp)), size);
        loc.delete();
        temp.renameTo(loc);
    }

    public void outputToPNG(OutputStream loc, Dimension size)
            throws IOException {
        BufferedImage bImg = new BufferedImage(size.width,
                                               size.height,
                                               BufferedImage.TYPE_INT_RGB);
        renderToGraphics(bImg.createGraphics(), size);
        ImageIO.write(bImg, "png", loc);
        loc.close();
    }

    public void renderToGraphics(Graphics2D g) {
        renderToGraphics(g, getPreferredSize());
    }

    public void renderToGraphics(Graphics2D g, Dimension size) {
        //addNotify tells the java component it's ok to lay itself out.
        //In the case where this isn't being drawn to screen and is not in a an
        //AWT component, ie JFrame, this needs to be done so you don't get a
        // gray box
        if(getRootPane() == null) {
            addNotify();
        }
        Dimension curSize = getSize();
        setSize(size);
        validate();
        print(g);
        setSize(curSize);
    }

    public Component add(Component comp) {
        throw new UnsupportedOperationException("Use add(JComponent, int) instead");
    }

    public void add(Component comp, Object obj) {
        throw new UnsupportedOperationException("Use add(JComponent, int) instead");
    }

    public Component add(Component comp, int position) {
        throw new UnsupportedOperationException("Use add(JComponent, int) instead");
    }

    /**
     * Adds the given component in the passed in position. The position must be
     * one of the nine position ints defined in this class
     */
    public void add(JComponent comp, int position) {
        clear(position);
        comps[position] = comp;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;//Fill all panels in both directions
        gbc.gridx = position % 3;
        gbc.gridy = position / 3;
        if(gbc.gridx == 1) gbc.weightx = 1;//All row 1 components have a x
        else gbc.weightx = 0;//weight of 1
        if(gbc.gridy == 1) gbc.weighty = 1;// All column 1 components have y
        else gbc.weighty = 0;//weight of 1
        super.add(comp, gbc);
    }

    public void clearBorders() {
        for(int i = 0; i < comps.length; i++) {
            if(i != CENTER) {
                clear(i);
            }
        }
    }

    public void clear(int position) {
        if(isFilled(position)) {
            remove(comps[position]);
            comps[position] = null;
        }
    }

    public void removeAll() {
        for(int i = 0; i < comps.length; i++) {
            clear(i);
        }
        super.removeAll();
    }

    public boolean isFilled(int position) {
        return comps[position] != null;
    }

    /**
     * The positions are of the form ROW_COLUMN
     */
    public static final int TOP_LEFT = 0, TOP_CENTER = 1, TOP_RIGHT = 2,
            CENTER_LEFT = 3, CENTER = 4, CENTER_RIGHT = 5, BOTTOM_LEFT = 6,
            BOTTOM_CENTER = 7, BOTTOM_RIGHT = 8;

    private JComponent[] comps = new JComponent[9];
}