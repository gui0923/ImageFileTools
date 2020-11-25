package com.guilei.image;

import com.guilei.utils.FileStringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 使用BufferedImage进行文件的缩放
 *  ps. 实际上来自于公司的临时一个需求，将原先338G的高清图片（分辨率很高）的图片进行缩略，从而能够在有限的带宽和有限的客户端资源情况（普通pc，普通移动端）下
 *      能够顺利的展示，因此就采用了java原生的图片缩放的方式进行处理，由于普通的bufferediamge只支持JPG和PNG，所以其他的就
 * @since 2020/11/24
 * @author guil
 */
public class BufferedImageZoomMain {

    public static void main(String[] args) {
        String srcDir = args[0];//原始图片的目录
        String tarDir = args[1];//目的拷贝文件的目录
        double scale = Double.valueOf(args[2]);//文件需要缩放的比例尺，等比例缩放
        //两个目录里面，除了图片之外的其他文件是拷贝还是不拷贝，按照需要设置，默认为不拷贝
        boolean isCopyOtherFile = args.length < 4 ? false : (StringUtils.isBlank(args[3]) ? false : Boolean.valueOf(args[3]));

        try {
            new BufferedImageZoomMain().foreachDirs(new File(srcDir), srcDir, tarDir, scale, isCopyOtherFile);
            while (threadPool.getActiveCount() > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threadPool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    private static ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final Integer MAX_THREAD_NUMERS = Runtime.getRuntime().availableProcessors() * 2;


    /**
     * 嵌套遍历目录里面的所有文件
     * @param file
     * @param srcDir
     * @param tarDir
     * @param scale
     * @param isCopyOtherFile
     * @throws IOException
     */
    public void foreachDirs(File file, String srcDir, String tarDir, double scale, boolean isCopyOtherFile) throws IOException {
        if (file.isFile()) {
            File tarFile = new File(file.getAbsolutePath().replace(srcDir, tarDir));
            if (tarFile.exists()) {
                return;
            }
            FileUtils.forceMkdirParent(tarFile);
            zoomAndCopyImageThreadPool(file, scale, isCopyOtherFile, tarFile);
        } else {
            File[] files = file.listFiles();
            for (File file1 : files) {
                foreachDirs(file1, srcDir, tarDir, scale, isCopyOtherFile);
            }
        }
    }

    private void zoomAndCopyImageThreadPool(final File file, final double scale, final boolean isCopyOtherFile, final File tarFile) {
        threadPool.execute(new Runnable() {
            public void run() {
                try {
                    zoomAndCopyImage(file, scale, isCopyOtherFile, tarFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //由于定长线程池的LinkedBlockingQueue的容量为无限，但是如果一味着放里面放置新的等待队列，内存是扛不住的，所以还是要手动控制一下
        while (threadPool.getActiveCount() >= MAX_THREAD_NUMERS) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void zoomAndCopyImage(File file, double scale, boolean isCopyOtherFile, File tarFile) throws IOException {
        String type = FileStringUtils.getFileSuffix(file);
        if (StringUtils.equals(type, "jpg") || StringUtils.equals(type, "JPG")
                || StringUtils.equals(type, "PNG") || StringUtils.equals(type, "png")) {
            BufferedImage srcImage = null;
            try {
                srcImage = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (srcImage != null) {
                int sw = srcImage.getWidth();
                int sh = srcImage.getHeight();
                int tw = (int) (sw * scale);
                int th = (int) (sh * scale);
                ColorModel cm = srcImage.getColorModel();
                WritableRaster raster = cm.createCompatibleWritableRaster(tw, th);
                boolean alphaPremultiplied = cm.isAlphaPremultiplied();

                BufferedImage target = new BufferedImage(cm, raster, alphaPremultiplied, null);
                Graphics2D g = target.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawRenderedImage(srcImage, AffineTransform.getScaleInstance(scale, scale));
                g.dispose();
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(target, type.toLowerCase(), baos);
                    FileOutputStream fos = new FileOutputStream(tarFile);
                    fos.write(baos.toByteArray());
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (isCopyOtherFile) {
                FileUtils.copyFile(file, tarFile);
            }
        }
    }

}
