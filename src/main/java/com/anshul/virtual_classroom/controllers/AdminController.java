package com.anshul.virtual_classroom.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import com.anshul.virtual_classroom.response.Response.Status;
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
	
	private BCryptPasswordEncoder passwordEcorder;
	
	public AdminController() {
		this.passwordEcorder = new BCryptPasswordEncoder();
	}
	
	@PostMapping(path="/add/admin", consumes= {"application/json"})
	public ResponseEntity<Response<String>> addAdmin(@RequestBody Admin a) {
		try {
			a.setPassword(passwordEcorder.encode(a.getPassword()));
			Admin admin = aRepo.save(a);
			return Response.SuccessResponse(admin.getUsername());
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/login", consumes= {"application/json"})
	public ResponseEntity<Response<List<String>>> checkAdminLogin(@RequestBody Admin a) {
		List<String> list = new ArrayList<>(2);
		list.add("ADMIN");
		list.add("false");
		try {
			Admin admin = aRepo.getById(a.getEmail());
			if (passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				list.set(1, admin.getUsername());
				list.add(admin.getEmail());
				return Response.SuccessResponse(list);
			}
			
			list.add("Incorrect Password");
			return new ResponseEntity<>(new Response(Status.error, list), HttpStatus.UNAUTHORIZED);
		} catch (EntityNotFoundException e) {
			list.add("Incorrect Email");
			return new ResponseEntity<>(new Response(Status.error, list), HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(new Response(Status.error, list), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/change_password", consumes= {"application/json"})
	public ResponseEntity<Response<List<String>>> changePassword(@RequestBody ChangePassword a) {
		List<String> list = new ArrayList<>();
		try {
			Admin admin = aRepo.getById(a.getEmail());
			if (passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				admin.setPassword(passwordEcorder.encode(a.getNewPassword()));
				aRepo.save(admin);
				list.add("Password Changed Successfully");
				return Response.SuccessResponse(list);
			} else {
				list.add("Incorrect Password");
				return new ResponseEntity<>(new Response(Status.error, list), HttpStatus.UNAUTHORIZED);
			}
		} catch (EntityNotFoundException e) {
			list.add("Incorrect Email");
			return new ResponseEntity<>(new Response(Status.error, list), HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(new Response(Status.error, list), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/all/faculties")
	public ResponseEntity<Response<List<Faculty>>> getFaculties() {
		List<Faculty> list = fRepo.findAll();
		
		if (list.isEmpty())
			return Response.SuccessResponse(list);
		
		for (Faculty f : list)
			f.setPassword(null);
		
		return Response.SuccessResponse(list);
	}
	
	@PostMapping(path="/change_faculty_access", consumes= {"application/json"})
	public ResponseEntity<Response<String>> changeFacultyAccess(@RequestBody Faculty f) {
		try {
			Faculty faculty = fRepo.findById(f.getEmail()).orElse(null);
			if (Objects.isNull(faculty)) {
				return new ResponseEntity<>(new Response(Status.error, "Faculty not found"), HttpStatus.NOT_FOUND);
			}
			faculty.setAllowed(f.isAllowed());
			fRepo.save(faculty);
			String res = f.isAllowed()==true ? "Access granted" : "Access denied";
			return Response.SuccessResponse(res);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add/faculty", consumes= {"application/json"})
	public ResponseEntity<Response<String>> addFaculty(@RequestBody Faculty f) {
		if (fRepo.checkFacultyExists(f.getName(), f.getEmail()) >= 1) {
			return new ResponseEntity<>(new Response(Status.error, "Faculty already exists"), HttpStatus.CONFLICT);
		}
		
		try {
			f.setPassword(passwordEcorder.encode(f.getPassword()));
			fRepo.save(f);
			return new ResponseEntity<>(new Response(Status.success, "Faculty added successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add/faculties", consumes= {"application/json"})
	public ResponseEntity<Response<String>> addFaculties(@RequestBody List<Faculty> faculties) {
		for (Faculty f : faculties) {
			if(fRepo.checkFacultyExists(f.getName(), f.getEmail()) >= 1) {
				String res = "Faculty with Email: " + f.getEmail() + " or Name: " + f.getName() + " already exists";
				return new ResponseEntity<>(new Response(Status.error, res), HttpStatus.CONFLICT);
			} else {
				f.setPassword(passwordEcorder.encode(f.getPassword()));
			}
		}
		
		try {
			fRepo.saveAll(faculties);
			return new ResponseEntity<>(new Response(Status.success, "Faculties added successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/all/students")
	public ResponseEntity<Response<List<Student>>> getStudents() {
		List<Student> list = sRepo.findAll();
		
		if (list.isEmpty())
			return Response.SuccessResponse(list);
		
		for (Student s : list)
			s.setPassword(null);
		
		return Response.SuccessResponse(list);
	}
	
	@PostMapping(path="/add/student", consumes= {"application/json"})
	public ResponseEntity<Response<String>> addStudent(@RequestBody Student s) {
		if (sRepo.checkStudentExists(s.getRollNo(), s.getEmail()) >= 1) {
			return new ResponseEntity<>(new Response(Status.error, "Student already exists"), HttpStatus.CONFLICT);
		}
		
		try {
			s.setPassword(passwordEcorder.encode(s.getPassword()));
			sRepo.save(s);
			return new ResponseEntity<>(new Response(Status.success, "Student added successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add/students", consumes= {"application/json"})
	public ResponseEntity<Response<String>> addStudents(@RequestBody List<Student> students) {
		for (Student s : students){
			if (sRepo.checkStudentExists(s.getRollNo(), s.getEmail()) >= 1) {
				String res = "Student with Email: " + s.getRollNo() + " or Roll No: " + s.getEmail() + " already exists";
				return new ResponseEntity<>(new Response(Status.error, res), HttpStatus.CONFLICT);
			} else {
				s.setPassword(passwordEcorder.encode(s.getPassword()));
			}
		}
		
		try {
			sRepo.saveAll(students);
			return new ResponseEntity<>(new Response(Status.success, "Students added successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional
	@DeleteMapping("/delete/student")
	public ResponseEntity<Response<String>> deleteStudent(@RequestParam("email") String email) {
		try {
			Student s = sRepo.getOneByEmail(email);
			sRepo.deleteFromMCQ(s.getRollNo());
			sRepo.deleteFromSubjective(s.getRollNo());
			sRepo.delete(s);
			return Response.SuccessResponse("Student record deleted successfully");
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/all/branch_subjects")
	public ResponseEntity<Response<List<BranchSubjects>>> getBranchSubjects() {
		return Response.SuccessResponse(bsRepo.findAll());
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/add/branch_subjects", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<String>> addBranchSubjects(@RequestBody BranchSubjects bs) {
		if (bsRepo.existsById(bs.getBranchId())) {
			return new ResponseEntity<>(new Response(Status.error, "Branch already exists"), HttpStatus.CONFLICT);
		}
		
		bsRepo.save(bs);
		return Response.SuccessResponse("Branch and Subjects added successfully");
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/update/branch_subjects", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<String>> updateBranchSubjects(@RequestBody BranchSubjects bs) {
		if (bsRepo.existsById(bs.getBranchId())) {
			bsRepo.save(bs);
			return Response.SuccessResponse("Updated successfully");
		}
		
		return new ResponseEntity<>(new Response(Status.error, "Invalid Branch-Id"), HttpStatus.CONFLICT);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/delete/branch_subjects")
	public ResponseEntity<Response<String>> deleteBranch(@RequestParam("branchId") int branchId) {
		BranchSubjects brSubs = bsRepo.findById(branchId).orElse(null);
		if (brSubs==null) {
			return new ResponseEntity<>(new Response(Status.error, "Branch does not exists"), HttpStatus.NOT_FOUND);
		}
		bsRepo.delete(brSubs);
		return Response.SuccessResponse("Branch deleted successfully");
	}
}
