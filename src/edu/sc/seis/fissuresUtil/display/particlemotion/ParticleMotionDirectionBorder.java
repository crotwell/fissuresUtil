package edu.sc.seis.fissuresUtil.display.particlemotion;

import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.borders.TitleBorder;

/**
 * @author hedx Created on Jul 5, 2005
 */
public class ParticleMotionDirectionBorder extends TitleBorder {

    public ParticleMotionDirectionBorder(int side,
                                         int order,
                                         SeismogramDisplay parent) {
        super(side, order);
        this.parent = parent;
    }

    public ParticleMotionSelfDrawableTitleProvider addLabel(String title,
                                                            ParticleMotionDisplayDrawable drawable) {
        ParticleMotionSelfDrawableTitleProvider provider = new ParticleMotionSelfDrawableTitleProvider(this,
                                                                                                       title,
                                                                                                       drawable,
                                                                                                       parent);
        add(provider);
        return provider;
    }

    public void removeTitle(ParticleMotionDisplayDrawable draw) { //removes the
                                                                  // title
                                                                  // associated
                                                                  // with the
                                                                  // drawable
        removeTitle(draw.getRightTitle());
        removeTitle(draw.getTopTitle());
    }

    private SeismogramDisplay parent;
}
