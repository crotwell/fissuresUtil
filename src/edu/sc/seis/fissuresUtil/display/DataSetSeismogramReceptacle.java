package edu.sc.seis.fissuresUtil.display;

import java.util.List;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * DataSetSeismogramReceptacle defines an interface for objects wishing to hold
 * DataSetSeismogram objects
 *
 * Created: Tue Aug 27 14:44:13 2002
 *
 * @author <a href="mailto:">Charlie Groves</a>
 * @version 1.0
 * @see BasicSeismogramDisplay, Registrar, TimeConfig, AmpConfig
 */
public interface DataSetSeismogramReceptacle {
    /**
     * <code>add</code> adds all of the seismograms in the array to the receptacle
     *
     * @param seismos a <code>DataSetSeismogram[]</code> containing seismograms to be
     * added to this receptacle     
     */
    public void add(DataSetSeismogram[] seismos);

    /**
     * <code>remove</code> removes all of the seismograms in this array
     *
     * @param seismos <code>DataSetSeismogram[]</code> an array of seismograms to be removed
     * @return true if the seismograms were all contained in the receptacle and removed, 
     * false if it did not contain them
     */
    public boolean remove(DataSetSeismogram[] seismos);

    /**
     * <code>contains</code> checks the receptacle for the presence of seismo
     *
     * @param seismo the seismogram whose presence is to be tested
     * @return true if the receptacle contains seismo, false otherwise
     */
    public boolean contains(DataSetSeismogram seismo);   

    
    /**
     * returns all of the seismograms held by this receptacle
     *
     * @return an array containing all of this receptacles seismograms
     */ 
    public DataSetSeismogram[] getSeismograms();

    /**
     * reset takes all of the seismograms contained in this receptacle and sets their state as if they had just been added to the 
     * receptacle and sets the instance varaibles of the receptacle back to their initial states
     */
    public void reset();

    /**
     * <code>reset</code> takes all of the seismograms in the array and sets their state
     * as if they had just been initially added to the receptacle
     * @param seismos a <code>DataSetSeismogram[]</code> value containing seismograms to be
     * reset
     */
    public void reset(DataSetSeismogram[] seismos);    
}// DataSetSeismogramReceptacle
