package com.anshul.examportal.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.examportal.faculty.Faculty;
import com.anshul.examportal.faculty.FacultyRepo;
import com.anshul.examportal.student.Student;
import com.anshul.examportal.student.StudentRepo;


@CrossOrigin//(origins ="https://angular-exam-portal.web.app")
@RestController
public class Admin_Controller {
	
	@Autowired
	private Admin_Repo aRepo;
	@Autowired
	private FacultyRepo fRepo;
	@Autowired
	private StudentRepo sRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	/*
	@PostMapping(path="/add_admin", consumes= {"application/json"})
	public ResponseEntity<String> addAdmin(@RequestBody Admin a) {
		try {
			a.setPassword(passwordEcorder.encode(a.getPassword()));
			Admin admin = aRepo.save(a);
			return new ResponseEntity<>(admin.getUser_name(), HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	*/
	
	@PostMapping(path="/admin_login", consumes= {"application/json"})
	public ResponseEntity<List<String>> checkAdminLogin(@RequestBody Admin a) {
		List<String> list = new ArrayList<>(2);
		list.add("ADMIN");
		list.add("false");
		try {
			Admin admin = aRepo.getOne(a.getUser_email());
			if(passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				list.set(1, admin.getUser_name());
				list.add(admin.getUser_email());
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
	
	@PostMapping(path="/change_password/ADMIN", consumes= {"application/json"})
	public ResponseEntity<List<String>> changePassword(@RequestBody Map<?, ?> a){
		List<String> list = new ArrayList<>();
		try {
			Admin admin = aRepo.getOne((String)a.get("email"));
			if(passwordEcorder.matches((String)a.get("password"), admin.getPassword())) {
				admin.setPassword(passwordEcorder.encode((String)a.get("newPassword")));
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
	
	@GetMapping("/trial")
	public List<Faculty> trialCheck(){
		List<Faculty> list = new ArrayList<>();
		
		Faculty f1 = new Faculty("abc@gmail.com", "abc", "123456");
		Faculty f2 = new Faculty("xyz@gmail.com", "xyz", "987654");
		
		list.add(f1);
		list.add(f2);
		
		return list;
	}
	
	@GetMapping("/faculties")
	public ResponseEntity<List<Faculty>> getFaculties(){
		List<Faculty> list = fRepo.findAll();
		
		if(list.size()==0)
			return new ResponseEntity<>(list, HttpStatus.NO_CONTENT);
		
		for(Faculty f : list)
			f.setPassword(null);
		
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	@PostMapping(path="/change_faculty_access", consumes= {"application/json"})
	public ResponseEntity<List<String>> changeFacultyAccess(@RequestBody Faculty f) {
		List<String> list = new ArrayList<>();
		try {
			Faculty faculty = fRepo.getOne(f.getEmail());
			faculty.setIsAllowed(f.getIsAllowed());
			fRepo.save(faculty);
			list.add(f.getIsAllowed()==true ? "Access granted" : "Access denaid");
			return new ResponseEntity<>(list, HttpStatus.OK);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add_faculty", consumes= {"application/json"})
	public ResponseEntity<List<String>> addFaculty(@RequestBody Faculty f) {
		List<String> list = new ArrayList<>();
		
		if(fRepo.checkFacultyExists(f.getName(), f.getEmail()) >= 1) {
			list.add("Faculty already exists");
			return new ResponseEntity<>(list, HttpStatus.CONFLICT);
		}
		
		try {
			f.setPassword(passwordEcorder.encode(f.getPassword()));
			fRepo.save(f);
			list.add("Faculty added successfully");
			return new ResponseEntity<>(list, HttpStatus.CREATED);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add_faculties", consumes= {"application/json"})
	public ResponseEntity<List<String>> addFaculties(@RequestBody List<Faculty> faculties) {
		List<String> list = new ArrayList<>();
		
		for(Faculty f : faculties){
			if(fRepo.checkFacultyExists(f.getName(), f.getEmail()) >= 1) {
				list.add("Faculty with Email: " + f.getEmail() + " or Name: " + f.getName() + " already exists");
				return new ResponseEntity<>(list, HttpStatus.CONFLICT);
			}else {
				f.setPassword(passwordEcorder.encode(f.getPassword()));
			}
		}
		
		try {
			fRepo.saveAll(faculties);
			list.add("Faculties added successfully");
			return new ResponseEntity<>(list, HttpStatus.CREATED);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/students")
	public ResponseEntity<List<Student>> getStudents(){
		List<Student> list = sRepo.findAll();
		
		if(list.size()==0)
			return new ResponseEntity<>(list, HttpStatus.NO_CONTENT);
		
		for(Student s : list)
			s.setPassword(null);
		
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	@DeleteMapping("/remove/student/{email}")
	public ResponseEntity<List<String>> deleteStudent(@PathVariable("email") String email) {
		List<String> list = new ArrayList<>();
		try {
			Student s = sRepo.getOneByEmail(email);
			sRepo.deleteFromMCQ(s.getRollNo());
			sRepo.deleteFromSubjective(s.getRollNo());
			sRepo.delete(s);
			list.add("Student record deleted successfully");
			return new ResponseEntity<>(list, HttpStatus.OK);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add_student", consumes= {"application/json"})
	public ResponseEntity<List<String>> addStudent(@RequestBody Student s) {
		List<String> list = new ArrayList<>();
		
		if(sRepo.checkStudentExists(s.getRollNo(), s.getEmail()) >= 1) {
			list.add("Student already exists");
			return new ResponseEntity<>(list, HttpStatus.CONFLICT);
		}
		
		try {
			s.setPassword(passwordEcorder.encode(s.getPassword()));
			sRepo.save(s);
			list.add("Student added successfully");
			return new ResponseEntity<>(list, HttpStatus.CREATED);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/add_students", consumes= {"application/json"})
	public ResponseEntity<List<String>> addStudents(@RequestBody List<Student> students) {
		List<String> list = new ArrayList<>();
		
		for(Student s : students){
			if(sRepo.checkStudentExists(s.getRollNo(), s.getEmail()) >= 1) {
				list.add("Faculty with Email: " + s.getRollNo() + " or Name: " + s.getEmail() + " already exists");
				return new ResponseEntity<>(list, HttpStatus.CONFLICT);
			}else {
				s.setPassword(passwordEcorder.encode(s.getPassword()));
			}
		}
		
		try {
			sRepo.saveAll(students);
			list.add("Students added successfully");
			return new ResponseEntity<>(list, HttpStatus.CREATED);
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
