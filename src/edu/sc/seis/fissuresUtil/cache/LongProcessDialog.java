/**
 * LongProcessDialog.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.fissuresUtil.cache;

import java.awt.BorderLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LongProcessDialog implements Runnable {

    public LongProcessDialog(JFrame parent, String message, Runnable runnable) {
        this.runnable = runnable;
        dialog = new JDialog(parent, "Long running process");
        dialog.getContentPane().setLayout(new BorderLayout());
        JTextArea textArea = new JTextArea();
        textArea.setText(message);
        dialog.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        dialog.pack();
        dialog.show();
    }

    public void run() {
        runnable.run();
        dialog.hide();
        dialog.dispose();
    }

    protected JDialog dialog;
    protected Runnable runnable;

}

