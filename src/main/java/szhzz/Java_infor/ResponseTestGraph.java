/** * ResponseTestGraph displays the response histogram. The graph * y-axis is lograthmic. * <p/> * Copyright &copy; 1996-1998 Martin Minow. All Rights Reserved.<p> * <p/> * Permission to use, copy, modify, and redistribute this software and its * documentation for personal, non-commercial use is hereby granted provided that * this copyright notice and appropriate documentation appears in all copies. This * software may not be distributed for fee or as part of commercial, "shareware," * and/or not-for-profit endevors including, but not limited to, CD-ROM collections, * online databases, and subscription services without specific license.<p> * * @author <a href="mailto:minow@apple.com">Martin Minow</a> * @version 1.0 * Set tabs every 4 characters. */package szhzz.Java_infor;import java.awt.*;public class ResponseTestGraph extends Canvas {    public static final double logE10 = Math.log(10.0);    private static final double minPower = 0.0;	/*    1 Msec	*/    private static final double maxPower = 3.0;	/* 1000 Msec	*/    private static final long minValue = 1;	/* TradeDate >= min	*/    private static final long maxValue = 1000;	/* TradeDate < max	*/    private int[] values = new int[1];    private int index = 0;    private boolean wrap = false;    private int width = 0;    private int height = 0;    private int graphWidth = 0;    private int graphHeight = 0;    private int xInset = 0;    private int yInset = 0;    /**     * ResponseTestGraph is a trivial ticker-tape histogram display.     */    public ResponseTestGraph() {        setBackground(Color.white);        initialize();    }    /**     * initialize resets the maxValue and restarts the display.     */    public void initialize() {        paint(getGraphics(), Long.MIN_VALUE, false, true);    }    /**     * Add a new value to the histogram and slide it along.     */    public void addValue(            long newValue    ) {        paint(getGraphics(), newValue, false, false);    }    public void update(            Graphics g    ) {        paint(g);    }    public void paint(            Graphics g    ) {        paint(g, Long.MIN_VALUE, true, false);    }    private synchronized void paint(            Graphics g,            long newValue,	/* Ignore if == Long.MIN_VALUE	*/            boolean fullRepaint,            boolean initialize    ) {        if (g == null) {            return;		/* Ignore early calls. */        }        Dimension d = size();        /*         * If the display shape changes or we are		 * explicitly re-initializing, reset the		 * graph shadow.		 */        if (d.width != width                || d.height != height                || initialize) {            FontMetrics fm = g.getFontMetrics();            xInset = fm.stringWidth(Long.toString(maxValue))                    + fm.charWidth(':') + 2;            yInset = fm.getAscent();            width = d.width;            height = d.height;            graphWidth = width - xInset;            graphHeight = height - yInset;            values = new int[graphWidth];            fullRepaint = true;            index = 0;            wrap = false;        }        if (fullRepaint) {            /*             * Clear the display and repaint all data.			 */            g.clearRect(0, 0, width, height);            drawYAxis(g);            drawXGridLines(g, 0, graphWidth);            g.setColor(Color.black);            if (wrap) {                for (int i = index; i < graphWidth; i++) {                    g.drawLine(                            i + xInset,                            getBarLength(values[i]) + yInset,                            i + xInset,                            height                    );                }            }            for (int i = 0; i < index; i++) {                g.drawLine(                        i + xInset,                        getBarLength(values[i]) + yInset,                        i + xInset,                        height                );            }        }        if (newValue != Long.MIN_VALUE) {            if (newValue < minValue) {                newValue = minValue;            } else if (newValue >= maxValue) {                newValue = maxValue;            }            /*             * Add a new value.			 */            if (index >= graphWidth) {                index = 0;                wrap = true;            }            values[index++] = (int) newValue;            g.copyArea(xInset, yInset, graphWidth - 1, graphHeight, 1, 0);            g.clearRect(xInset, yInset, 1, graphHeight);            drawXGridLines(g, 0, 1);            g.setColor(Color.black);            g.drawLine(                    xInset,                    getBarLength((int) newValue) + yInset,                    xInset,                    height            );            if (false) {                // System.out.println("value " + newValue//                        + ", x1 " + xInset//                        + ", y1 " + (getBarLength((int) newValue) + yInset)//                        + ", x2 " + xInset//                        + ", y2 " + (height)//                );            }        }    }    private void drawXGridLines(            Graphics g,            int x,		/* Will be translated	*/            int length    ) {        x += xInset;        for (int major = (int) minValue; major <= (int) maxValue; major *= 10) {            int y = getBarLength(major) + yInset;            g.setColor(Color.blue);            g.drawLine(x, y, x + length, y);            g.setColor(Color.gray);            for (int minor = 2; minor < 10; minor += 2) {                y = getBarLength(major * minor) + yInset;                g.drawLine(x, y, x + length, y);            }        }    }    private void drawYAxis(            Graphics g    ) {        FontMetrics fm = g.getFontMetrics(getFont());        for (int major = (int) minValue; major <= (int) maxValue; major *= 10) {            String text = Integer.toString(major);            int textWidth = fm.stringWidth(text);            int y = getBarLength(major) + fm.getDescent() + yInset;            /*             * This hack ensures that the lowest value is fully displayed.			 */            if ((y + fm.getHeight()) >= height) {                y = height - fm.getHeight();            }            g.drawString(text, xInset - textWidth - 2, y);        }        g.drawLine(xInset - 1, 0, xInset - 1, height);    }    public Dimension getPreferredSize() {        FontMetrics fm = getFontMetrics(getFont());        return (new Dimension(                fm.stringWidth(Long.toString(maxValue)) + 2 + 144,                fm.getHeight() * (((int) maxPower) + 2) + 2        ));    }    public Dimension getMinimumSize() {        return (getPreferredSize());    }    public Dimension preferredSize() {        return (getPreferredSize());    }    public Dimension minimumSize() {        return (getMinimumSize());    }    private int getBarLength(            int thisValue    ) {        /*         * Get the log(10) of the value. It will be in the range		 * minPower <= log(value) < maxPowr.		 */        double logValue = Math.log((double) thisValue) / logE10;        double percent = (logValue - minPower) / maxPower;        if (false) {            // System.out.println("value = " + thisValue//                    + ", log " + Double.toString(logValue)//                    + ", pct " + Double.toString(percent)//                    + ", bar " + (graphHeight - ((int) (percent * (double) graphHeight)))//            );        }        return (graphHeight - ((int) (percent * (double) graphHeight)));    }}