package edu.sc.seis.fissuresUtil.display;

import javax.swing.JPanel;

public abstract class SeismogramDisplayProvider extends JPanel{
    public abstract SeismogramDisplay provide();
}

