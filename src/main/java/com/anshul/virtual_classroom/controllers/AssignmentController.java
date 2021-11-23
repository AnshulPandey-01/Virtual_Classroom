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
import com.anshul.virtual_classroom.response.Response.Status;
import com.anshul.virtual_classroom.utility.service.MailUtilityService;
import com.anshul.virtual_classroom.utility.service.TimeUtilityService;

@CrossOrigin
@RestController
@RequestMapping("assignment")
public class AssignmentController {
	
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private TimeUtilityService timeUtility;
	@Autowired
	private MailUtilityService mailService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/create")
	public ResponseEntity<Response> createAssignment(
			@RequestParam("title") String title, @RequestParam("content") String content, @RequestParam(name = "attachment") MultipartFile file,
			@RequestParam(name = "createdBy") String createdBy, @RequestParam("assignTime") String assignTime, @RequestParam("dueTime") String dueTime,
			@RequestParam("sem") int sem, @RequestParam(name = "branch") String branch, @RequestParam(name = "section") String section,
			@RequestParam(name = "subjectCode") String subjectCode, @RequestParam("marks") int marks, HttpServletRequest request) {
		try {
			byte[] pdf = file.getBytes();
			
			Post post = new Post(Boolean.TRUE, createdBy, timeUtility.getCurrentTime(), title, content, pdf,
					assignTime, dueTime, sem, branch, section, subjectCode, marks);
			postRepo.save(post);
			
//			mailService.sendMails(post, request);
			
			return new ResponseEntity<>(new Response(Status.success, "Assignment created successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Status.error, "An error occured please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional(readOnly=true)
	@RequestMapping(method = RequestMethod.GET, value = "/attachment/{uniqueKey}")
	public ResponseEntity<byte[]> getAssignments(@PathVariable("uniqueKey") String uniqueKey, HttpServletResponse response) {
		Post assignment = postRepo.findByUniqueKeyAndIsAssignment(uniqueKey, Boolean.TRUE).orElse(null);
		if(Objects.isNull(assignment)) {
			return ResponseEntity.notFound().build();
		}
		
		byte[] file = assignment.getAttachment();
		String fileName = assignment.getTitle() + "_" + "attachment.pdf";
		
		ContentDisposition contentDisposition = ContentDisposition.builder("inline").filename(fileName).build();
		CacheControl cacheControl = CacheControl.maxAge(1, TimeUnit.DAYS);
		
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_PDF);
	    headers.setContentDisposition(contentDisposition);
	    headers.setCacheControl(cacheControl);
	    
	    return new ResponseEntity<>(file, headers, HttpStatus.OK);
	}
	
}
