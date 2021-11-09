package com.anshul.virtual_classroom.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.virtual_classroom.entity.Admin;
import com.anshul.virtual_classroom.entity.BranchSubjects;
import com.anshul.virtual_classroom.entity.Faculty;
import com.anshul.virtual_classroom.entity.Student;
import com.anshul.virtual_classroom.repos.AdminRepo;
import com.anshul.virtual_classroom.repos.BranchSubjectsRepos;
import com.anshul.virtual_classroom.repos.FacultyRepo;
import com.anshul.virtual_classroom.repos.StudentRepo;
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Respond;
import com.anshul.virtual_classroom.utility.ChangePassword;

@CrossOrigin
@RestController
@RequestMapping("admin")
public class AdminController {
	
	@Autowired
	private AdminRepo aRepo;
	@Autowired
	private FacultyRepo fRepo;
	@Autowired
	private StudentRepo sRepo;
	@Autowired
	private BranchSubjectsRepos bsRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="/add/admin", consumes= {"application/json"})
	public ResponseEntity<String> addAdmin(@RequestBody Admin a) {
		try {
			a.setPassword(passwordEcorder.encode(a.getPassword()));
			Admin admin = aRepo.save(a);
			return new ResponseEntity<>(admin.getUsername(), HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/login", consumes= {"application/json"})
	public ResponseEntity<List<String>> checkAdminLogin(@RequestBody Admin a) {
		List<String> list = new ArrayList<>(2);
		list.add("ADMIN");
		list.add("false");
		try {
			Admin admin = aRepo.getById(a.getEmail());
			if(passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				list.set(1, admin.getUsername());
				list.add(admin.getEmail());
				return new ResponseEntity<>(list, HttpStatus.OK);
			}
			
			list.add("Incorrect Password");
			return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
		}catch(EntityNotFoundException e) {
			list.add("Incorrect Email");
			return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/change_password", consumes= {"application/json"})
	public ResponseEntity<List<String>> changePassword(@RequestBody ChangePassword a){
		List<String> list = new ArrayList<>();
		try {
			Admin admin = aRepo.getById(a.getEmail());
			if(passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				admin.setPassword(passwordEcorder.encode(a.getNewPassword()));
				aRepo.save(admin);
				list.add("Password Changed Successfully");
				return new ResponseEntity<>(list, HttpStatus.OK);
			}else {
				list.add("Incorrect Password");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			}
		}catch(EntityNotFoundException e) {
			list.add("Incorrect Email");
			return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/trial")
	public List<Faculty> trialCheck(){
		List<Faculty> list = new ArrayList<>();
		
		Faculty f1 = new Faculty("abc@gmail.com", "abc", "123456");
		Faculty f2 = new Faculty("xyz@gmail.com", "xyz", "987654");
		
		list.add(f1);
		list.add(f2);
		
		return list;
	}
	
	@GetMapping("/all/faculties")
	public ResponseEntity<Response> getFaculties(){
		List<Faculty> list = fRepo.findAll();
		
		if(list.size()==0)
			return new ResponseEntity<>(new Response(Respond.error.toString(), "No content"), HttpStatus.OK);
		
		for(Faculty f : list)
			f.setPassword(null);
		
		return new ResponseEntity<>(new Response(Respond.success.toString(), list), HttpStatus.OK);
	}
	
	@PostMapping(path="/change_faculty_access", consumes= {"application/json"})
	public ResponseEntity<Response> changeFacultyAccess(@RequestBody Faculty f) {
		try {
			Faculty faculty = fRepo.getById(f.getEmail());
			faculty.setAllowed(f.isAllowed());
			fRepo.save(faculty);
			String res = f.isAllowed()==true ? "Access granted" : "Access denaid";
			return new ResponseEntity<>(new Response(Respond.success.toString(), res), HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add/faculty", consumes= {"application/json"})
	public ResponseEntity<Response> addFaculty(@RequestBody Faculty f) {		
		if(fRepo.checkFacultyExists(f.getName(), f.getEmail()) >= 1) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Faculty already exists"), HttpStatus.CONFLICT);
		}
		
		try {
			f.setPassword(passwordEcorder.encode(f.getPassword()));
			fRepo.save(f);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Faculty added successfully"), HttpStatus.CREATED);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add/faculties", consumes= {"application/json"})
	public ResponseEntity<Response> addFaculties(@RequestBody List<Faculty> faculties) {		
		for(Faculty f : faculties){
			if(fRepo.checkFacultyExists(f.getName(), f.getEmail()) >= 1) {
				String res = "Faculty with Email: " + f.getEmail() + " or Name: " + f.getName() + " already exists";
				return new ResponseEntity<>(new Response(Respond.error.toString(), res), HttpStatus.CONFLICT);
			}else {
				f.setPassword(passwordEcorder.encode(f.getPassword()));
			}
		}
		
		try {
			fRepo.saveAll(faculties);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Faculties added successfully"), HttpStatus.CREATED);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/all/students")
	public ResponseEntity<Response> getStudents(){
		List<Student> list = sRepo.findAll();
		
		if(list.size()==0)
			return new ResponseEntity<>(new Response(Respond.error.toString(), "No content"), HttpStatus.OK);
		
		for(Student s : list)
			s.setPassword(null);
		
		return new ResponseEntity<>(new Response(Respond.success.toString(), list), HttpStatus.OK);
	}
	
	@Transactional
	@DeleteMapping("/delete/student")
	public ResponseEntity<Response> deleteStudent(@RequestParam("email") String email) {
		try {
			Student s = sRepo.getOneByEmail(email);
			sRepo.deleteFromMCQ(s.getRollNo());
			sRepo.deleteFromSubjective(s.getRollNo());
			sRepo.delete(s);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Student record deleted successfully"), HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add/student", consumes= {"application/json"})
	public ResponseEntity<Response> addStudent(@RequestBody Student s) {		
		if(sRepo.checkStudentExists(s.getRollNo(), s.getEmail()) >= 1) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Student already exists"), HttpStatus.CONFLICT);
		}
		
		try {
			s.setPassword(passwordEcorder.encode(s.getPassword()));
			sRepo.save(s);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Student added successfully"), HttpStatus.CREATED);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add/students", consumes= {"application/json"})
	public ResponseEntity<Response> addStudents(@RequestBody List<Student> students) {		
		for(Student s : students){
			if(sRepo.checkStudentExists(s.getRollNo(), s.getEmail()) >= 1) {
				String res = "Student with Email: " + s.getRollNo() + " or Roll No: " + s.getEmail() + " already exists";
				return new ResponseEntity<>(new Response(Respond.error.toString(), res), HttpStatus.CONFLICT);
			}else {
				s.setPassword(passwordEcorder.encode(s.getPassword()));
			}
		}
		
		try {
			sRepo.saveAll(students);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Students added successfully"), HttpStatus.CREATED);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/all/branch_subjects")
	public ResponseEntity<Response> getBranchSubjects(){
		return new ResponseEntity<>(new Response(Respond.success.toString(), bsRepo.findAll()), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/add/branch_subjects", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> addBranchSubjects(@RequestBody BranchSubjects bs){
		if(bsRepo.existsById(bs.getBranch())) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Branch already exists"), HttpStatus.CONFLICT);
		}
		
		bsRepo.save(bs);
		return new ResponseEntity<>(new Response(Respond.success.toString(), "Branch and Subjects added successfully"), HttpStatus.CREATED);
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.PUT, value = "/update/branch_subjects", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response> updateBranchSubjects(@RequestBody BranchSubjects bs, @RequestParam(value = "branch", required = false) String branch){
		if(branch==null) {
			BranchSubjects brSubs = bsRepo.getById(bs.getBranch());
			brSubs.setSubjects(bs.getSubjects());
			bsRepo.save(brSubs);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Subjects updated successfully"), HttpStatus.OK);
		}else {
			BranchSubjects brSubs = bsRepo.findById(branch).orElse(null);
			if(brSubs==null) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Branch does not exists"), HttpStatus.NOT_FOUND);
			}
			bsRepo.updateBranchAndSubjects(branch, bs.getBranch(), bs.subjectsToString());
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Branch and Subjects updated successfully"), HttpStatus.OK);
		}
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/delete/branch_subjects")
	public ResponseEntity<Response> deleteBranch(@RequestParam("branch") String branch){
		BranchSubjects brSubs = bsRepo.findById(branch).orElse(null);
		if(brSubs==null) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Branch does not exists"), HttpStatus.NOT_FOUND);
		}
		bsRepo.delete(brSubs);
		return new ResponseEntity<>(new Response(Respond.success.toString(), "Branch deleted successfully"), HttpStatus.OK);
	}
	
}
