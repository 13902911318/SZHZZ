package szhzz.Files;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: wn
 * Date: 2008-12-15
 * Time: 22:30:54
 * To change this template use File | Settings | File Templates.
 */
// This action creates and shows a modal open-file dialog.
public class OpenFileAction extends AbstractAction {
    JFrame frame;
    JFileChooser chooser;

    OpenFileAction(JFrame frame, JFileChooser chooser) {
        super("Open...");
        this.chooser = chooser;
        this.frame = frame;
    }

    public void actionPerformed(ActionEvent evt) {
        // Show dialog; this method does not return until dialog is closed
        chooser.showOpenDialog(frame);

        // Get the selected file
        File file = chooser.getSelectedFile();
    }
}


//        JFrame frame = new JFrame();
//        // Create a file chooser
//        String filename = File.separator+"tmp";
//        JFileChooser fc = new JFileChooser(new File(filename));
//
//        // Create the actions
//        Action openAction = new OpenFileAction(frame, fc);
//        Action saveAction = new SaveFileAction(frame, fc);
//
//        // Create buttons for the actions
//        JButton openButton = new JButton(openAction);
//        JButton saveButton = new JButton(saveAction);
//
//        // Add the buttons to the frame and show the frame
//        frame.getContentPane().add(openButton, BorderLayout.NORTH);
//        frame.getContentPane().add(saveButton, BorderLayout.SOUTH);
//        frame.pack();
//        frame.setVisible(true);
