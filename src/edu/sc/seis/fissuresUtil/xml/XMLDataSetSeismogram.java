package edu.sc.seis.fissuresUtil.xml;

/**
 * XMLDataSetSeismogram.java
 *
 *
 * Created: Thu Mar 20 19:02:57 2003
 *
 * @author <a href="mailto:chinna@SRINIVAS"></a>
 * @version 1.0
 */
public class XMLDataSetSeismogram implements SeisDataChangeListener{
    private XMLDataSetSeismogram() {
	finished = false;
	//add the opening tags fro datasetseismogram.
    } // XMLDataSetSeismogram constructor
    
    public synchronized static void insert(Element element,
			      DataSetSeismogram dss) {
	dss.retrieveData(this);
	while(finished != true) {
	    try {
		Thread.sleep(200);
	    } catch(Exception e) {
		
	    }
	}
	
	//add ending tags for datasetseismogram.
	return;
    }

    public void pushData(LocalSeismogram[] seis, SeisDataChangeListener listener) {
	if(listener != this) return;
	//must add xml for seis.
	
    }
    
    public void finished(SeisDataChangeListener listener) {
	if(listener != this) return;
	finished = true;
    }

    private boolean finished = false;
} // XMLDataSetSeismogram
