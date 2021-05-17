package com.anshul.examportal.admin;

import java.util.ArrayList;
import java.util.List;

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


@CrossOrigin//(origins ="http://localhost:4500")
@RestController
public class Admin_Controller {
	
	@Autowired
	private Admin_Repo aRepo;
	@Autowired
	private FacultyRepo fRepo;
	@Autowired
	private StudentRepo sRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="add_admin", consumes= {"application/json"})
	public ResponseEntity<String> addAdmin(@RequestBody Admin a) {
		try {
			a.setPassword(passwordEcorder.encode(a.getPassword()));
			Admin admin = aRepo.save(a);
			return new ResponseEntity<>(admin.getUser_name(), HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="admin_login", consumes= {"application/json"})
	public ResponseEntity<List<String>> checkAdminLogin(@RequestBody Admin a) {
		List<String> list = new ArrayList<>(2);
		list.add("ADMIN");
		try {
			Admin admin = aRepo.getOne(a.getUser_email());
			if(passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				list.add(admin.getUser_name());
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
	public ResponseEntity<String> changePassword(@RequestBody ChangePassword a){
		try {
			Admin admin = aRepo.getOne(a.getEmail());
			if(passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				admin.setPassword(passwordEcorder.encode(a.getNewPassword()));
				aRepo.save(admin);
				return new ResponseEntity<>("Password Changed Successfully", HttpStatus.OK);
			}else {
				return new ResponseEntity<>("Incorrect Password", HttpStatus.UNAUTHORIZED);
			}
		}catch(EntityNotFoundException e) {
			return new ResponseEntity<>("Incorrect Email", HttpStatus.UNAUTHORIZED);
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
	public ResponseEntity<String> changeFacultyAccess(@RequestBody Faculty f) {
		try {
			Faculty faculty = fRepo.getOne(f.getEmail());
			faculty.setIsAllowed(f.getIsAllowed());
			fRepo.save(faculty);
			return new ResponseEntity<>("Access changed successfully", HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="add_faculty", consumes= {"application/json"})
	public ResponseEntity<Boolean> addFaculty(@RequestBody Faculty f) {
		if(fRepo.existsById(f.getEmail()))
			return new ResponseEntity<>(false, HttpStatus.CONFLICT);
		
		try {
			f.setPassword(passwordEcorder.encode(f.getPassword()));
			fRepo.save(f);
			return new ResponseEntity<>(true, HttpStatus.CREATED);
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
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
	public ResponseEntity<String> deleteStudent(@PathVariable("email") String email) {
		try {
			Student s = sRepo.getOneByEmail(email);
			sRepo.deleteFromMCQ(s.getRollNo());
			sRepo.deleteFromSubjective(s.getRollNo());
			sRepo.delete(s);
			return new ResponseEntity<>("Student record deleted successfully", HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="add_student", consumes= {"application/json"})
	public ResponseEntity<Boolean> addStudent(@RequestBody Student s) {
		if(sRepo.existsById(s.getRollNo()))
			return new ResponseEntity<>(false, HttpStatus.CONFLICT);
		try {
			s.setPassword(passwordEcorder.encode(s.getPassword()));
			sRepo.save(s);
			return new ResponseEntity<>(true, HttpStatus.CREATED);
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
