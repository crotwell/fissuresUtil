package edu.sc.seis.fissuresUtil.display;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;

/**
 * ExtendedJTextArea.java
 *
 *
 * Created: Wed Mar 12 19:24:45 2003
 *
 * @author <a href="mailto:"></a>
 * @version 1.0
 */
public class ExtendedJTextArea extends JTextArea{
    public ExtendedJTextArea() {
    super();
    this.addMouseListener(new MyMouseListener(this));
    setLineWrap(true);
    } // ExtendedJTextArea constructor

    public ExtendedJTextArea(Document doc) {
    super(doc);
    this.addMouseListener(new MyMouseListener(this));
    setLineWrap(true);
    }
    public  ExtendedJTextArea(Document doc, String text, int rows, int columns) {
    super(doc, text, rows, columns);
    this.addMouseListener(new MyMouseListener(this));
    setLineWrap(true);
    }
    
    public ExtendedJTextArea(int rows, int columns) {
    super(rows, columns);
    this.addMouseListener(new MyMouseListener(this));
    setLineWrap(true);
    }

    public ExtendedJTextArea(String text) {
    super(text);
    this.addMouseListener(new MyMouseListener(this));
    setLineWrap(true);
    }
   
    public ExtendedJTextArea(String text, int rows, int columns) {
    super(text, rows, columns);
    this.addMouseListener(new MyMouseListener(this));
    setLineWrap(true);
    }
    
   

    public static void main(String[] args) {
    
    JFrame frame = new JFrame();
    frame.getContentPane().add(new ExtendedJTextArea("hi this is a test"));
    frame.pack();
    frame.show();
    }
    
} // ExtendedJTextArea

class MyMouseListener extends MouseAdapter {
    public MyMouseListener(JTextArea area) {
    this.area = area;
    }

    public void mouseEntered(MouseEvent me) {
    //  System.out.println("Mouse Entered");
    }

    public void mouseClicked(MouseEvent me) {
    //here generate code for context sensitive help.
    //  System.out.println("mouse is clicked");
    if(me.isPopupTrigger()) {
        popMenu(me);
    } else {
        if(popMenu != null) {
        popMenu.setVisible(false);
        }
    }
    }
    
    public void mousePressed(MouseEvent me) {
    //  System.out.println("Mouse Pressed");
    if(me.isPopupTrigger()) {
        popMenu(me);
    } else {
        if(popMenu != null) {
        popMenu.setVisible(false);
        }
    }
    }
    
    public void mouseReleased(MouseEvent me) {
    //  System.out.println("Mouse Released");
    if(me.isPopupTrigger()) {
        popMenu(me);
    } else {
        if(popMenu != null) {
        popMenu.setVisible(false);
        }
    }
    }

    public void mouseExited(MouseEvent me) {
    //  System.out.println("Mouse Exited");
    }

    private void popMenu(MouseEvent me) {
    popMenu = new JPopupMenu("clipboard");
    popMenu.add( (Action)new copyAction("copy"));
    popMenu.add( (Action) new cutAction("cut"));
    popMenu.add( (Action) new pasteAction("paste"));
    popMenu.show(me.getComponent(), me.getX(), me.getY());
    //popMenu.pack();
    //popMenu.setVisible(true);
    }

    private JTextArea area;
    JPopupMenu popMenu;

    private  class copyAction extends AbstractAction {
    public copyAction(String name) {
        super(name);
    }
    
    public void actionPerformed(ActionEvent ae) {
        Clipboard systemboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection strSelection = new StringSelection(area.getSelectedText());
        systemboard.setContents(strSelection, strSelection);
        //  System.out.println("The text is "+area.getText());
    }
    }

    private class cutAction extends AbstractAction {
    
    public cutAction(String name) {
        super(name);
    }
    
    public void actionPerformed(ActionEvent ae) {
        Clipboard systemboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection strSelection = new StringSelection(area.getSelectedText());
        int start = area.getSelectionStart();
        int end = area.getSelectionEnd();
        area.replaceRange("", start, end);
        systemboard.setContents(strSelection, strSelection);
    }
    }

    private class pasteAction extends AbstractAction {

    public pasteAction(String name) {
        super(name);
        
    }
    
    public void actionPerformed(ActionEvent ae) {
        Clipboard systemboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = systemboard.getContents(null);
        if(        transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        String data = new String();
        try {
            data = (String)transferable.getTransferData(DataFlavor.stringFlavor);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Problem transfering data.", e);
        }
        int pos = area.getCaretPosition();
        area.insert(data, pos);
        }
    }
    }

}
