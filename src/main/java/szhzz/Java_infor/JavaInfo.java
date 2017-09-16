/**
 * UtilityTest.java demonstrates usage of the progress bar and
 * border methods. It is not intended as an example of good
 * graphics design.
 * <p/>
 * Copyright &copy; 1996-1998 Martin Minow. All Rights Reserved.<p>
 * <p/>
 * Permission to use, copy, modify, and redistribute this software and its
 * documentation for personal, non-commercial use is hereby granted provided that
 * this copyright notice and appropriate documentation appears in all copies. This
 * software may not be distributed for fee or as part of commercial, "shareware,"
 * and/or not-for-profit endevors including, but not limited to, CD-ROM collections,
 * online databases, and subscription services without specific license.<p>
 *
 * @author <a href="mailto:minow@apple.com">Martin Minow</a>
 * @version 1.0
 * Set tabs every 4 characters.
 */
package szhzz.Java_infor;

import java.applet.Applet;
import java.awt.*;

public class JavaInfo extends java.applet.Applet {
    public static final int textAreaRow = 24;
    public static final int textAreaCol = 40;
    public static final int systemPropertyChoice = 0;
    public static final int toolkitDataChoice = 1;
    public static final int systemLocaleDataChoice = 2;
    public static final int systemTimezonesChoice = 3;
    public static final int systemThreadInfoChoice = 4;
    public static final int aboutJavaInfoChoice = 5;
    public static final int firstJava11Item = 2;
    public static final String[] choices = {
            "System Properties",
            "Toolkit Data",
            "System Locale Data",
            "System Timezones",
            "Current Threads",
            "About JavaInfo"
    };
    public TextArea textArea = new TextArea(textAreaRow, textAreaCol);
    EtchedBorder border = new EtchedBorder(textArea);
    Choice parameterChoice = new Choice();
    int currentChoice = -1;
    Button systemColorInfoButton = new Button("System Color Info");
    Button beepButton = new Button("Beep");
    Button systemThreadInfoButton = new Button("Update ThreadInfo");
    Panel buttonPanel = new Panel();
    ColorTestDialog colorTestDialog = null;
    boolean java11Classes = false;

    /**
     * Stripped-down QuickSort.
     *
     * @param vector     The vector of strings to sort
     * @param startIndex The first element to sort
     * @param endIndex   The last element to sort
     * @usage JavaInfo.quickSort(vector, 0, vector.length - 1);
     */
    public static void quickSort(
            String[] vector,
            int startIndex,
            int endIndex
    ) {
        int i = startIndex;
        int j = endIndex;
        String pivot = vector[(i + j) / 2];
        do {
            while (i < endIndex && pivot.compareTo(vector[i]) > 0) {
                ++i;
            }
            while (j > startIndex && pivot.compareTo(vector[j]) < 0) {
                --j;
            }
            if (i < j) {
                String temp = vector[i];
                vector[i] = vector[j];
                vector[j] = temp;
            }
            if (i <= j) {
                ++i;
                --j;
            }
        } while (i <= j);
        if (startIndex < j) {
            quickSort(vector, startIndex, j);
        }
        if (i < endIndex) {
            quickSort(vector, i, endIndex);
        }
    }

    public static void main(
            String args[]
    ) {
        Frame window = new Frame("JavaInfo");
        Applet applet = new JavaInfo();
        window.setLayout(new BorderLayout());
        window.add("Center", applet);
        window.pack();
//        window.resize(500, 380);
        window.setSize(500, 380);
        window.validate();
        window.setVisible(true);
        applet.init();
    }

    public void init() {
        setBackground(Color.lightGray);
        textArea.setBackground(Color.white);
        border.setBackground(Color.gray);
        border.setLabelFont(new Font("TimesRoman", Font.BOLD, 12));
        border.setThickness(3).setGap(1);
        setFont(new Font("Courier", Font.PLAIN, 10));
        textArea.setEditable(false);
        try {
            String version = System.getProperty("java.version");
            java11Classes = (version.compareTo("1.1") >= 0);
        } catch (SecurityException e) {
            java11Classes = false;
        }
        for (int i = 0; i < firstJava11Item; i++) {
            parameterChoice.addItem(choices[i]);
        }
        if (java11Classes) {
            for (int i = firstJava11Item; i < choices.length; i++) {
                parameterChoice.addItem(choices[i]);
            }
        }
        selectChoice(0);
        Panel mainPanel = new Panel();
        mainPanel.setLayout(new BorderLayout(2, 2));
        mainPanel.add("Center", border);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        /*
           * Setting the font to Geneva 9 misaligns the popup window
           * with respect to the buttons.
           */
        Font font = new Font("Dialog", Font.PLAIN, 12);
        /* */
        systemThreadInfoButton.setFont(font);
        systemThreadInfoButton.setBackground(Color.white);
        buttonPanel.add(systemThreadInfoButton);
        systemThreadInfoButton.setVisible(false);
        /* */
        parameterChoice.setFont(font);
        parameterChoice.setBackground(Color.white);
        buttonPanel.add(parameterChoice);
        if (java11Classes) {
            systemColorInfoButton.setFont(font);
            systemColorInfoButton.setBackground(Color.white);
            beepButton.setFont(font);
            beepButton.setBackground(Color.white);
            buttonPanel.add(systemColorInfoButton);
            buttonPanel.add(beepButton);
        }
        mainPanel.add("South", buttonPanel);
        SimpleBorder memoryPanel = new SimpleBorder(new MemoryStatus());
        SimpleBorder responsePanel = new SimpleBorder(new ResponseTest());
        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        gridbag.setConstraints(mainPanel, c);
        add(mainPanel);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        gridbag.setConstraints(memoryPanel, c);
        add(memoryPanel);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.insets = new Insets(2, 2, 2, 2);
        gridbag.setConstraints(responsePanel, c);
        add(responsePanel);
        validate();
        setVisible(true);
    }

    public boolean action(
            Event event,
            Object arg
    ) {
        boolean result = true;
        if (event.target == systemColorInfoButton) {
            ColorTestDialog colorTestDialog = new ColorTestDialog(this);
        } else if (event.target == beepButton) {
            Toolkit.getDefaultToolkit().beep();
        } else if (event.target == systemThreadInfoButton) {
            if (currentChoice == systemThreadInfoChoice) {
                textArea.setText(new SystemThreadInfo(null).toString());
            }
        } else if (event.target == parameterChoice) {
            int newChoice = parameterChoice.getSelectedIndex();
            if (newChoice != currentChoice) {
                selectChoice(newChoice);
            }
        } else {
            result = false;
        }
        return (result);
    }

    public void selectChoice(
            int newChoice
    ) {
        int oldChoice = currentChoice;
        currentChoice = newChoice;
        border.setLabelText(choices[newChoice]);
        textArea.setText("Processing " + choices[newChoice] + "...");
        SystemInfo info = null;
        switch (newChoice) {
            case systemPropertyChoice:
                info = new SystemPropertyInfo(border);
                break;
            case toolkitDataChoice:
                info = new ToolkitDataInfo(border);
                break;
            case systemLocaleDataChoice:
                info = new SystemLocaleInfo(border);
                break;
            case systemTimezonesChoice:
//TODO 	HF		info = new SystemTimezonesInfo(border);
                break;
            case systemThreadInfoChoice:
                info = new SystemThreadInfo(border);
                break;
            case aboutJavaInfoChoice:
                info = new AboutJavaInfo(border);
                break;
            default:
                textArea.setText("Strange choice: " + newChoice);
                break;
        }
        if (oldChoice != systemThreadInfoChoice
                && currentChoice == systemThreadInfoChoice) {
            systemThreadInfoButton.setVisible(true);
            buttonPanel.doLayout();
            buttonPanel.repaint();
        } else if (oldChoice == systemThreadInfoChoice
                && currentChoice != systemThreadInfoChoice) {
            systemThreadInfoButton.setVisible(false);
            buttonPanel.doLayout();
            buttonPanel.repaint();
        }
        if (info != null) {
            textArea.setText(info.toString());
        }
    }
}

