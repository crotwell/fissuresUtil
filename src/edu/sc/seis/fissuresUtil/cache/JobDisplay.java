/**
 * JobDisplay.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;



public class JobDisplay extends JTable implements TrackerListener{

    private JobDisplay(){
        setModel(ptm);
        ptm.setColumnSizes();
        tracker.add(this);
    }

    public void trackerUpdated(JobTracker tracker) {
        ptm.fireTableChanged(new TableModelEvent(ptm));
    }

    public static JobDisplay getDisplay(){
        return display;
    }

    public int numColumns(){
        return ptm.getColumnCount();
    }

    private class ProcessTableModel extends AbstractTableModel{
        public void  setColumnSizes() {
            int columnCount = getColumnCount();
            for(int counter = 0; counter < columnCount; counter++) {
                String columnName =getColumnName(counter);
                int width = 50;
                if( columnName.equals("Name") ) width = 100;
                else if( columnName.equals("Status") ) width = 100;
                else if( columnName.equals("Finished")) width = 20;
                getColumnModel().getColumn(counter).setPreferredWidth(width);
                getColumnModel().getColumn(counter).setMinWidth(width/2);

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
            Job rowProcess;
            if(tracker.getActiveJobs().size() > rowIndex){
                rowProcess = (Job)tracker.getActiveJobs().get(rowIndex);
            }else if(getRowCount() > rowIndex){
                rowProcess = (Job)tracker.getFinishedJobs().get(rowIndex - tracker.getActiveJobs().size());
            }else{
                return null;
            }
            if(columnIndex == 0){
                return rowProcess.getName();
            }
            if(columnIndex == 1){
                return rowProcess.getStatus();
            }
            return Boolean.valueOf(rowProcess.isFinished());
        }

        public int getRowCount() {
            return tracker.getActiveJobs().size() + tracker.getFinishedJobs().size();
        }

        private String[] columnNames = {"Name", "Status", "Finished"};

    }

    private JobTracker tracker = JobTracker.getTracker();

    private ProcessTableModel ptm = new ProcessTableModel();

    private final static JobDisplay display = new JobDisplay();
}

