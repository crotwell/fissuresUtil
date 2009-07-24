package edu.sc.seis.fissuresUtil.chooser;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import edu.iris.Fissures.IfNetwork.Station;

/**
 * @author groves Created on Nov 15, 2004
 */
public class SortedStationJList extends JList {

    public SortedStationJList(DefaultListModel listModel) {
        this(listModel, new NameListCellRenderer(true));
    }

    public SortedStationJList(DefaultListModel listModel,
            NameListCellRenderer namer) {
        super(listModel);
        this.namer = namer;
    }

    public void setNamer(NameListCellRenderer namer) {
        this.namer = namer;
    }

    public void sort() {
        DefaultListModel model = (DefaultListModel)this.getModel();
        // We need a java.util.List for for Collections.sort
        int size = model.getSize();
        List list = new ArrayList(size);
        for(int x = 0; x < size; ++x) {
            list.add(model.get(x));
        }
        Collections.sort(list, comparator);// sort the List
        // update the model
        for(int x = 0; x < size; ++x) {
            if(model.getElementAt(x) != list.get(x)) {
                model.set(x, list.get(x));
            }
        }
    }

    private NameListCellRenderer namer;

    private Comparator comparator = new Comparator() {

        private Comparator stringator = Collator.getInstance();

        public int compare(Object o1, Object o2) {
            return stringator.compare(getString(o1), getString(o2));
        }

        public String getString(Object o1) {
            if(o1 instanceof String) {
                return (String)o1;
            } else {
                return namer.getStringToDisplay((Station)o1);
            }
        }
    };
}