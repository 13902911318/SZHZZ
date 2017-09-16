/** * MemoryStatus.java runs a background process that sleeps for a * pre-determined amount of time. When it awakens, it displays * current memory usage in a "ticker-tape" display window. * <p/> * This is loosly based on StatusMemory.java by Jef Poskanzer, from * his public distribution on <http://www.acme.com> * <p/> * Copyright &copy; 1996-1998 Martin Minow. All Rights Reserved.<p> * <p/> * Permission to use, copy, modify, and redistribute this software and its * documentation for personal, non-commercial use is hereby granted provided that * this copyright notice and appropriate documentation appears in all copies. This * software may not be distributed for fee or as part of commercial, "shareware," * and/or not-for-profit endevors including, but not limited to, CD-ROM collections, * online databases, and subscription services without specific license.<p> * * @author <a href="mailto:minow@apple.com">Martin Minow</a> * @version 1.0 * Set tabs every 4 characters. */package szhzz.Java_infor;import java.awt.*;public class MemoryStatus extends Panel implements Runnable {    public static final long defaultSleepTime = 2000;    public static final String startString = "Start";    public static final String stopString = "Stop";    private static final int nHistory = 4;    private MemoryStatusGraph memoryStatusGraph;    private MemoryStatusInfo memoryStatusInfo;    private UpdateIntervalPanel updateIntervalPanel;    /*     * Initialize the startStopButton with the longer of the two strings.     */    private Button startStopButton = new Button(startString);    private Button initButton = new Button("Clear");    private Button gcButton = new Button("Garbage Collect");    private EtchedBorder border;    private Thread thread = null;    private long sleepTime = defaultSleepTime;    private transient long[] usedMemory = new long[nHistory];    private transient long[] totalMemory = new long[nHistory];    private transient long[] timeMSec = new long[nHistory];    private transient double[] bytesPerMsec = new double[nHistory];    private transient int borderTotalK = Integer.MAX_VALUE;    private transient int borderUsageK = Integer.MAX_VALUE;    private transient Runtime runtime = Runtime.getRuntime();    /**     * MemoryStatus provides a simple measure of system memory usage.     * It sleeps for a specified time (default is two seconds) then     * measures the current memory usage, writing the data into a     * "ticker-tape" canvas and displaying it in this panel.     */    public MemoryStatus() {        setFont(getFont());        memoryStatusGraph = new MemoryStatusGraph();        memoryStatusInfo = new MemoryStatusInfo();        updateIntervalPanel = new UpdateIntervalPanel();        Panel displayPanel = new Panel();        displayPanel.setLayout(new BorderLayout());        displayPanel.add("West", memoryStatusInfo);        displayPanel.add("Center", memoryStatusGraph);        initButton.setBackground(Color.white);        startStopButton.setBackground(Color.white);        gcButton.setBackground(Color.white);        /* */        Panel bottomButtonPanel = new Panel();        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));        bottomButtonPanel.setFont(getFont());        bottomButtonPanel.setBackground(Color.gray);        /* */        bottomButtonPanel.add(initButton);        bottomButtonPanel.add(startStopButton);        bottomButtonPanel.add(gcButton);        /* */        Panel buttonPanel = new Panel();        buttonPanel.setLayout(new BorderLayout());        buttonPanel.add("North", updateIntervalPanel);        buttonPanel.add("South", bottomButtonPanel);        /* */        border = new EtchedBorder(new SimpleBorder(displayPanel));        border.setBackground(Color.gray);        border.setLabelFont(new Font("TimesRoman", Font.BOLD, 12));        border.setLabelText("Memory Usage");        border.setThickness(3).setGap(1);        setLayout(new BorderLayout(2, 2));        add("Center", border);        add("South", buttonPanel);        validate();        start();    }    public void start() {        if (thread == null) {            initializeMemoryStatus();            thread = new Thread(this);            thread.setName("Memory Status");            thread.start();            startStopButton.setLabel(stopString);        }    }    public void stop() {        thread = null;        startStopButton.setLabel(startString);    }    public void run() {        while (thread != null) {            long start = System.currentTimeMillis();            try {                thread.sleep(sleepTime);            } catch (InterruptedException e) {            }            computeMemoryStatus();        }    }    /*     * We use action to remain Java 1.0.2 complient     */    public boolean action(            Event event,            Object arg    ) {        boolean result = true;        if (event.target == startStopButton) {            if (thread != null) {                stop();            } else {                start();            }        } else if (event.target == initButton) {            initializeMemoryStatus();            computeMemoryStatus();        } else if (event.target == updateIntervalPanel) {            setSleepTime(updateIntervalPanel.getUpdateInterval());        } else if (event.target == gcButton) {            System.gc();	/* Force garbage collection */        } else {            result = false;        }        return (result);    }    private synchronized void initializeMemoryStatus() {        long totalMemory = runtime.totalMemory();        long timeMSec = System.currentTimeMillis();        long usedMemory = totalMemory - runtime.freeMemory();        for (int i = 0; i < nHistory; i++) {            this.usedMemory[i] = usedMemory;            this.totalMemory[i] = totalMemory;            this.timeMSec[i] = timeMSec;            this.bytesPerMsec[i] = 0.0;        }        borderTotalK = Integer.MAX_VALUE;        borderUsageK = Integer.MAX_VALUE;        memoryStatusInfo.repaint();        memoryStatusGraph.resetGraph();    }    private void computeMemoryStatus() {        long thisTotalMemory = runtime.totalMemory();        long thisTimeMSec = System.currentTimeMillis();        long thisUsedMemory = thisTotalMemory - runtime.freeMemory();        int totalK;        int usageK;        double usageKBytesPerSec;        synchronized (this) {            /*             * Update the history and insert the new values.			 */            for (int i = 1; i < nHistory; i++) {                usedMemory[i] = usedMemory[i - 1];                totalMemory[i] = totalMemory[i - 1];                timeMSec[i] = timeMSec[i - 1];                bytesPerMsec[i] = bytesPerMsec[i - 1];            }            usedMemory[0] = thisUsedMemory;            totalMemory[0] = thisTotalMemory;            timeMSec[0] = thisTimeMSec;            bytesPerMsec[0] =                    ((double) usedMemory[0] - usedMemory[1])                            / ((double) timeMSec[0] - timeMSec[1]);            totalK = (int) ((thisTotalMemory + 1023) / 1024);            usageK = (int) ((thisUsedMemory + 1023) / 1024);			/*             * Compute the weighted average of the number of KBytes/sec.			 * This watches for memory leaks.			 */            usageKBytesPerSec =                    ((3.0 * bytesPerMsec[0]                            + (2.0 * bytesPerMsec[1])                            + (bytesPerMsec[2])) / 6144.0);	/* 6.0 * 1024.0 */        }        memoryStatusGraph.setValues(totalK, usageK);        memoryStatusInfo.setValues(totalK, usageK, usageKBytesPerSec);    }    /*     * Accessor functions to allow MemoryStatus to live as a JavaBean.     *     * These accessors pass the request to the Graph component. The range     * from the top of the memory area to the top of the graph view area     * is drawn in the background color.     */    public long getSleepTime() {        return (sleepTime);    }    public void setSleepTime(            long newSleepTime    ) {        if (newSleepTime > 0) {            this.sleepTime = newSleepTime;            if (thread != null) {                thread.interrupt();            }        }    }    public Color getGraphGridColor() {        return (memoryStatusGraph.getGridColor());    }    public void setGraphGridColor(            Color color    ) {        memoryStatusGraph.setGridColor(color);    }    public Color getGraphFreeColor() {        return (memoryStatusGraph.getFreeColor());    }    public void setGraphFreeColor(            Color color    ) {        memoryStatusGraph.setFreeColor(color);    }    public Color getGraphUsageColor() {        return (memoryStatusGraph.getUsageColor());    }    public void setGraphUsageColor(            Color color    ) {        memoryStatusGraph.setUsageColor(color);    }    /*     * These accessors pass the request to the Info (text) component.     */    public Font getInfoFont() {        return (memoryStatusInfo.getFont());    }    public void setInfoFont(            Font font    ) {        memoryStatusInfo.setFont(font);	/* Does this repaint?	*/    }    public Color getInfoBackground() {        return (memoryStatusInfo.getBackground());    }    public void setInfoBackground(            Color color    ) {        memoryStatusInfo.setBackground(color);    }    public Color getInfoForeground() {        return (memoryStatusInfo.getForeground());    }    public void setInfoForeground(            Color color    ) {        memoryStatusInfo.setForeground(color);    }    /*     * These accessors manage the MemoryStatus component as a whole.     */    public boolean isRunning() {        return (thread != null);    }    public void setRunning(            boolean runNow    ) {        if (runNow != (thread != null)) {            stop();            if (runNow) {                start();            }        }    }    public Dimension preferredSize() {        return (new Dimension(260, 160));    }    public Dimension getPreferredSize() {        return (preferredSize());    }}