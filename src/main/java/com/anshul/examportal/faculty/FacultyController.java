package com.anshul.examportal.faculty;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.anshul.examportal.admin.ChangePassword;
import com.anshul.examportal.test.PastTests;
import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.TestRepo;
import com.anshul.examportal.test.mcq.MCQTest;
import com.anshul.examportal.test.mcq.MCQTestRepo;
import com.anshul.examportal.test.subjective.SubTestRepo;
import com.anshul.examportal.test.subjective.SubjectiveTest;


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
	public ResponseEntity<List<String>> checkFacultyLogin(@RequestBody Faculty f) {
		List<String> list = new ArrayList<>();
		list.add("FACULTY");
		try {
			Faculty faculty = fRepo.getOne(f.getEmail());
			if(faculty.getIsAllowed()) {
				if(passwordEcorder.matches(f.getPassword(), faculty.getPassword())) {
					list.add(faculty.getName());
					list.add(faculty.getEmail());
					return new ResponseEntity<>(list, HttpStatus.OK);
				}else {
					list.add("Incorrect Password");
					return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
				}
			}else {
				list.add("Your access is disabled");
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
	
	@PostMapping(path="/change_password/FACULTY", consumes= {"application/json"})
	public ResponseEntity<String> changePassword(@RequestBody ChangePassword a){
		try {
			Faculty faculty = fRepo.getOneByEmail(a.getEmail());
			if(passwordEcorder.matches(a.getPassword(), faculty.getPassword())) {
				faculty.setPassword(passwordEcorder.encode(a.getNewPassword()));
				fRepo.save(faculty);
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
	
	@GetMapping("/tests/{faculty}")
	public List<Test> getTests(@PathVariable("faculty") String name){
		List<Test> list = tRepo.getScheduledTestsByFaculty(name);
		return list;
	}
	
	@PostMapping(path="create_test", consumes = {"application/json"})
	public int addTest(@RequestBody Test t) {
		try{
			Test test = tRepo.save(t);
			return test.getTestId();
		}catch(Exception e){
			System.out.println(e.getMessage());
			return 0;
		}
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
	
	@GetMapping("/tests/past/{faculty}")
	public List<PastTests> getFacultyPastTests(@PathVariable("faculty") String name){
		List<Test> list = tRepo.getPastTestsByFaculty(name);
		List<PastTests> pTests = new ArrayList<>();
		for(Test t : list)
			pTests.add(new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.getIsSubjective(), t.getResultOn()));
		
		return pTests;
	}
	
}
