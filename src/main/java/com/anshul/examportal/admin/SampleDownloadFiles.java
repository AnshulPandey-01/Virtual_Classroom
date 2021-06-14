package com.anshul.examportal.admin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@CrossOrigin//(origins ="http://localhost:4500")
@Controller
public class SampleDownloadFiles {
	
	@GetMapping("/download/{fileName}")
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(@PathVariable("fileName") String fileName) {
		String currentDirectory = System.getProperty("user.dir");
		File file = new File(currentDirectory + "\\Excel_Sample_File\\" + fileName + ".xlsx");
		
		Path path = Paths.get(file.getAbsolutePath());
		ByteArrayResource resource = null;
		try {
			resource = new ByteArrayResource(Files.readAllBytes(path));
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}

	    HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Disposition", "attachment; filename=" + fileName +".xlsx");
        
	    return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(file.length())
	            .body(resource);
	}
	
}
