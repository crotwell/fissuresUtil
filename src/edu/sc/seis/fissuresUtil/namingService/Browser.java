package edu.sc.seis.fissuresUtil.namingService;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import edu.sc.seis.fissuresUtil.simple.Initializer;


public class Browser extends JComponent {
    
    public Browser(FissuresNamingService fisName) {
        setLayout(new BorderLayout());
        TreeModel model = new NameServiceTreeModel(fisName);
        JTree tree = new JTree(model);
        JScrollPane scroll =  new JScrollPane(tree);
        add(scroll, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        // this parse the args, reads properties, and inits the orb
        Initializer.init(args);
        FissuresNamingService fisName = Initializer.getNS();
        Browser b = new Browser(fisName);
        JFrame frame = new JFrame("NS Browser");
        frame.getContentPane().add(b);
        frame.setSize(400, 400);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
}