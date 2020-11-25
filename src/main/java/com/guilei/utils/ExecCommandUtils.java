package com.guilei.utils;

import java.io.*;

/**
 * 调用操作系统的执行命令，例如： ipconfig之类的
 * @since 2017/5/22
 * @author guilei
 */
public class ExecCommandUtils {

    /**
     *
     * @param processinfo 命令行字符串
     * @return
     */
    public static boolean openExe(String processinfo)
    {
        Runtime runtime = Runtime.getRuntime();
        Process ps = null;

        try
        {
            ps = runtime.exec(processinfo);
            ps.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null)
            {
                System.out.println(line);
            }
            return true;
        }
        catch (IOException e)
        {
            try
            {
                PrintWriter pw = new PrintWriter(System.currentTimeMillis() + ".log");
                pw.println(e.getMessage());
                pw.close();
            }
            catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }
            return false;
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
