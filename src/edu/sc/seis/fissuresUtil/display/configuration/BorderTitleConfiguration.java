package edu.sc.seis.fissuresUtil.display.configuration;

import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.display.borders.TitleProvider;
import edu.sc.seis.fissuresUtil.display.borders.UnchangingTitleProvider;

/**
 * @author groves Created on Feb 18, 2005
 */
public class BorderTitleConfiguration {

    public BorderTitleConfiguration(Element el) {
        title = DOMHelper.extractText(el, "text");
        f = new FontConfiguration(DOMHelper.extractElement(el, "font"));
    }

    public TitleProvider createTitle() {
        return new UnchangingTitleProvider(title, f.createFont());
    }

    private FontConfiguration f;

    private String title;
}