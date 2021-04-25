package com.anshul.examportal.faculty;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.Test_Repo;


@CrossOrigin(origins ="http://localhost:4500")
@RestController
public class Faculty_Controller {
	
	@Autowired
	private Faculty_Repo fRepo;
	@Autowired
	private Test_Repo tRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="faculty_login", consumes= {"application/json"})
	public List<String> checkFacultyLogin(@RequestBody Faculty f) {
		
		List<String> list = new ArrayList<>(2);
		list.add("faculty");
		
		Faculty faculty = fRepo.find(f.getEmail());
		if(faculty!=null) {
			if(passwordEcorder.matches(f.getPassword(), faculty.getPassword())) {
				list.add(faculty.getName());
				return list;
			}
		}
		
		list.add("false");
		return list;
	}
	
	
	@PostMapping(path="create_test", consumes= {"application/json"})
	public int addTest(@RequestBody Test t) {
		t.setCreated_by("abc");
		Test test = tRepo.save(t);
		return test.getTest_id();
	}
}
