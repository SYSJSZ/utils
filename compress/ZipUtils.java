package com.sy.combiz.common.compress;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @Description: zip压缩工具类
 * @Author: lsj
 * @Date: 2019-04-29 9:40
 */
public class ZipUtils {

    public static Logger logger = LoggerFactory.getLogger(ZipUtils.class);

    private static final int BUFFER_SIZE = 2 * 1024;

    private static final String ZIP_SUFFIX = ".zip";

    /**
     * 压缩成ZIP 方法1
     * @param srcDir 压缩文件夹路径
     * @param targetDir    压缩文件输出流
     * @param fileName    压缩后文件名字
     * @param keepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @return 压缩文件路径
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static String toZip(String srcDir, String targetDir,String fileName, boolean keepDirStructure)
            throws RuntimeException {
        long start = System.currentTimeMillis();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        File targetFile = new File(targetDir + File.separator + fileName + ZIP_SUFFIX);
        try {
            if(!targetFile.getParentFile().exists()){
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
            }
            fos = new FileOutputStream(targetFile);
            //使用缓冲流
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(bos);
            File sourceFile = new File(srcDir);
            if(!sourceFile.exists()){
                throw new RuntimeException("待压缩的文件目录："+srcDir+"不存在");
            }
            compress(sourceFile, zos, sourceFile.getName(), keepDirStructure);
            long end = System.currentTimeMillis();
            logger.info("压缩完成，耗时：" + (end - start) + " ms，文件路径："+targetFile.getPath());
            return targetFile.getPath();
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 压缩成ZIP 方法2
     * @param srcFiles 需要压缩的文件列表
     * @param targetDir           压缩文件指定路径
     * @param fileName    压缩后文件名字
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static String toZip(List<File> srcFiles, String targetDir,String fileName) throws RuntimeException {
        long start = System.currentTimeMillis();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        File targetFile = new File(targetDir + File.separator + fileName + ZIP_SUFFIX);
        try {
            fos = new FileOutputStream(targetFile);
            //使用缓冲流
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(bos);
            for (File srcFile : srcFiles) {
                if(!srcFile.exists()){
                    logger.error("压缩文件："+srcFile.getName()+"不存在");
                }else {
                    byte[] buf = new byte[BUFFER_SIZE];
                    zos.putNextEntry(new ZipEntry(srcFile.getName()));
                    int len;
                    FileInputStream in = new FileInputStream(srcFile);
                    while ((len = in.read(buf)) != -1) {
                        zos.write(buf, 0, len);
                    }
                    zos.closeEntry();
                    in.close();
                }
            }
            long end = System.currentTimeMillis();
            logger.info("压缩完成，耗时：" + (end - start) + " ms，文件路径："+targetFile.getPath());
            return targetFile.getPath();
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param name       压缩后的名称
     * @param keepDirStructure  是否保留原来的目录结构,true:保留目录结构;
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean keepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (keepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }

            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (keepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,
                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), keepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), keepDirStructure);
                    }

                }
            }
        }
    }


    /**
     * zip解压
     * @param srcFile        zip源文件路径
     * @param targetDir     解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static void unZip(String srcFile, String targetDir) throws RuntimeException {
        long start = System.currentTimeMillis();
        File file = new File(srcFile);
        // 判断源文件是否存在
        if (!file.exists()) {
            throw new RuntimeException(file.getPath() + "所指文件不存在");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                logger.info("解压" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = targetDir + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(targetDir + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if(!targetFile.getParentFile().exists()){
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[BUFFER_SIZE];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            logger.info("解压完成，耗时：" + (end - start) +" ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if(zipFile != null){
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * zip解压
     * @param srcFile        zip源文件
     * @param targetDir     解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static void unZip(File srcFile, String targetDir) throws RuntimeException {
        long start = System.currentTimeMillis();
        File file = srcFile;
        // 判断源文件是否存在
        if (!file.exists()) {
            throw new RuntimeException(file.getPath() + "所指文件不存在");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                logger.info("解压" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = targetDir + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(targetDir + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if(!targetFile.getParentFile().exists()){
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[BUFFER_SIZE];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            logger.info("解压完成，耗时：" + (end - start) +" ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if(zipFile != null){
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        /** 测试压缩方法1  */
        ZipUtils.toZip("D:/软件下载","D:/软件下载","aaa", true);

        /** 测试压缩方法2  */
        List<File> fileList = new ArrayList<>();
        fileList.add(new File("D:/软件下载/ChromeSetup.exe"));
        fileList.add(new File("D:/软件下载/DingTalk_v3.3.3.exe"));
        FileOutputStream fos2 = new FileOutputStream(new File("D:/软件下载/mytest02.zip"));
        ZipUtils.toZip(fileList, "D:/软件下载","aaa");
    }

    @Test
    public void testUnZip(){
        ZipUtils.unZip("D:/软件下载/aa.zip","D:/软件下载/testUnZip");
    }

    @Test
    public void testUnZip2(){
        ZipUtils.unZip(new File("D:/软件下载/aa.zip"),"D:/软件下载/testUnZip2");
    }

}
