/**
 * ProcessDisplay.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;



public class ProcessDisplay extends JTable implements StatusListener{

    private ProcessDisplay(){
        setModel(ptm);
        ptm.setColumnSizes();
    }

    public static ProcessDisplay getDisplay(){
        return display;
    }

    public void add(Process proc){
        proc.addStatusListener(this);
        ptm.add(proc);
    }

    public int numColumns(){
        return ptm.getColumnCount();
    }

    public void statusUpdated(Process updated) {
        ptm.fireTableChanged(new TableModelEvent(ptm));
    }

    private class ProcessTableModel extends AbstractTableModel{
        public void  setColumnSizes() {
            int columnCount = getColumnCount();
            for(int counter = 0; counter < columnCount; counter++) {
                String columnName =getColumnName(counter);
                int width = 50;
                if( columnName.equals("Name") ) width = 100;
                else if( columnName.equals("Status") ) width = 200;
                else if( columnName.equals("Finished")) width = 20;
                getColumnModel().getColumn(counter).setPreferredWidth(width);
                getColumnModel().getColumn(counter).setMinWidth(width);

            }
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        public Class getColumnClass(int columnIndex) {
            if(columnIndex < 2){
                return String.class;
            }
            return Boolean.class;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Process rowProcess = (Process)processes.get(rowIndex);
            if(columnIndex == 0){
                return rowProcess.getName();
            }
            if(columnIndex == 1){
                return rowProcess.getStatus();
            }
            return Boolean.valueOf(rowProcess.isFinished());
        }

        public int getRowCount() {
            return processes.size();
        }

        public void add(Process proc){
            processes.add(proc);
            fireTableChanged(new TableModelEvent(this));
        }

        private String[] columnNames = {"Name", "Status", "Finished"};

        private List processes = new ArrayList();
    }

    private ProcessTableModel ptm = new ProcessTableModel();

    private final static ProcessDisplay display = new ProcessDisplay();
}

