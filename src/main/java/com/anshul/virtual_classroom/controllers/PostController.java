package com.anshul.virtual_classroom.controllers;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
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
import com.anshul.virtual_classroom.utility.service.MailUtilityService;
import com.anshul.virtual_classroom.utility.service.TimeUtilityService;

@CrossOrigin
@RestController
@RequestMapping("post")
public class PostController {
	
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private TimeUtilityService timeUtility;
	@Autowired
	private MailUtilityService mailService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/create")
	public ResponseEntity<Response> createPost(
			@RequestParam("title") String title, @RequestParam("content") String content, @RequestParam(name = "attachment", required = false) MultipartFile file,
			@RequestParam(name = "createdBy") String createdBy, @RequestParam("sem") int sem, @RequestParam(name = "branch") String branch,
			@RequestParam(name = "section") String section, @RequestParam(name = "subjectCode") String subjectCode, HttpServletRequest request) {
		try {
			Post post = new Post(Boolean.FALSE, createdBy, timeUtility.getCurrentTime(), title, content, sem, branch, section, subjectCode);
			if(Objects.nonNull(file)) {
				post.setAttachment(file.getBytes());
			}
			postRepo.save(post);
			
			mailService.sendMails(post, request);
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Post created successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Respond.error.toString(), "An error occured please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional(readOnly=true)
	@RequestMapping(method = RequestMethod.GET, value = "/attachment/{id}")
	public ResponseEntity<byte[]> getAssignments(@PathVariable("id") int id, HttpServletResponse response) {
		Post post = postRepo.findByIdAndIsAssignment(id, Boolean.FALSE).orElse(null);
		if(Objects.isNull(post) || Objects.isNull(post.getAttachment())) {
			return ResponseEntity.notFound().build();
		}
		
		byte[] file = post.getAttachment();
		String fileName = post.getTitle() + "_" + "attachment.pdf";
		
		ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(fileName).build();
		CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.DAYS);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_PDF);
	    headers.setContentDisposition(contentDisposition);
	    headers.setCacheControl(cacheControl);
	    
	    return new ResponseEntity<>(file, headers, HttpStatus.OK);        
	}
	
}
