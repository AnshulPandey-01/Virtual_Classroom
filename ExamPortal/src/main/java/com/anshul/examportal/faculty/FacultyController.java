package com.anshul.examportal.faculty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.TestRepo;
import com.anshul.examportal.test.mcq_test.MCQTest;
import com.anshul.examportal.test.mcq_test.MCQTestRepo;
import com.anshul.examportal.test.sub_test.SubTestRepo;
import com.anshul.examportal.test.sub_test.SubjectiveTest;


@CrossOrigin//(origins ="http://localhost:4500")
@RestController
public class FacultyController {
	
	@Autowired
	private FacultyRepo fRepo;
	@Autowired
	private TestRepo tRepo;
	@Autowired
	private MCQTestRepo mcqRepo;
	@Autowired
	private SubTestRepo subRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="faculty_login", consumes= {"application/json"})
	public List<String> checkFacultyLogin(@RequestBody Faculty f) {
		
		List<String> list = new ArrayList<>(2);
		list.add("FACULTY");
		
		Faculty faculty = fRepo.getByEmail(f.getEmail());
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
		List<Test> list = tRepo.findByCreatedBy(name);
		return list;
	}
	
	
	@PostMapping(path="create_test", consumes = {"application/json"})
	public int addTest(@RequestBody Test t) {
		System.out.println(t.toString());
		Test test = tRepo.save(t);
		return test.getTestId();
	}
	
	@PostMapping(path="create_mcq_test", consumes = {"application/json"})
	public boolean addMCQTest(@RequestBody List<MCQTest> m_test) {
		for(MCQTest mt : m_test) 
			mt.setQuestionId(mt.getTestId() + "-" + mt.getQuestionId());
		
		try {
			mcqRepo.saveAll(m_test);
			return true;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="create_sub_test", consumes = {"application/json"})
	public boolean addSUBTest(@RequestBody List<SubjectiveTest> s_test) {
		for(SubjectiveTest st : s_test) 
			st.setQuestionId(st.getTestId() + "-" + st.getQuestionId());
		
		try {
			subRepo.saveAll(s_test);
			return true;
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
}
