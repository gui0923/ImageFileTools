package com.guilei.image;

import com.guilei.utils.ExecCommandUtils;
import com.guilei.utils.FileStringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * BufferedImageZoomMain类有一个缺陷，就是针对大型图片，会因为内存不足而无法进行，将一个100M的图片读取到内存，内存直接就爆掉了
 * 所以如果更好的解决这个问题的话，就需要引入gdal工具，具体参考https://gdal.org, 引入这个工具之后，无论多大的文件，曾经处理过超过20G的单个TIF文件也可以
 * 正常的切割和缩放，这不是任何常用的图片处理工具能比拟的
 * @since 2020/11/24
 * @author guilei
 */
public class GdalZoomMain {
    public static void main(String[] args) {
        String srcDir = args[0];//原始图片的目录
        String tarDir = args[1];//目的拷贝文件的目录
        String scale = args[2];//文件需要缩放的比例尺，等比例缩放，这个地方gdal接收的是类似 20% 的字符串，不是0.2
        //两个目录里面，除了图片之外的其他文件是拷贝还是不拷贝，按照需要设置，默认为不拷贝
        boolean isCopyOtherFile = args.length < 4 ? false : (StringUtils.isBlank(args[3]) ? false : Boolean.valueOf(args[3]));
        new GdalZoomMain().copyFileAndImage(new File(srcDir), srcDir, tarDir, scale, isCopyOtherFile);
        while (threadPool.getActiveCount() > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        threadPool.shutdown();
        System.exit(0);
    }
    private static ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private static final Integer MAX_THREAD_NUMERS = Runtime.getRuntime().availableProcessors() * 2;
    private String format = " -outSize %s %s -of %s %s %s";

    public GdalZoomMain() {
        initFormat();
    }

    private void initFormat() {
        Properties properties = new Properties();
        try {
            properties.load(GdalZoomMain.class.getClassLoader().getResourceAsStream("GdalComman.properties"));
            format = properties.getProperty("gdal_translate", "gdal_translate") + format;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void copyFileAndImage(File file, String srcDir, String tarDir, String scale, boolean isCopyOtherFile) {
        if (file.isFile()) {
            copyAndZoomThreadPool(file, srcDir, tarDir, scale, isCopyOtherFile);
        } else {
            File[] files = file.listFiles();
            for (File file1 : files) {
                copyFileAndImage(file1, srcDir, tarDir, scale, isCopyOtherFile);
            }
        }
    }

    private  void copyAndZoomThreadPool(final File file, final String srcDir, final String tarDir, final String scale, final boolean isCopyOtherFile) {
        threadPool.execute(new Runnable() {
            public void run() {
                try {
                    copyAndZoom(file, srcDir, tarDir, scale, isCopyOtherFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        while (threadPool.getActiveCount() >= MAX_THREAD_NUMERS) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyAndZoom(File file, String srcDir, String tarDir, String scale, boolean isCopyOtherFile) throws IOException {
        File tarFile = new File(file.getAbsolutePath().replace(srcDir, tarDir));
        if (tarFile.exists()) {
            return;
        }
        FileUtils.forceMkdirParent(tarFile);
        String type = FileStringUtils.getFileSuffix(file);
        if (StringUtils.equals(type, "jpg") || StringUtils.equals(type, "JPG")) {
            ExecCommandUtils.openExe(String.format(format, scale, scale, "JPEG", file.getAbsolutePath(), tarFile.getAbsolutePath()));
            deleteAuxFile(tarFile);
        } else if (StringUtils.equals(type, "PNG") || StringUtils.equals(type, "png")) {
            ExecCommandUtils.openExe(String.format(format, scale, scale, "PNG", file.getAbsolutePath(), tarFile.getAbsolutePath()));
            deleteAuxFile(tarFile);
        } else {
            if (isCopyOtherFile) {
                FileUtils.copyFile(file, tarFile);
            }
        }
        System.out.println(file);
    }

    /**
     * 删除gdal_tanslate命令成功之后产生的.aux.xml文件
     * @param file
     */
    private void deleteAuxFile(File file) {
        try {
            new File(file.getAbsolutePath() + ".aux.xml").delete();
        } catch (Exception e) {

        }
    }

}
