/**
 * LayoutScaler.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display.registrar;

import edu.sc.seis.fissuresUtil.display.RecordSectionDisplay;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LayoutScaler extends JSlider implements ChangeListener{
    public LayoutScaler(RecordSectionDisplay recSec){
        super(JSlider.VERTICAL);
        setMinPoint();
        setValue(INITIAL_SCALE);
        setPaintLabels(true);
        setPaintTicks(true);
        this.recSec = recSec;
        addChangeListener(this);
        recSec.setLayoutScaler(this);
    }
    
    public void stateChanged(ChangeEvent ce){
        if(!changingValues){
            recSec.scalingChanged(getValue());
        }
    }
    
    public void setMinPoint(){
        setMinPoint(MIN_SCALE, INITIAL_SCALE);
    }
    
    private boolean changingValues = false;
    
    public void setMinPoint(int minpoint, int curValue){
        changingValues = true;
        setMinimum(minpoint);
        int maxpoint = minpoint + minpoint*10;
        setMaximum(maxpoint);
        setValue(curValue);
        changingValues = false;
        setMajorTickSpacing((maxpoint - minpoint)/10);
        //Create the label table
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer(minpoint), new JLabel("Min"));
        labelTable.put(new Integer(maxpoint), new JLabel("Max"));
        setLabelTable(labelTable);
    }
    
    private RecordSectionDisplay recSec;
    
    public static final int MIN_SCALE = 10, INITIAL_SCALE = 45;
}

