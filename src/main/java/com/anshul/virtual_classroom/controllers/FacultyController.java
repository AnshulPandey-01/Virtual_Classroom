package com.anshul.virtual_classroom.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.virtual_classroom.entity.Faculty;
import com.anshul.virtual_classroom.entity.Test;
import com.anshul.virtual_classroom.repos.FacultyRepo;
import com.anshul.virtual_classroom.repos.PostRepo;
import com.anshul.virtual_classroom.repos.TestRepo;
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Status;
import com.anshul.virtual_classroom.response.test.PastTests;
import com.anshul.virtual_classroom.utility.ChangePassword;
import com.anshul.virtual_classroom.utility.assignment.AssignmentFacultyView;
import com.anshul.virtual_classroom.utility.post.PostFacultyView;

@CrossOrigin
@RestController
@RequestMapping("faculty")
public class FacultyController {
	
	@Autowired
	private FacultyRepo fRepo;
	@Autowired
	private TestRepo tRepo;
	@Autowired
	private PostRepo postRepo;
	
	private BCryptPasswordEncoder passwordEcorder;
	
	public FacultyController() {
		this.passwordEcorder = new BCryptPasswordEncoder();
	}
	
	@PostMapping(path="/login", consumes= {"application/json"})
	public ResponseEntity<List<String>> checkFacultyLogin(@RequestBody Faculty f) {
		List<String> list = new ArrayList<>();
		list.add("FACULTY");
		
		try {
			Faculty faculty = fRepo.getById(f.getEmail());
			if (faculty.isAllowed()) {
				if (passwordEcorder.matches(f.getPassword(), faculty.getPassword())) {
					list.add(faculty.getName());
					list.add(faculty.getEmail());
					return new ResponseEntity<>(list, HttpStatus.OK);
				} else {
					list.add("Incorrect Password");
					return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
				}
			} else {
				list.add("Your access is disabled");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			}
		} catch (EntityNotFoundException e) {
			list.add("Incorrect Email");
			return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/change_password", consumes= {"application/json"})
	public ResponseEntity<List<String>> changePassword(@RequestBody ChangePassword a) {
		List<String> list = new ArrayList<>();
		try {
			Faculty faculty = fRepo.getById((String)a.getEmail());
			if (passwordEcorder.matches(a.getPassword(), faculty.getPassword())) {
				faculty.setPassword(passwordEcorder.encode(a.getNewPassword()));
				fRepo.save(faculty);
				list.add("Password Changed Successfully");
				return new ResponseEntity<>(list, HttpStatus.OK);
			} else {
				list.add("Incorrect Password");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			}
		} catch (EntityNotFoundException e) {
			list.add("Incorrect Email");
			return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/{faculty}/tests")
	public ResponseEntity<Response> getTests(@PathVariable("faculty") String name) {
		try {
			Faculty faculty = fRepo.findByName(name).orElse(null);
			if (Objects.isNull(faculty)) {
				return new ResponseEntity<>(new Response(Status.error, "Faculty not found"), HttpStatus.NOT_FOUND);
			}
			
			List<Test> tests = tRepo.getScheduledTestsByFaculty(name);
			if (Objects.isNull(tests) || tests.size()==0) {
				return new ResponseEntity<>(new Response(Status.success, "No scheduled tests"), HttpStatus.OK);
			}
			
			return new ResponseEntity<>(new Response(Status.success, tests), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/{faculty}/past_tests")
	public ResponseEntity<Response> getFacultyPastTests(@PathVariable("faculty") String name) {
		Faculty faculty = fRepo.findByName(name).orElse(null);
		if (Objects.isNull(faculty)) {
			return new ResponseEntity<>(new Response(Status.error, "Faculty not found"), HttpStatus.NOT_FOUND);
		}
		
		List<Test> list = tRepo.getPastTestsByFaculty(name);
		if (Objects.isNull(list) || list.size()==0) {
			return new ResponseEntity<>(new Response(Status.success, "No previous test record"), HttpStatus.OK);
		}
		
		List<PastTests> pTests = new ArrayList<>();
		for (Test t : list)
			pTests.add(new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(), t.getResultOn()));
		
		return new ResponseEntity<>(new Response(Status.success, pTests), HttpStatus.OK);
	}
	
	@GetMapping("/{faculty}/assignments")
	public ResponseEntity<Response> getAssignments(@PathVariable("faculty") String name) {
		Faculty faculty = fRepo.findByName(name).orElse(null);
		if (Objects.isNull(faculty)) {
			return new ResponseEntity<>(new Response(Status.error, "Faculty not found"), HttpStatus.NOT_FOUND);
		}
		
		List<AssignmentFacultyView> assignments = postRepo.getAssignmentsCreatedBy(name);
		if (Objects.isNull(assignments) || assignments.size()==0) {
			return new ResponseEntity<>(new Response(Status.success, "No scheduled assignments"), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Status.success, assignments), HttpStatus.OK);
	}
	
	@GetMapping("/{faculty}/past_assignments")
	public ResponseEntity<Response> getPastAssignments(@PathVariable("faculty") String name) {
		Faculty faculty = fRepo.findByName(name).orElse(null);
		if (Objects.isNull(faculty)) {
			return new ResponseEntity<>(new Response(Status.error, "Faculty not found"), HttpStatus.NOT_FOUND);
		}
		
		List<AssignmentFacultyView> assignments = postRepo.getPastAssignmentsCreatedBy(name);
		if (Objects.isNull(assignments) || assignments.size()==0) {
			return new ResponseEntity<>(new Response(Status.success, "No scheduled assignments"), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Status.success, assignments), HttpStatus.OK);
	}
	
	@GetMapping("/{faculty}/posts")
	public ResponseEntity<Response> getPosts(@PathVariable("faculty") String name) {
		Faculty faculty = fRepo.findByName(name).orElse(null);
		if (Objects.isNull(faculty)) {
			return new ResponseEntity<>(new Response(Status.error, "Faculty not found"), HttpStatus.NOT_FOUND);
		}
		
		List<PostFacultyView> assignments = postRepo.getPostsCreatedBy(name);
		if (Objects.isNull(assignments) || assignments.size()==0) {
			return new ResponseEntity<>(new Response(Status.success, "No scheduled posts"), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Status.success, assignments), HttpStatus.OK);
	}
	
}
