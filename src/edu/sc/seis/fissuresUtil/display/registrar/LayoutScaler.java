/**
 * LayoutScaler.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.registrar;

import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LayoutScaler extends JSlider implements ChangeListener{
    public LayoutScaler(RecordSectionDisplay recSec){
        super(JSlider.VERTICAL, MIN_SCALE, MAX_SCALE, INITIAL_SCALE);
        setMajorTickSpacing(10);
        setPaintTicks(true);

        //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer(MIN_SCALE), new JLabel("Min"));
        labelTable.put(new Integer(MAX_SCALE), new JLabel("Max"));
        setLabelTable(labelTable);
        setPaintLabels(true);
        this.recSec = recSec;
        addChangeListener(this);
    }

    public void stateChanged(ChangeEvent ce){
        recSec.scalingChanged(getValue());
    }

    private RecordSectionDisplay recSec;

    public static final int MIN_SCALE = 10, MAX_SCALE = 100, INITIAL_SCALE = 45;
}

