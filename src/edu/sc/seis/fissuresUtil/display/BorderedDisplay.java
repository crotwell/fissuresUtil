package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.borders.Border;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BorderedDisplay extends JPanel{
    public BorderedDisplay(){ this.setLayout(new GridBagLayout()); }

    public BorderedDisplay(JComponent centerPanel){
        this();
        this.center = centerPanel;
    }

    public JComponent getCenter(){ return center; }

    public void setCenter(JComponent center){ add(center, CENTER); }

    public void outputToPNG(String filename) throws IOException{
        if(getRootPane() == null){
            JFrame frame = new JFrame();
            frame.getContentPane().add(this);
            frame.pack();
        }
        Dimension size = getSize();
        BufferedImage bImg = new BufferedImage(size.width, size.height,
                                               BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bImg.createGraphics();
        print(g2d);
        File loc = new File(filename);
        loc.getCanonicalFile().getParentFile().mkdirs();
        File temp = File.createTempFile(loc.getName(), null);
        ImageIO.write(bImg, "png", temp);
        loc.delete();
        temp.renameTo(loc);
    }

    protected void add(JComponent comp, int position){
        if(position == CENTER)center = comp;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;//Fill all panels in both directions
        gbc.gridx = position%3;
        gbc.gridy = position/3;
        System.out.println("adding " + comp.getClass().getName() + " to " + position + " x: " + gbc.gridx + " y: " + gbc.gridy);
        if(gbc.gridx == 1) gbc.weightx = 1;//All row 1 components have a x
        else gbc.weightx = 0;//weight of 1
        if(gbc.gridy == 1) gbc.weighty = 1;// All column 1 components have y
        else gbc.weighty = 0;//weight of 1
        super.add(comp, gbc);
    }

    public void addBorder(Border border, int position) {
        add(border, position);
        borders[position] = border;
    }

    public void addTitle(String title, int position){

    }

    public void clear(int position){
        if(position == CENTER){
            remove(center);
            center = null;
        }else if(isFilled(position)){
            remove(borders[position]);
            borders[position] = null;
        }
    }

    public void removeAll(){
        for (int i = 0; i < borders.length; i++) { clear(i); }
        super.removeAll();
    }

    public boolean isFilled(int position){ return borders[position] != null; }


    /**
     * The positions are of the form ROW_COLUMN
     */
    public static final int TOP_LEFT = 0, TOP_CENTER = 1, TOP_RIGHT = 2,
        CENTER_LEFT = 3, CENTER_RIGHT = 5, BOTTOM_LEFT = 6, BOTTOM_CENTER = 7,
        BOTTOM_RIGHT = 8;

    protected static final int CENTER = 4;

    private JComponent center;

    private Border[] borders = new Border[9];
}

