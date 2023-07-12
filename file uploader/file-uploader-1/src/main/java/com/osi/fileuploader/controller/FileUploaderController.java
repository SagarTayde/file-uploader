package com.osi.fileuploader.controller;

import java.io.IOException;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.osi.fileuploader.service.Demo;
import com.osi.fileuploader.service.FileUploadservice;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/file-upload")
public class FileUploaderController {

	@Autowired
	private FileUploadservice fileUploaderService;
	
	
	
	@PostMapping("/")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws SQLException, ClassNotFoundException, IOException {
        fileUploaderService.fileUploader(file);
        return "File upload process completed.";
    }
	
	
}
