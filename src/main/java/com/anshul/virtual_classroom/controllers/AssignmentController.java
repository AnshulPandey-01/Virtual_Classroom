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
@RequestMapping("assignment")
public class AssignmentController {
	
	@Autowired
	private PostRepo postRepo;
	
	private TimeUtilityService timeUtility;
	
	public AssignmentController(TimeUtilityService timeUtility) {
		this.timeUtility = timeUtility;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/create")
	public ResponseEntity<Response> createAssignment(
			@RequestParam("title") String title, @RequestParam("content") String content, @RequestParam(name = "attachment") MultipartFile file,
			@RequestParam(name = "createdBy") String createdBy, @RequestParam("assignTime") String assignTime, @RequestParam("dueTime") String dueTime,
			@RequestParam("sem") int sem, @RequestParam(name = "branch") String branch, @RequestParam(name = "section") String section,
			@RequestParam(name = "subjectCode") String subjectCode, @RequestParam("marks") int marks){
		try {
			byte[] pdf = file.getBytes();
			
			Post post = new Post(Boolean.TRUE, createdBy, timeUtility.getCurrentTime(), title, content, pdf,
					assignTime, dueTime, sem, branch, section, subjectCode, marks);
			postRepo.save(post);
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Assignment created successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Respond.error.toString(), "An error occured please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional(readOnly=true)
	@RequestMapping(method = RequestMethod.GET, value = "/attachment/{id}")
	public ResponseEntity<Response> getAssignments(@PathVariable("id") int id, HttpServletResponse response){
		Post assignment = postRepo.findByIdAndIsAssignment(id, Boolean.TRUE).orElse(null);
		if(Objects.isNull(assignment)) {
			return ResponseEntity.notFound().build();
		}
		
		byte[] file = assignment.getAttachment();
		
//		ByteArrayResource byteResource = new ByteArrayResource(file);
//		
//	    HttpHeaders headers = new HttpHeaders();
//        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//        headers.add("Pragma", "no-cache");
//        headers.add("Expires", "0");
//        headers.add("Content-Disposition", "attachment; filename=" + post.getTitle() + "_" + "attachment.pdf");
//        
//	    return ResponseEntity.ok().headers(headers).body(byteResource);
		
		
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLength(file.length);
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=" + assignment.getTitle() + "_" + "attachment.pdf");
        response.setHeader(headerKey, headerValue);
        
        try (OutputStream outStream = response.getOutputStream()) {
            outStream.write(file);
            return null;
        } catch (Exception e) {
        	e.printStackTrace();
        	return ResponseEntity.internalServerError().build();
        }
        
	}
	
}
