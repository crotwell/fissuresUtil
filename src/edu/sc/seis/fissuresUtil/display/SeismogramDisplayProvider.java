package edu.sc.seis.fissuresUtil.display;

import javax.swing.JComponent;

public abstract class SeismogramDisplayProvider extends JComponent{
    public abstract SeismogramDisplay provide();
}

