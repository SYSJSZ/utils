package com.sy.combiz.common.compress;

import com.sy.combiz.profile.dto.AttachmentDto;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * @Description: 文件实体类
 * @Author: lsj
 * @Date: 2019-04-28 14:10
 */
public class FileBean implements Serializable {

    private static final long serialVersionUID = 6669747020406786160L;

    private String filePath;//文件保存路径

    private String fileName;//文件保存名称

    private String suffix; //文件后缀

    private boolean isOss = false; //是否Oss附件


    private AttachmentDto attachmentDto; //oss对象

    private OutputStream outputStream; //文件输出流 ps:暂时无用，生成的文件为空

    private byte[] bytes; //文件字节数组


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public boolean isOss() {
        return isOss;
    }

    public void setOss(boolean oss) {
        isOss = oss;
    }

    public AttachmentDto getAttachmentDto() {
        return attachmentDto;
    }

    public void setAttachmentDto(AttachmentDto attachmentDto) {
        this.attachmentDto = attachmentDto;
    }



    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
