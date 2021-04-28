package com.anshul.examportal.faculty;

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

import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.Test_Repo;
import com.anshul.examportal.test.mcq_test.MCQ_Test;
import com.anshul.examportal.test.mcq_test.MCQ_Test_Repo;
import com.anshul.examportal.test.sub_test.Sub_Test_Repo;
import com.anshul.examportal.test.sub_test.Subjective_Test;


@CrossOrigin(origins ="http://localhost:4500")
@RestController
public class Faculty_Controller {
	
	@Autowired
	private Faculty_Repo fRepo;
	@Autowired
	private Test_Repo tRepo;
	@Autowired
	private MCQ_Test_Repo mRepo;
	@Autowired
	private Sub_Test_Repo sRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="faculty_login", consumes= {"application/json"})
	public List<String> checkFacultyLogin(@RequestBody Faculty f) {
		
		List<String> list = new ArrayList<>(2);
		list.add("FACULTY");
		
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
	
	@GetMapping("/tests/{faculty}")
	public List<Test> getTests(@PathVariable("faculty") String name){
		List<Test> list = tRepo.getByCreated_by(name);
		return list;
	}
	
	
	@PostMapping(path="create_test", consumes = {"application/json"})
	public int addTest(@RequestBody Test t) {
		Test test = tRepo.save(t);
		return test.getTest_id();
	}
	
	@PostMapping(path="create_mcq_test", consumes = {"application/json"})
	public boolean addMCQTest(@RequestBody List<MCQ_Test> m_test) {
		for(MCQ_Test mt : m_test) 
			mt.setQuestion_id(mt.getTest_id() + "-" + mt.getQuestion_id());
		
		try {
			mRepo.saveAll(m_test);
			return true;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="create_sub_test", consumes = {"application/json"})
	public boolean addSUBTest(@RequestBody List<Subjective_Test> s_test) {
		for(Subjective_Test st : s_test) 
			st.setQuestion_id(st.getTest_id() + "-" + st.getQuestion_id());
		
		try {
			sRepo.saveAll(s_test);
			return true;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
}
