/**
 * LegendPanel.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.fissuresUtil.map;

import java.awt.BorderLayout;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LegendPanel extends JPanel{

    public LegendPanel(){
        initComponents();
    }

    private void initComponents(){
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Map Key"));
        String imgPath = "edu/sc/seis/fissuresUtil/data/maps/";

        //Deep Events
        URL imgURL = getClass().getClassLoader().getResource(imgPath + "legend.png");
        ImageIcon imageIcon = new ImageIcon(imgURL);
        JLabel imgLabel = new JLabel(imageIcon);
        add(imgLabel, BorderLayout.CENTER);
    }

}

