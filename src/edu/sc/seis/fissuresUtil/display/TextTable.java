/**
 * TextTable.java
 *
 * A plain text table formatter.
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.display;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public class TextTable{
	
	protected int numColumns;
	private boolean hasHeader;
	private String[] header = null;
	protected List rows = new ArrayList();
	protected int[] widths;
	private static Logger logger = Logger.getLogger(TextTable.class);
	
	public TextTable(int columns){
		this(columns, false);
	}
	
	public TextTable(int columns, boolean hasHeader){
		numColumns = columns;
		this.hasHeader = hasHeader;
		initColWidthArray();
	}
	
	public void addRow(String tabDelimitedData){
		addRow(tabDelimitedData, false);
	}
	
	public void addRow(String tabDelimetedData, boolean isHeader){
		StringTokenizer tok = new StringTokenizer(tabDelimetedData,"\t");
		String[] data = new String[tok.countTokens()];
		for (int i = 0; i < data.length; i++) {
			data[i] = tok.nextToken();
		}
		addRow(data, isHeader);
	}
	
	public void addRow(String[] data){
		addRow(data, false);
	}
	
	public void addRow(String[] data, boolean isHeader){
		printTableStats();
		if (data.length > numColumns){
			String[] tmp = new String[numColumns];
			System.arraycopy(data, 0, tmp, 0, numColumns);
			System.out.println("truncating data (length " + data.length + ") to number of table columns (" + numColumns + ")");
			data = tmp;
		}
		else if (data.length < numColumns){
			logger.debug("data.length: " + data.length);
			return;
		}
		
		if (isHeader){
			header = data;
		}
		else{
			rows.add(data);
		}
		
		updateWidths(data);
		
		printTableStats();
	}
	
	private void updateWidths(String[] data){
		for (int i = 0; i < numColumns; i++) {
			if (data[i].length() > widths[i]){
				logger.debug("changing width of column " + i + " to " + data.length);
				widths[i] = data[i].length();
			}
		}
	}
	
	public void clear(){
		rows.clear();
		initColWidthArray();
		if (hasHeader && header != null){
			updateWidths(header);
		}
	}
	
	public String toString(){
		printTableStats();
		StringBuffer buf = new StringBuffer();
		if (hasHeader){
			String headerString = getRow(header);
			buf.append(headerString);
			buf.append(getHeaderHyphens(headerString.length()));
		}
		
		for (int i = 0; i < rows.size(); i++) {
			buf.append(getRow((String[])rows.get(i)));
		}
		
		return buf.toString();
	}
	
	private String getRow(String[] rowCells){
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < rowCells.length; i++) {
			buf.append(fillInWithSpaces(rowCells[i], i));
		}
		buf.append('\n');
		return buf.toString();
	}
	
	private String getHeaderHyphens(int length){
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buf.append('-');
		}
		buf.append('\n');
		return buf.toString();
	}
	
	private void initColWidthArray(){
		widths = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			widths[i] = 0;
		}
	}
	
	//I wish I knew what was wrong here
	private int calculateTabs(int colWidth, int cellLength){
		int tab = 3;
		int space = colWidth - cellLength;
		int numTabs = space/tab;
		if (numTabs < 0) numTabs = 0;
		if (space%tab == 0){
			numTabs ++;
		}
		return numTabs;
	}
	
	private String fillInWithSpaces(String cellValue, int column){
		StringBuffer buf = new StringBuffer();
		buf.append(cellValue);
		int width = widths[column];
		System.out.println("width of cell value is " + cellValue.length());
		System.out.println("width for column " + column + " is " + width);
		int numSpaces = width - cellValue.length() + 3;
		for (int i = 0; i < numSpaces; i++) {
			buf.append(' ');
		}
		return buf.toString();
	}
	
	public TextTable join(TextTable table){
		if (table.numColumns != numColumns){
			return table;
			//I'll throw an exception eventually.  I'm just not sure what yet.
		}
		
		rows.addAll(table.rows);
		
		for (int i = 0; i < numColumns; i++) {
			if (table.widths[i] > widths[i]){
				logger.debug("changing width of column " + i + " to " + table.widths.length);
				widths[i] = table.widths[i];
			}
		}
		
		return this;
	}
	
	public void printTableStats(){
		System.out.println("Column widths:");
		for (int i = 0; i < widths.length; i++) {
			System.out.println(i + ": " + widths[i]);
		}
	}
}

