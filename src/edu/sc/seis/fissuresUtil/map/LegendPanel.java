/**
 * LegendPanel.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map;

import java.awt.GridLayout;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LegendPanel extends JPanel{

    public LegendPanel(){
        initComponents();
    }

    private void initComponents(){
        setLayout(new GridLayout(1,2));
        String imgPath = "edu/sc/seis/fissuresUtil/data/maps/";

        //Deep Events
        URL imgURL = getClass().getClassLoader().getResource(imgPath + "deep-event.png");
        ImageIcon imageIcon = new ImageIcon(imgURL);
        JLabel imgLabel = new JLabel(imageIcon);
        JLabel description = new JLabel("Deep Earthquake");
        add(imgLabel);
        add(description);
    }

}

