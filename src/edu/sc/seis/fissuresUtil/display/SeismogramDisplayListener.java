/**
 * SeismogramDisplayListener.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.display;

import edu.sc.seis.fissuresUtil.display.drawable.Drawable;

public interface SeismogramDisplayListener{
    /**
     *called when the display <code>from</code> is being replaced by <code>to</code>
     */
    public void switching(SeismogramDisplay from, SeismogramDisplay to);

    public void added(SeismogramDisplay recipient, Drawable drawable);

    public void removed(SeismogramDisplay bereaved, Drawable drawable);
}

