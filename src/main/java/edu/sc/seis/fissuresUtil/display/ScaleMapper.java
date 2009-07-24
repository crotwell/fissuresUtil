package edu.sc.seis.fissuresUtil.display;

/**
 * Simple interface for scale mappers, which decide how many and which values
 * should be used as ticks in scale bars, which should be labeled etc.
 * Separating this from the actual plotting widget increases flexibility and
 * reuse.
 *
 *
 * Created: Mon Oct 18 16:22:13 1999
 *
 * @author Philip Crotwell
 * @version
 */

public interface ScaleMapper  {

  /** Gets the pixel location for the ith tick.
   * @param i The number of the tick of interest.
   * @returns The pixel location for the ith tick.
   */
  public int getPixelLocation(int i);

    /** Gets the label if there is one for the ith tick. If there is no label
     *  then a empty string should be returned instead of a null.
     */
    public String getLabel(int i);

    /** Gets the total number of ticks.
     * @returns The total number of ticks.
     */
    public int getNumTicks();

    /** True if the ith tick is a major tick. This could be used by the
     *  plotting component to draw major tick longer or thicker.
     *
     * @param i The number of the tick of interest.
     * @returns True if the ith tick is major, false otherwise.
     */
    public boolean isMajorTick(int i);

    public String getAxisLabel();

    /**
     * returns the total pixels used to calculate map.
     * SBH
     * @return
     */
    public int getTotalPixels();

} // ScaleMapper
