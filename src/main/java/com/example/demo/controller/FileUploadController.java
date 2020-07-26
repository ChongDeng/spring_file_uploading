package com.example.demo.controller;

import com.example.demo.Result;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@CrossOrigin
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private Logger logger = LoggerFactory.getLogger(FileUploadController.class);


    @GetMapping("test")
    String test(){
        return "test";
    }

    @PostMapping("/part")
    @ResponseBody
    public Result bigFile(HttpServletRequest request, HttpServletResponse response, String guid, Integer chunk, MultipartFile file, Integer chunks) {
        try {

            String projectUrl = System.getProperty("user.dir").replaceAll("\\\\", "/");

            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                if (chunk == null) chunk = 0;

                // 临时目录用来存放所有分片文件
                String tempFileDir = projectUrl + "/upload/" + guid;
                File parentFileDir = new File(tempFileDir);
                if (!parentFileDir.exists()) {
                    parentFileDir.mkdirs();
                }

                // 分片处理时，前台会多次调用上传接口，每次都会上传文件的一部分到后台
                File tempPartFile = new File(parentFileDir, guid + "_" + chunk + ".part");
                FileUtils.copyInputStreamToFile(file.getInputStream(), tempPartFile);
            }

        } catch (Exception e) {
            return Result.failMessage(400,e.getMessage());
        }
        return Result.successMessage(200,"上次成功");
    }

    @RequestMapping("merge")
    @ResponseBody
    public Result mergeFile(String guid, String fileName) {

        String projectUrl = System.getProperty("user.dir").replaceAll("\\\\", "/");

        try {

            String sname = fileName.substring(fileName.lastIndexOf("."));
            //时间格式化格式
            Date currentTime = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
            //获取当前时间并作为时间戳
            String timeStamp = simpleDateFormat.format(currentTime);
            //拼接新的文件名
            String newName = timeStamp + sname;

            simpleDateFormat = new SimpleDateFormat("yyyyMM");
            String path = projectUrl + "/upload/";
            String tmp = simpleDateFormat.format(currentTime);

            System.out.println("parent folder: " + path);
            File parentFileDir = new File(path + guid);
            if (parentFileDir.isDirectory()) {

                //destTempFile 就是最终的文件
                File destTempFile = new File(path + tmp, newName);
                if (!destTempFile.exists()) {
                    //先得到文件的上级目录，并创建上级目录，在创建文件
                    destTempFile.getParentFile().mkdir();
                    try {
                        destTempFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < parentFileDir.listFiles().length; i++) {
                    File partFile = new File(parentFileDir, guid + "_" + i + ".part");
                    System.out.println("part file: " + partFile.getPath());
                    FileOutputStream destTempfos = new FileOutputStream(destTempFile, true);
                    //遍历"所有分片文件"到"最终文件"中
                    FileUtils.copyFile(partFile, destTempfos);
                    destTempfos.close();
                }

                // 删除临时目录中的分片文件
                FileUtils.deleteDirectory(parentFileDir);

                return Result.successMessage(200,"合并成功");
            }else{
                return Result.failMessage(400,"没找到目录");
            }

        } catch (Exception e) {
            return Result.failMessage(400,e.getMessage());
        }

    }


}
