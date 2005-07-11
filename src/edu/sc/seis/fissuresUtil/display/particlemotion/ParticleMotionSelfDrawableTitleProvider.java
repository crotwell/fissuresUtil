package edu.sc.seis.fissuresUtil.display.particlemotion;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;

/**
 * @author hedx Created on Jul 6, 2005
 */
public class ParticleMotionSelfDrawableTitleProvider extends MouseAdapter
        implements MouseMotionListener, SelfDrawableTitleProvider {

    public ParticleMotionSelfDrawableTitleProvider(TitleBorder border,
                                                   String title,
                                                   ParticleMotionDisplayDrawable drawable,
                                                   SeismogramDisplay parent) {
        this(border, title, DisplayUtils.DEFAULT_FONT, drawable, parent);
    }

    public ParticleMotionSelfDrawableTitleProvider(TitleBorder border,
                                                   String title,
                                                   Font f,
                                                   ParticleMotionDisplayDrawable drawable,
                                                   SeismogramDisplay parent) {
        setTitle(title);
        this.parent = parent;
        this.drawable = drawable;
        this.border = border;
        setTitleFont(f);
        this.border.addMouseListener(this);
        this.border.addMouseMotionListener(this);
    }

    public String getTitle() {
        return title;
    }

    public Font getTitleFont() {
        return font;
    }

    public void setTitleFont(Font f) {
        this.font = f;
    }

    public void setTitleColor(Color c) {
        this.titleColor = c;
    }

    public Color getTitleColor() {
        return titleColor;
    }

    public Rectangle2D getBounds(Graphics2D canvas) {
        FontMetrics fm = canvas.getFontMetrics();
        Rectangle2D stringBounds = fm.getStringBounds(title, canvas);
        double height = findMaxHeight(stringBounds.getHeight());
        double width = stringBounds.getWidth() + DEFAULT_LENGTH;
        Rectangle2D rec = new Rectangle();
        rec.setRect(0, 0, width, height);
        return rec;
    }

    public void draw(int x, int y, Graphics2D canvas) { //here
        xDrawMin = x;
        xDrawMax = x + DEFAULT_LENGTH;
        yDrawMin = y;
        yDrawMax = y + DEFAULT_LENGTH;
        if(visible && !SeismogramDisplay.PRINTING) {
            canvas.setColor(drawColorOfBigX);
            canvas.setStroke(DisplayUtils.THREE_PIXEL_STROKE);
            canvas.drawLine(xDrawMin, yDrawMin, xDrawMax, yDrawMax);
            canvas.drawLine(xDrawMin, yDrawMax, xDrawMax, yDrawMin);
            canvas.setStroke(DisplayUtils.ONE_PIXEL_STROKE);
            canvas.setColor(titleColor);
            canvas.drawString(title, xDrawMax + 5, yDrawMax + 2);
        }
    }

    public void setVisibility(boolean visible) {
        this.visible = visible;
    }

    public void mouseDragged(MouseEvent e) {
        if(intersects(e)) {
            setDrawColorOfBigX(Color.RED);
            repaint();
        } else {
            setDrawColorOfBigX(Color.BLACK);
            repaint();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if(intersects(e)) {
            ((ParticleMotionDisplay)parent).remove(drawable.getSeismogram());
            ((ParticleMotionDisplay)parent).removeTitle(drawable);
            repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
        if(intersects(e)) {
            setDrawColorOfBigX(Color.RED);
            repaint();
        } else {
            setDrawColorOfBigX(Color.BLACK);
            repaint();
        }
    }
    
    private void repaint(){
        border.repaint();
        
    }

    protected boolean intersects(MouseEvent e) {
        int clickX = e.getX();
        int clickY = e.getY();
        if(clickX >= horzXMin && clickX <= horzXMax && clickY >= horzYMin
                && clickY <= horzYMax) {
            return true;
        }
        if(clickX >= vertXMin && clickX <= vertXMax && clickY >= vertYMin
                && clickY <= vertYMax) {
            return true;
        }
        return false;
    }

    public void setVerticalCoordinates(double x, double y) {
        vertXMin = x;
        vertXMax = x + DEFAULT_LENGTH;
        vertYMin = y;
        vertYMax = y + DEFAULT_LENGTH;
    }

    public void setHorizontalCoordinates(double x, double y) {
        horzXMin = x;
        horzXMax = x + DEFAULT_LENGTH;
        horzYMin = y;
        horzYMax = y + DEFAULT_LENGTH;
    }

    private void setDrawColorOfBigX(Color newColor) {
        drawColorOfBigX = newColor;
    }

    public Color getDrawColorOfBigX() {
        return drawColorOfBigX;
    }

    private double findMaxHeight(double num) {
        if(num > DEFAULT_LENGTH) {
            return num;
        } else
            return DEFAULT_LENGTH;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private Color drawColorOfBigX = Color.BLACK;

    private Color titleColor;

    private int xDrawMax, xDrawMin, yDrawMax, yDrawMin;

    private double horzXMin, horzXMax, horzYMin, horzYMax;

    private double vertXMin, vertXMax, vertYMin, vertYMax;

    private boolean visible = true;

    private Font font = DisplayUtils.DEFAULT_FONT;

    private final int DEFAULT_LENGTH = 5;

    private String title;

    private int count = 0;

    private ParticleMotionDisplayDrawable drawable;

    private SeismogramDisplay parent;

    private TitleBorder border;
}
