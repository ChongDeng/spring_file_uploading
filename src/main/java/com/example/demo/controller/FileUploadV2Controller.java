package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


//@CrossOrigin
@RestController
public class FileUploadV2Controller {

    @GetMapping( "/")
    public String page(){
        return "upload";
    }

    /**
     * 检查文件存在与否
     */
    @PostMapping("checkFile")
    @ResponseBody
    public Boolean checkFile(@RequestParam(value = "md5File") String md5File) {
        Boolean exist = false;

        //实际项目中，这个md5File唯一值，应该保存到数据库或者缓存中，通过判断唯一值存不存在，来判断文件存不存在，这里我就不演示了
		/*if(true) {
			exist = true;
		}*/
        return exist;
    }

    /**
     * 检查分片存不存在
     *   这里通过文件md5和分片数chunk来检查分片文件，其实严格一点还需要检查分片文件的偏移位置和文件大小，
     *   以防止分片文件上传失败的情况，这里表示一下就行，需要自己改改
     */
    @PostMapping("checkChunk")
    @ResponseBody
    public Boolean checkChunk(@RequestParam(value = "md5File") String md5File,
                              @RequestParam(value = "chunk") Integer chunk) {
        Boolean exist = false;

        String uploadFolderPath = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/uploadV2/";

        String path = uploadFolderPath + md5File+"/";//分片存放目录
        String chunkName = chunk+ ".tmp";//分片名
        File file = new File(path+chunkName);
        if (file.exists()) {
            exist = true;
        }
        return exist;
    }

    /**
     * 修改上传
     */
    @PostMapping("upload")
    @ResponseBody
    public Boolean upload(@RequestParam(value = "file") MultipartFile file,
                          @RequestParam(value = "md5File") String md5File,
                          @RequestParam(value = "chunk",required= false) Integer chunk) { //第几片，从0开始

        String uploadFolderPath = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/uploadV2/";
        String path = uploadFolderPath + md5File+"/";
        File dirfile = new File(path);
        if (!dirfile.exists()) {//目录不存在，创建目录
            dirfile.mkdirs();
        }

        String chunkName;
        if(chunk == null) {//表示是小文件，还没有一片
            chunkName = "0.tmp";
        }else {
            chunkName = chunk+ ".tmp";
        }

        String filePath = path+chunkName;
        File savefile = new File(filePath);

        try {
            if (!savefile.exists()) {
                savefile.createNewFile();//文件不存在，则创建
            }
            file.transferTo(savefile);//将文件保存
        }
        catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * 合成分片
     */
    @PostMapping("merge")
    @ResponseBody
    public Boolean  merge(@RequestParam(value = "chunks",required =false) Integer chunks,
                          @RequestParam(value = "md5File") String md5File,
                          @RequestParam(value = "name") String name) throws Exception {

        String uploadFolderPath = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/uploadV2/";

        FileOutputStream fileOutputStream = new FileOutputStream(uploadFolderPath+"/"+name);  //合成后的文件
        try {
            byte[] buf = new byte[1024];
            for(long i=0;i<chunks;i++) {
                String chunkFile=i+".tmp";
                File file = new File(uploadFolderPath+"/"+md5File+"/"+chunkFile);
                InputStream inputStream = new FileInputStream(file);
                int len = 0;
                while((len=inputStream.read(buf))!=-1){
                    fileOutputStream.write(buf,0,len);
                }
                inputStream.close();
            }
            //删除md5目录，及临时文件，这里代码省略

        } catch (Exception e) {
            return false;
        }finally {
            fileOutputStream.close();
        }
        return true;
    }
}
