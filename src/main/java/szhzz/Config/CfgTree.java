package szhzz.Config;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.LinkedList;
import java.util.Vector;


/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12-9-8
 * Time: 上午10:29
 * To change this template use File | Settings | File Templates.
 * <p/>
 */
public class CfgTree extends CfgProvider {

    protected CfgTree() {

    }

    /**
     * @param groupName Cfg 文件所在的目录节点
     * @return
     */
    public static CfgTree getInstance(String groupName) {
        return (CfgTree) getInstance(groupName, false);
    }

    public static CfgProvider getInstance(String groupName, boolean safeModel) {
        CfgProvider onlyOne = provider.get(groupName);
        if (onlyOne == null || !(onlyOne instanceof CfgTree)) {
            onlyOne = new CfgTree();
            onlyOne.setSafeModel(safeModel);
            onlyOne.laodCfgs(groupName);
            provider.put(groupName, onlyOne);
        }
        return (CfgTree) onlyOne;
    }

    public LinkedList<String> getSubCfgIDs() {
        return getSubCfgIDs(cfg);
    }

    /**
     * @param c
     * @return
     */
    public LinkedList<String> getSubCfgIDs(Config c) {
        return null;
    }


    public void createTree(DefaultMutableTreeNode top) {
        DefaultMutableTreeNode level1 = null;
        for (String cfgId : cfgNames) {
            level1 = createTree(cfgId);
            top.add(level1);
        }
    }


    protected DefaultMutableTreeNode createTree(String cfgID) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(cfgID);
        Config cfg = getCfg(cfgID);
        if (cfg != null) {
            LinkedList<String> cfgIDs = getSubCfgIDs(cfg);
            for (String id : cfgIDs) {
                DefaultMutableTreeNode child = createTree(id);
                node.add(child);
            }
        }
        return node;
    }

    private String getInUsed(String cfgID, String cfgBeSearched, String path) {
        Config cfg_ = getCfg(cfgBeSearched);
        String retval = "";
        if (cfg_ == null) {
            return "";
        }
        if (path.length() > 0) path += "->";
        path += cfgBeSearched;

        LinkedList<String> cfgIDs = getSubCfgIDs(cfg_);
        for (String id : cfgIDs) {
            if (cfgID.equals(id)) {
                return path;
            } else {
                String suPath = getInUsed(cfgID, id, path);
                if (suPath.length() > 0) {
                    if (retval.length() > 0) {
                        retval += "\n";
                    }
                    retval += suPath;
                }
            }
        }
        return retval;
    }

    public Vector<String> getInUsed(String cfgID) {
        Vector<String> inUsed = new Vector<String>();
        for (Config cfg_ : allCfgs.values()) {
            String path = getInUsed(cfgID, cfg_.getConfigID(), "");
            if (path.length() > 0) {
                inUsed.add(path);
            }
        }
        return inUsed;
    }

    public Config getCfg(String cfgID) {
        return getCfg(cfgID, false);
    }
}
