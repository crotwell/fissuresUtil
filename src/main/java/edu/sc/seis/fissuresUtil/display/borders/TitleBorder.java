package edu.sc.seis.fissuresUtil.display.borders;


/**
 * @author groves Created on Feb 15, 2005
 */
public class TitleBorder extends NoTickBorder {

    public TitleBorder(int side, int order) {
        super(side, order);
    }

    public TitleBorder(int side, int order, String title) {
        this(side, order);
        add(new UnchangingTitleProvider(title));
    }

    protected BorderFormat createNoTickFormat() {
        return new TitleBorderFormat();
    }

    private class TitleBorderFormat extends BorderFormat {

        public TitleBorderFormat() {
            super(0, 0);
        }

        public String getMaxString() {
            return null;
        }

        public String getLabel(double value) {
            return null;
        }
    }
}