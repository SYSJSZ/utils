package com.sy.combiz.common.compress;

import com.sy.common.util.CollectionUtils;
import com.sy.common.util.Md5Util;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * @Description: 文件打包压缩，处理流文件，和oss文件
 * @Author: lsj
 * @Date: 2019-04-28 14:09
 */
public class CompressUtil {

    public static Logger logger = LoggerFactory.getLogger(CompressUtil.class);

    private static final String path = File.separator+"userdata"+File.separator+"zip"+File.separator+"data";

    private static final int byteSize = 1024;


    /**
     * 静态获取session对象
     * @return
     */
    private static HttpSession getSession() {
        HttpSession session = null;
        try {
            session = getRequest().getSession();
        } catch (Exception e) {
        }
        return session;
    }

    /**
     * 静态获取requqest对象
     * @return
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getRequest();
    }

    /**
     * 静态获取response对象
     * @return
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getResponse();
    }


    /**
     * 获取处理数据的默认目录
     * @return
     */
    private static String getTempFileDir(){
        //用当前毫秒值创建文件，防止同时请求文件占用
        return getRequest().getSession().getServletContext().getRealPath(path)+File.separator+ Md5Util.md5(Long.toString(new Date().getTime()));
    }

    /**
     * 导出
     * @param parentFileName
     * @param fileBeans
     * @return
     * @throws Exception
     */
    public static ResponseEntity<byte[]> doExport(String parentFileName,List<FileBean> fileBeans) throws Exception {
        //创建文件。返回处理数据的默认目录
        String fileDir = createFile(parentFileName, fileBeans);
        //压缩文件包路径
        String targetPath = fileDir+File.separator+"zip";
        //压缩
        String zipPath = doZip(fileDir+File.separator+parentFileName, targetPath, parentFileName, true);
        //下载，返回requestEntity
        ResponseEntity<byte[]> responseEntity = download(zipPath);
        //删除文件
        boolean b = deleteFolder(fileDir);
        logger.info(fileDir+"文件夹删除"+(b?"成功":"失败"));
        return responseEntity;
    }

    /**
     * 输出流导出
     * @param parentFileName
     * @param fileBeans
     * @throws Exception
     */
    public static void doExport2(String parentFileName,List<FileBean> fileBeans) throws Exception {
        //创建文件。返回处理数据的默认目录
        String fileDir = createFile(parentFileName, fileBeans);
        //压缩文件包路径
        String targetPath = fileDir+File.separator+"zip";
        //压缩
        String zipPath = doZip(fileDir+File.separator+parentFileName, targetPath, parentFileName, true);
        //下载
        download2(zipPath);
        //删除文件
        boolean b = deleteFolder(fileDir);
        logger.info(fileDir+"文件夹删除"+(b?"成功":"失败"));
    }

    /**
     * 下载
     * @param filePath 压缩文件路径
     * @return
     * @throws IOException
     */
    public static ResponseEntity<byte[]> download(String filePath) throws IOException {
        HttpServletRequest request = getRequest();
        File file = new File(filePath);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getName());
        return new ResponseEntity<byte[]>(org.apache.commons.io.FileUtils.readFileToByteArray(file),headers, HttpStatus.CREATED);
    }

    /**
     * 下载
     * @param filePath 压缩文件路径
     * @return
     * @throws IOException
     */
    public static String download2(String filePath) throws IOException {
        HttpServletResponse response = getResponse();
        File file = new File(filePath);
        // 如果文件存在，则进行下载
        if (file.exists()) {

            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));

            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                System.out.println("Download the song successfully!");
            }
            catch (Exception e) {
                System.out.println("Download the song failed!");
            }
            finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 创建文件
     * @param parentFileName
     * @param fileBeans
     * @return
     */
    public static String createFile(String parentFileName,List<FileBean> fileBeans){
        String fileDir = getTempFileDir();
        try {
            File file = new File(fileDir+File.separator+parentFileName);
            boolean mkFlag = createFileDir(file);
            if(mkFlag){
                if(CollectionUtils.isNotEmpty(fileBeans)){
                    fileBeans.forEach(fileBean -> {
                        try {
                            //拼接文件名
                            String fileName = splicingFileName(file.getCanonicalPath(), fileBean.getFilePath(), fileBean.getFileName(), fileBean.getSuffix());
                            File oneFile = new File(fileName);
                            if(!oneFile.getParentFile().exists()){
                                oneFile.getParentFile().mkdirs();
                            }
                            if(oneFile.exists()){
                                oneFile.delete();
                            }
                            oneFile.createNewFile();
                            //判断是否为oss服务器文件，做相关处理
                            if(!fileBean.isOss()) {
                                writeFile(oneFile, fileBean);
                            }else{
                                writeOssFile(oneFile,fileBean);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileDir;
    }

    /**
     * 压缩
     * @param srcDir 文件地址
     * @param targetDir 压缩文件存放地址
     * @param name 压缩名称
     * @param keepDirStructure 是否保留目录结构
     * @return
     */
    public static String doZip(String srcDir,String targetDir,String name,boolean keepDirStructure){
        String zipPath = ZipUtils.toZip(srcDir, targetDir, name, keepDirStructure);
        return zipPath;
    }


    /**
     * 创建文件目录
     * @param file
     * @return
     */
    public static boolean createFileDir(File file){
        boolean mkFlag = false;
        if(file.exists()){
            deleteFolder(file.getPath());
    }

        if(file.mkdirs()){
            mkFlag = true;
            logger.info("创建目录"+file.getPath()+"成功");
        }else{
            mkFlag =false;
            logger.info("创建目录"+file.getName()+"失败");
        }
        return mkFlag;
    }



    /**
     * 处理普通附件 暂时无效
     * @param file 写入的文件对象
     * @param outputStream 文件输出流
     * @throws IOException
     */
    public static void writeFile(File file, OutputStream outputStream) throws IOException {

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            logger.info(outputStream.toString());
            int len = 0;
            byte[] buffer = new byte[byteSize];
            while ((len = fileInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
                outputStream.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(outputStream != null){
                outputStream.close();
            }
            if(fileInputStream != null){
                fileInputStream.close();
            }
        }
    }

    /**
     * 处理普通附件
     * @param file 写入的文件对象
     * @param fileBean 文件相关实体类
     * @throws IOException
     */
    public static void writeFile(File file, FileBean fileBean) throws IOException {

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            //获取字节数组
            byte data[] = fileBean.getBytes();
            fileOutputStream.write(data);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            //4: 资源操作的最后必须关闭
            if(fileOutputStream != null){
                fileOutputStream.close();
            }
        }
    }

    /**
     * 写入oss服务器文件
     * @param file 写入的文件对象
     * @param fileBean 文件相关实体类
     * @throws IOException
     */
    public static void writeOssFile(File file, FileBean fileBean) throws IOException {

        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            InputStream inputStreamByUrl = getInputStreamByUrl(fileBean.getAttachmentDto().getRequestLink());
            fileOutputStream = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[byteSize];
            while ((len = inputStreamByUrl.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
                fileOutputStream.flush();
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(fileOutputStream != null){
                fileOutputStream.close();
            }
            if(fileInputStream != null){
                fileInputStream.close();
            }
        }
    }

    /**
     * 根据地址获得数据的输入流 get请求
     * @param strUrl 网络连接地址
     * @return url的输入流
     */
    public static InputStream getInputStreamByUrl(String strUrl) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(20 * 1000);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), output);
            return new ByteArrayInputStream(output.toByteArray());
        } catch (Exception e) {
            logger.error(e + "");
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                logger.error(e + "");
            }
        }
        return null;
    }

    /**
     *  根据路径删除指定的目录或文件，无论存在与否
     *@param sPath  要删除的目录或文件
     *@return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String sPath) {
       boolean flag = false;
       File file = new File(sPath);
        // 判断目录或文件是否存在
        if (!file.exists()) {  // 不存在返回 false
            return flag;
        } else {
            // 判断是否为文件
            if (file.isFile()) {  // 为文件时调用删除文件方法
                return deleteFile(sPath);
            } else {  // 为目录时调用删除目录方法
                return deleteDirectory(sPath);
            }
        }
    }

    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     * @param   sPath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 拼接文件名
     * @param fileDir
     * @param parentFileDir
     * @param fileName
     * @param suffix
     * @return
     */
    public static String splicingFileName(String fileDir,String parentFileDir,String fileName,String suffix){
        return fileDir+File.separator+parentFileDir+File.separator+fileName+"."+suffix;
    }
}
