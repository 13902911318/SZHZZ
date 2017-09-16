package szhzz.Utils;

/**
 * Created by Administrator on 2016/4/11.
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CaptureScreen {
    public static File captureScreen(Rectangle screenRectangle, String fileName) throws Exception {
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        String folderName = "c:\\tbd";
        //保存路径
        File folder = new File(folderName);
        if (!folder.exists()) {
            folder.mkdir();
        }
        File f = new File(folder, fileName);
        ImageIO.write(image, "png", f);
        return f;

//        //自动打开
//        if (Desktop.isDesktopSupported()
//                && Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
//            Desktop.getDesktop().open(f);
    }

    public static void main(String[] args) {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle screenRectangle = new Rectangle(screenSize);
            captureScreen(screenRectangle, "11.png");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
