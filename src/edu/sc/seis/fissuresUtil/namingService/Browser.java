package edu.sc.seis.fissuresUtil.namingService;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
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