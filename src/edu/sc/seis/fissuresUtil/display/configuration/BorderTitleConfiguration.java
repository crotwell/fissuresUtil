package edu.sc.seis.fissuresUtil.display.configuration;

import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.display.borders.TitleProvider;
import edu.sc.seis.fissuresUtil.display.borders.UnchangingTitleProvider;

/**
 * @author groves Created on Feb 18, 2005
 */
public class BorderTitleConfiguration {

    public BorderTitleConfiguration(Element el) {
        if(el.hasAttribute("id")) {
            id = el.getAttribute("id");
        }
        title = DOMHelper.extractText(el, "text");
        f = FontConfiguration.create(DOMHelper.extractElement(el, "font"));
        if(DOMHelper.hasElement(el, "titleColor")) {
            titleColor = ColorConfiguration.create(DOMHelper.getElement(el,
                                                                        "titleColor"));
        }
    }

    public TitleProvider createTitle() {
        TitleProvider tp = new UnchangingTitleProvider(title, f.createFont());
        if(titleColor != null) {
            tp.setTitleColor(titleColor.createColor());
        }
        return tp;
    }

    public void setText(String text) {
        this.title = text;
    }
    
    public String getId() {
        return id;
    }

    private String id = "";

    private FontConfiguration f;

    private ColorConfiguration titleColor;

    private String title;
}