package edu.sc.seis.fissuresUtil.display;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;              //for layout managers
import java.awt.event.*;        //for action and window events

//drag and drop
import java.awt.dnd.*;
import java.awt.datatransfer.*;

/**
 * TextInfoDisplay.java
 *
 *
 * Created: Fri May 31 10:04:14 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public abstract class TextInfoDisplay extends JPanel{

    public TextInfoDisplay (){
        setLayout(new BorderLayout());
        //Create a text pane.
        textPane = new JTextPane();
        textPane.setEditable(false);
        paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //paneScrollPane.setPreferredSize(new Dimension(250, 155));
        paneScrollPane.setMinimumSize(new Dimension(10, 15));
        add(paneScrollPane, BorderLayout.CENTER);
        initStylesForTextPane(textPane);

    }


    public void appendLabelValue(Document doc, String label, String value)
        throws javax.swing.text.BadLocationException {
        //      doc.insertString(doc.getLength(),
        //               label+": ",
        //               textPane.getStyle("label"));
        doc.insertString(doc.getLength(),
                         label,
                         textPane.getStyle("label"));
        doc.insertString(doc.getLength(),
                         value+"\n",
                         textPane.getStyle("value"));
    }

    public  void appendLine(Document doc, String value)
        throws javax.swing.text.BadLocationException {
        doc.insertString(doc.getLength(),
                         value+"\n",
                         textPane.getStyle("value"));
    }


    public void appendHeader(Document doc, String value)
        throws javax.swing.text.BadLocationException {
        doc.insertString(doc.getLength(),
                         value+"\n",
                         textPane.getStyle("header"));
    }

    public void displayProblem(String problem) {
        Document doc = textPane.getDocument();
        try {
            doc.remove(0, doc.getLength());
            appendProblem(doc, problem);
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    public void appendProblem(Document doc, String problem)
        throws javax.swing.text.BadLocationException {
        doc.insertString(doc.getLength(),
                         "Problem: ",
                         textPane.getStyle("label"));
        doc.insertString(doc.getLength(),
                         problem+"\n",
                         textPane.getStyle("problem"));
    }

    public Document getDocument() {
        return textPane.getDocument();
    }

    public void clear() {
        Document doc = textPane.getDocument();
        try {
            doc.remove(0, doc.getLength());
            toTop();
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert message.");
        }
    }

    public void toTop() {
        int min = paneScrollPane.getVerticalScrollBar().getMinimum();
        paneScrollPane.getVerticalScrollBar().setValue(min);
        repaint();
    }

    protected void initStylesForTextPane(JTextPane textPane) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
            getStyle(StyleContext.DEFAULT_STYLE);

        Style header = textPane.addStyle("header", def);
        StyleConstants.setFontFamily(header, "SansSerif");
        StyleConstants.setFontSize(header, 18);
        StyleConstants.setForeground(header, Color.green);

        Style regular = textPane.addStyle("regular", def);
        StyleConstants.setFontFamily(regular, "Serif");
        StyleConstants.setFontFamily(regular, "MonoSpaced");

        Style s = textPane.addStyle("label", regular);
        StyleConstants.setBold(s, true);

        s = textPane.addStyle("value", regular);

        s = textPane.addStyle("problem", regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, Color.red);

        Style mono = StyleContext.getDefaultStyleContext().
            getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(mono, "MonoSpaced");
        mono = textPane.addStyle("mono", mono);
    }

    JTextPane textPane;
    JScrollPane paneScrollPane;

}// TextInfoDisplay
