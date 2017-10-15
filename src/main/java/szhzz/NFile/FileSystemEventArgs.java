package szhzz.NFile;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 14-5-10
 * Time: 上午12:16
 * To change this template use File | Settings | File Templates.
 */

import java.nio.file.WatchEvent.Kind;

/**
 * 文件系统事件类型
 *
 * @author wangxiang
 */
public final class FileSystemEventArgs {
    private final String fileName;
    private final Kind kind;

    public FileSystemEventArgs(String fileName, Kind kind) {
        this.fileName = fileName;
        this.kind = kind;
    }

    /**
     * 文件的路径
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 操作类型：变更、创建、删除
     */
    @SuppressWarnings("rawtypes")
    public Kind getKind() {
        return kind;
    }
}