package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class UploadService {

    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/jpg","image/png","image/bmp");

    public String uploadImage(MultipartFile file) {
        try {
            //校验文件类型
            String contentType = file.getContentType();
            if(!ALLOW_TYPES.contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //检验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null){
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //准备目标路径
            File dest = new File("C:\\leyou_upload\\images", file.getOriginalFilename());
            //保存文件到本地
            file.transferTo(dest);
            //返回路径
            return "http://image.leyou.com/"+file.getOriginalFilename();
        } catch (IOException e) {
            //上传失败
            log.error("上传文件失败!",e);
            throw new LyException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }


    }
}
