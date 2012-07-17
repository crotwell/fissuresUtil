package edu.sc.seis.fissuresUtil.display;

/**
 * SeismogramDisplayRemovalBorder.java
 *
 * @author Created by Charlie Groves
 */

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

import edu.sc.seis.fissuresUtil.display.drawable.BigX;
import edu.sc.seis.fissuresUtil.display.drawable.DisplayRemover;

public class SeismogramDisplayRemovalBorder extends AbstractBorder{

    public SeismogramDisplayRemovalBorder(SeismogramDisplay display) {
        top = 0;
        left = 0;
        right = 15;
        bottom = 0;
        this.display = display;
        remover = new DisplayRemover(display);
        remover.useInsets(false);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(top, left, bottom, right);
    }

    public Insets getBorderInsets(Component c, Insets i) {
        i.top = top;
        i.left = left;
        i.right = right;
        i.bottom = bottom;
        return new Insets(top, left, bottom, right);
    }

    public void paintBorder(Component c,
                            Graphics g,
                            int x,
                            int y,
                            int width,
                            int height) {
        remover.setXY(width - 10, 5);
        remover.draw((Graphics2D)g, null, null, null);
    }

    private int top, left, bottom, right;

    private SeismogramDisplay display;

    private BigX remover;
}

