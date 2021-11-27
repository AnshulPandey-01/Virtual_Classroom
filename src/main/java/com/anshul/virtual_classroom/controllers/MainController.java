package com.anshul.virtual_classroom.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Status;

@CrossOrigin
@Controller
public class MainController {
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/")
	public ResponseEntity<Response> welcome(){
		return new ResponseEntity<>(new Response(Status.success, "Welcome to Virtual Classroom"), HttpStatus.OK);
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/download/sample_file/{fileName}")
	public ResponseEntity<Resource> downloadFile(@PathVariable("fileName") String fileName) {
		fileName += ".xlsx";
		
		Resource resource = new ClassPathResource("static/Sample_Excel_Files/" + fileName);
		File file = null;
		ByteArrayResource byteResource = null;
		try {
			file = resource.getFile();
			Path path = Paths.get(file.getAbsolutePath());
			byteResource = new ByteArrayResource(Files.readAllBytes(path));
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.notFound().build();
		}
		
		ContentDisposition contentDisposition = ContentDisposition.builder("attachment").filename(fileName).build();
		CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.DAYS);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_PDF);
	    headers.setContentDisposition(contentDisposition);
	    headers.setCacheControl(cacheControl);
        
	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(file.length())
	            .body(byteResource);
	}
}
