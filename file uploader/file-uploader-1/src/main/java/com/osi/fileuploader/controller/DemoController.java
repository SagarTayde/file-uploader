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

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/file-upload")
public class DemoController {

	@Autowired
	private Demo demo;
	
	@PostMapping("/demo")
    public String uploadFiles(@RequestParam("file") MultipartFile file) throws SQLException, ClassNotFoundException, IOException {
        demo.fileUploader(file);
        return "File upload process completed.";
    }
}
