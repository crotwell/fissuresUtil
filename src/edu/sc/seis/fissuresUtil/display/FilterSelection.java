package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.freq.ColoredFilter;
import edu.sc.seis.fissuresUtil.freq.SeisGramText;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.apache.log4j.Logger;

/**
 * FilterSelection.java
 *
 *
 * Created: Wed Jun 26 20:36:05 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version
 */

public class FilterSelection extends JPanel{

    public FilterSelection(VerticalSeismogramDisplay listener){
        original.setMargin(new Insets(0,0,0,0));
        teleseismic.setMargin(new Insets(0,0,0,0));
        smoothTeleseismic.setMargin(new Insets(0,0,0,0));
        vSmoothTeleseismic.setMargin(new Insets(0,0,0,0));
        regional.setMargin(new Insets(0,0,0,0));
        local.setMargin(new Insets(0,0,0,0));
        this.add(original);
        this.add(teleseismic);
        this.add(smoothTeleseismic);
        this.add(vSmoothTeleseismic);
        this.add(regional);
        this.add(local);
        original.addItemListener(filterCheck);
        teleseismic.addItemListener(filterCheck);
        smoothTeleseismic.addItemListener(filterCheck);
        vSmoothTeleseismic.addItemListener(filterCheck);
        regional.addItemListener(filterCheck);
        local.addItemListener(filterCheck);
        this.listener = listener;
    }

    protected LinkedList getCurrentFilters(){ return currentFilters; }

    protected JCheckBox original = new JCheckBox("Original", true);

    protected JCheckBox teleseismic = new JCheckBox("Teleseismic");

    protected JCheckBox smoothTeleseismic = new JCheckBox("Smooth Teleseismic");

    protected JCheckBox vSmoothTeleseismic = new JCheckBox("Very Smooth Telelseismic");

    protected JCheckBox regional = new JCheckBox("Regional");

    protected JCheckBox local = new JCheckBox("Local");

    protected static SeisGramText localeText = new SeisGramText(null);

    protected static int TWOPASS = ColoredFilter.TWOPASS;

    private static Color[] filterColors = { Color.yellow, Color.green, Color.black, Color.darkGray, Color.orange };

    protected static ColoredFilter lp_p1Hz = new ColoredFilter(localeText,
                                                               0.0,
                                                               0.05,
                                                               2,
                                                               TWOPASS,
                                                               filterColors[0]);

    protected static ColoredFilter lp_p5Hz = new ColoredFilter(localeText,
                                                               0.0,
                                                               0.5,
                                                               2,
                                                               TWOPASS,
                                                               filterColors[1]);

    protected static ColoredFilter lp1Hz = new ColoredFilter(localeText,
                                                             0.0,
                                                             1.0,
                                                             2,
                                                             TWOPASS,
                                                             filterColors[2]);

    protected static ColoredFilter bp_1_10Hz = new ColoredFilter(localeText,
                                                                 0.1,
                                                                 10.0,
                                                                 2,
                                                                 TWOPASS,
                                                                 filterColors[3]);

    protected static ColoredFilter bp1_10Hz = new ColoredFilter(localeText,
                                                                1.0,
                                                                10.0,
                                                                2,
                                                                TWOPASS,
                                                                filterColors[4]);

    protected VerticalSeismogramDisplay listener;


    protected FilterSelectionListener filterCheck = new FilterSelectionListener();

    protected LinkedList currentFilters  = new LinkedList();

    class FilterSelectionListener implements ItemListener{
        public void itemStateChanged(ItemEvent e){
            int selected = e.getStateChange();
            boolean visibility;
            if(selected == ItemEvent.SELECTED){
                visibility = true;
            }else{
                visibility = false;
            }
            Object source = e.getItemSelectable();
            if(source == original){
                listener.setOriginalVisibility(visibility);
            }else{
                ColoredFilter chosenFilter;
                if(source == teleseismic){
                    chosenFilter = lp_p1Hz;
                }else if(source == smoothTeleseismic){
                    chosenFilter = lp_p5Hz;
                }else if(source == vSmoothTeleseismic){
                    chosenFilter=lp1Hz;
                }else if(source == regional){
                    chosenFilter=bp_1_10Hz;
                }else{
                    chosenFilter = bp1_10Hz;
                }
                if(visibility == true){
                    currentFilters.add(chosenFilter);
                }else{
                    currentFilters.remove(chosenFilter);
                }
                listener.applyFilter(chosenFilter);
            }
        }
    }

    Logger logger = Logger.getLogger(FilterSelection.class);

}// FilterSelection

