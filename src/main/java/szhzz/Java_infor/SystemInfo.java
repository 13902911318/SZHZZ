/** * SystemInfo is the parent of all of the system info providers. * <p/> * Copyright &copy; 1996-1998 Martin Minow. All Rights Reserved.<p> * <p/> * Permission to use, copy, modify, and redistribute this software and its * documentation for personal, non-commercial use is hereby granted provided that * this copyright notice and appropriate documentation appears in all copies. This * software may not be distributed for fee or as part of commercial, "shareware," * and/or not-for-profit endevors including, but not limited to, CD-ROM collections, * online databases, and subscription services without specific license.<p> * * @author <a href="mailto:minow@apple.com">Martin Minow</a> * @version 1.0 * Set tabs every 4 characters. */package szhzz.Java_infor;/** * SystemInfo is a simple wrapper for StringBuffer, which cannot * be subclassed directly. It is never used by itself, but only * as the parent of a concrete class, such as SystemPropertyInfo. * A subclass that needs to modify the enclosing border label * may do so by calling setBorderText(). Classes must provide * a no-parameter constructor. */public abstract class SystemInfo {    protected StringBuffer text = new StringBuffer();    protected EtchedBorder border = null;    public SystemInfo() {    }    public SystemInfo(            EtchedBorder border    ) {        this();        setBorder(border);    }    /**     * Border accessors.     */    public EtchedBorder getBorder() {        return (border);    }    public void setBorder(            EtchedBorder border    ) {        this.border = border;    }    public void setBorderText(            String text    ) {        if (border != null) {            border.setLabelText(text);        }    }    /**     * Subclasses will call append to add data to the StringBuffer()     */    public void append(            String someText    ) {        text.append(someText);    }    /**     * Return the StringBuffer itself.     */    public StringBuffer getText() {        return (text);    }    /**     * Return the contents of the StringBuffer.     */    public String toString() {        return (text.toString());    }}