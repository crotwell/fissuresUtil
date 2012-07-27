package edu.sc.seis.fissuresUtil.display.registrar;


public interface LayoutConfig extends DataSetSeismogramReceptacle{

    public String getLabel();

    public void addListener(LayoutListener listener);

    public void removeListener(LayoutListener listener);

    /**
     * fires a layout event to all LayoutListeners with an event from
     * generateLayoutEvent()
     */
    public void fireLayoutEvent();

    /**
     * @returns a layout event based on the rule this config encapsulates and
     * the seismograms contained in its getSeismograms() method
     */
    public LayoutEvent generateLayoutEvent();

    public LayoutEvent getLayout();

    /**
     *sets the amount by which every seismogram in the layout is being scaled
     *@param scale - the factor by which the seismogram height is multiplied
     */
    public void setScale(double scale);

    public double getScale();
}

