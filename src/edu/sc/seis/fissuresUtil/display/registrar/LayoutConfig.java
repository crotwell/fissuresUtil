package edu.sc.seis.fissuresUtil.display.registrar;


public interface LayoutConfig extends DataSetSeismogramReceptacle{
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
}

