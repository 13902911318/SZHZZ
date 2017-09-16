/** * SimpleBorder draws a one-pixel border around its enclosure.<p> * <p/> * Copyright &copy; 1997 Martin Minow. All Rights Reserved.<p> * <p/> * Permission to use, copy, modify, and redistribute this software and its * documentation for personal, non-commercial use is hereby granted provided that * this copyright notice and appropriate documentation appears in all copies. This * software may not be distributed for fee or as part of commercial, "shareware," * and/or not-for-profit endevors including, but not limited to, CD-ROM collections, * online databases, and subscription services without specific license.<p> * * @author <a href="mailto:minow@apple.com">Martin Minow</a> * @version 1.0 * Set tabs every 4 characters. */package szhzz.Java_infor;import java.awt.*;class SimpleBorder extends Panel {    /**     * Create a SimpleBorder panel.     *     * @param inside The Component that will be bordered.     *               The border is drawn in black by default.     *               <p/>     *               Usage:     *               parentComponent.add(new SimpleBorder(insideComponent));     */    public SimpleBorder(            Component inside    ) {        setLayout(new BorderLayout());        add("Center", inside);        setForeground(Color.black);    }    public void update(            Graphics g    ) {        paint(g);    }    public void paint(            Graphics g    ) {        g.setColor(getForeground());        Dimension size = size();        g.drawRect(0, 0, size.width - 1, size.height - 1);    }    public Insets insets() {        Insets insets = super.insets();        insets.top += 1;        insets.left += 1;        insets.right += 1;        insets.bottom += 1;        return (insets);    }}