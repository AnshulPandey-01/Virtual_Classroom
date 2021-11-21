package com.anshul.virtual_classroom.controllers;

import java.io.OutputStream;
import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.anshul.virtual_classroom.entity.Post;
import com.anshul.virtual_classroom.repos.PostRepo;
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Respond;
import com.anshul.virtual_classroom.utility.TimeUtilityService;

@CrossOrigin
@RestController
@RequestMapping("post")
public class PostController {
	
	@Autowired
	private PostRepo postRepo;
	
	private TimeUtilityService timeUtility;
	
	public PostController(TimeUtilityService timeUtility) {
		this.timeUtility = timeUtility;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/create")
	public ResponseEntity<Response> createPost(
			@RequestParam("title") String title, @RequestParam("content") String content, @RequestParam(name = "attachment", required = false) MultipartFile file,
			@RequestParam(name = "createdBy") String createdBy, @RequestParam("sem") int sem, @RequestParam(name = "branch") String branch,
			@RequestParam(name = "section") String section, @RequestParam(name = "subjectCode") String subjectCode){
		try {
			Post post = new Post(Boolean.FALSE, createdBy, timeUtility.getCurrentTime(), title, content, sem, branch, section, subjectCode);
			if(Objects.nonNull(file)) {
				post.setAttachment(file.getBytes());
			}
			
			postRepo.save(post);
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Post created successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Respond.error.toString(), "An error occured please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional(readOnly=true)
	@RequestMapping(method = RequestMethod.GET, value = "/attachment/{id}")
	public ResponseEntity<Resource> getAssignments(@PathVariable("id") int id, HttpServletResponse response){
		Post post = postRepo.findByIdAndIsAssignment(id, Boolean.FALSE).orElse(null);
		if(Objects.isNull(post) || Objects.isNull(post.getAttachment())) {
			return ResponseEntity.notFound().build();
		}
		
		byte[] file = post.getAttachment();
		
		ByteArrayResource byteResource = new ByteArrayResource(file);
		
	    HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Disposition", "attachment; filename=" + post.getTitle() + "_" + "attachment.pdf");
        
	    return ResponseEntity.ok().headers(headers).body(byteResource);
		
		
//		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        response.setContentLength(file.length);
//        String headerKey = "Content-Disposition";
//        String headerValue = String.format("attachment; filename=" + post.getTitle() + "_" + "attachment.pdf");
//        response.setHeader(headerKey, headerValue);
//        
//        try (OutputStream outStream = response.getOutputStream()) {
//            outStream.write(file);
//            return null;
//        } catch (Exception e) {
//        	e.printStackTrace();
//        	return ResponseEntity.internalServerError().build();
//        }
        
	}
	
}
