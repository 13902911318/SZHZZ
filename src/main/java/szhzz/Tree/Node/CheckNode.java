package szhzz.Tree.Node;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: SZHZZ
 * Date: 13-1-26
 * Time: 下午8:33
 * To change this template use File | Settings | File Templates.
 * <p/>
 * 生成一颗节点为CheckNode的树，并setCellRenderer(new CheckRenderer());
 */
public class CheckNode extends DefaultMutableTreeNode {
    public final static int SINGLE_SELECTION = 0;
    public final static int DIG_IN_SELECTION = 4;
    protected int selectionMode;
    protected boolean isSelected;
    protected boolean expanded;

    public CheckNode() {
        this(null);
    }

    public CheckNode(Object userObject) {
        this(userObject, true, false);
    }

    public CheckNode(Object userObject, boolean allowsChildren, boolean isSelected) {
        super(userObject, allowsChildren);
        this.isSelected = isSelected;
        setSelectionMode(DIG_IN_SELECTION);
    }

    public int getSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(int mode) {
        selectionMode = mode;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.expanded = isExpanded;

        if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
            Enumeration e = children.elements();
            while (e.hasMoreElements()) {
                CheckNode node = (CheckNode) e.nextElement();
                node.setExpanded(isExpanded);
            }
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;

        if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
            Enumeration e = children.elements();
            while (e.hasMoreElements()) {
                CheckNode node = (CheckNode) e.nextElement();
                node.setSelected(isSelected);
            }
        }
    }
}

