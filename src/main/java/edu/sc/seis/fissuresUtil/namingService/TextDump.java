package edu.sc.seis.fissuresUtil.namingService;

import javax.swing.tree.TreeModel;
import edu.sc.seis.fissuresUtil.simple.Initializer;

public class TextDump {

    public static void main(String[] args) {
        Initializer.init(args);
        FissuresNamingService fisName = Initializer.getNS();
        TreeModel model = new NameServiceTreeModel(fisName);
        dump(model.getRoot(), model, "");
    }

    public static void dump(Object obj, TreeModel model, String indent) {
        indent += "   ";
        for(int i = 0; i < model.getChildCount(obj); i++) {
            Object child = model.getChild(obj, i);
            System.out.println(indent + child);
            dump(child, model, indent);
        }
    }
}
