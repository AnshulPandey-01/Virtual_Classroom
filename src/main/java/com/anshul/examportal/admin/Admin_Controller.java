package com.anshul.examportal.admin;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.examportal.faculty.Faculty;
import com.anshul.examportal.faculty.FacultyRepo;
import com.anshul.examportal.student.Student;
import com.anshul.examportal.student.StudentRepo;


@CrossOrigin(origins ="http://localhost:4500")
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
	public boolean addAdmin(@RequestBody Admin a) {
		a.setPassword(passwordEcorder.encode(a.getPassword()));
		aRepo.save(a);
		return true;
	}
	
	@PostMapping(path="admin_login", consumes= {"application/json"})
	public List<String> checkAdminLogin(@RequestBody Admin a) {
		
		List<String> list = new ArrayList<>(2);
		list.add("ADMIN");
		
		Admin admin = aRepo.find(a.getUser_email());
		if(admin!=null) {
			if(passwordEcorder.matches(a.getPassword(), admin.getPassword())) {
				list.add(admin.getUser_name());
				return list;
			}
		}
		
		list.add("false");
		return list;
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
	public List<Faculty> getFaculties(){
		List<Faculty> list = fRepo.findAll();
		
		for(Faculty f : list)
			f.setPassword(null);
		
		return list;
	}
	
	@GetMapping("/remove/faculty/{email}")
	public boolean deleteFaculty(@PathVariable("email") String email) {
		try {
			fRepo.deleteById(email);
			return true;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="add_faculty", consumes= {"application/json"})
	public boolean addFaculty(@RequestBody Faculty f) {
		if(fRepo.existsById(f.getEmail())) return false;
		
		try {
			f.setPassword(passwordEcorder.encode(f.getPassword()));
			fRepo.save(f);
			return true;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@GetMapping("/students")
	public List<Student> getStudents(){
		List<Student> list = sRepo.findAll();
		
		for(Student s : list)
			s.setPassword(null);
		
		return list;
	}
	
	@GetMapping("/remove/student/{email}")
	public boolean deleteStudent(@PathVariable("email") String email) {
		try {
			sRepo.deleteByEmail(email);
			return true;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="add_student", consumes= {"application/json"})
	public boolean addStudent(@RequestBody Student s) {
		if(sRepo.existsById(s.getRollNo())) return false;
		try {
			s.setPassword(passwordEcorder.encode(s.getPassword()));
			sRepo.save(s);
			return true;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
}
