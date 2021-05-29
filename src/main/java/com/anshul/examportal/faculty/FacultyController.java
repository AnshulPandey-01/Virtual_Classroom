package com.anshul.examportal.faculty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.anshul.examportal.student.Student;
import com.anshul.examportal.student.StudentRepo;
import com.anshul.examportal.test.PastTests;
import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.TestRepo;
import com.anshul.examportal.test.answer.MCQData;
import com.anshul.examportal.test.answer.MCQTestResult;
import com.anshul.examportal.test.answer.SubTestResult;
import com.anshul.examportal.test.answer.TestInfo;
import com.anshul.examportal.test.answer.TestResultInfo;
import com.anshul.examportal.test.mcq.MCQTest;
import com.anshul.examportal.test.mcq.MCQTestRepo;
import com.anshul.examportal.test.mcq.answer.MCQAnswerRepo;
import com.anshul.examportal.test.subjective.SubTestRepo;
import com.anshul.examportal.test.subjective.SubjectiveTest;
import com.anshul.examportal.test.subjective.answer.SubAnswerRepo;


@CrossOrigin//(origins ="http://localhost:4500")
@RestController
public class FacultyController {
	
	@Autowired
	private FacultyRepo fRepo;
	@Autowired
	private StudentRepo sRepo;
	@Autowired
	private TestRepo tRepo;
	@Autowired
	private MCQTestRepo mcqRepo;
	@Autowired
	private SubTestRepo subRepo;
	@Autowired
	private MCQAnswerRepo mcqAnsRepo;
	@Autowired
	private SubAnswerRepo subAnsRepo;
	
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
	public ResponseEntity<List<String>> changePassword(@RequestBody ChangePassword a){
		List<String> list = new ArrayList<>();
		
		try {
			Faculty faculty = fRepo.getOneByEmail(a.getEmail());
			if(passwordEcorder.matches(a.getPassword(), faculty.getPassword())) {
				faculty.setPassword(passwordEcorder.encode(a.getNewPassword()));
				fRepo.save(faculty);
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
	
	@GetMapping("/tests/{faculty}")
	public List<Test> getTests(@PathVariable("faculty") String name){
		List<Test> list = tRepo.getScheduledTestsByFaculty(name);
		return list;
	}
	
	@PostMapping(path="/create_test", consumes = {"application/json"})
	public int addTest(@RequestBody Test t) {
		try{
			Test test = tRepo.save(t);
			return test.getTestId();
		}catch(Exception e){
			System.out.println(e.getMessage());
			return 0;
		}
	}
	
	@PostMapping(path="/create_mcq_test", consumes = {"application/json"})
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
	
	@PostMapping(path="/create_sub_test", consumes = {"application/json"})
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
	
	@GetMapping("/faculty/past_tests/{faculty}")
	public List<PastTests> getFacultyPastTests(@PathVariable("faculty") String name){
		List<Test> list = tRepo.getPastTestsByFaculty(name);
		List<PastTests> pTests = new ArrayList<>();
		for(Test t : list)
			pTests.add(new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.getIsSubjective(), t.getResultOn()));
		
		return pTests;
	}
	
	@GetMapping("/faculty/past_tests/attendance/{testId}")
	public List<TestResultInfo> getTestAttendance(@PathVariable("testId") int testId){
		List<TestResultInfo> endResult = new ArrayList<>();
		
		Test test = tRepo.getOne(testId);
		List<Student> list = sRepo.findBySemAndBranchAndSection(test.getSem(), test.getBranch(), test.getSection());
		
		Map<String, TestResultInfo> map = new HashMap<>();
		if(test.getIsSubjective()) {
			List<SubTestResult> results = subAnsRepo.getTestResult(testId);
			
			for(SubTestResult result : results) {
				if(!map.containsKey(result.getRollNo())) {
					map.put(result.getRollNo(), new TestResultInfo(result.getName(), result.getRollNo(), result.getScore(), true));
				}else if(result.getScore()==-1 && map.get(result.getRollNo()).getScore()!=-1){
					TestResultInfo trf = map.get(result.getRollNo());
					trf.setScore(result.getScore());
					map.put(result.getRollNo(), trf);
				}else if(map.get(result.getRollNo()).getScore()!=-1) {
					TestResultInfo trf = map.get(result.getRollNo());
					trf.setScore(trf.getScore()+ result.getScore());
					map.put(result.getRollNo(), trf);
				}
			}
			
			for(Student s : list) {
				if(map.containsKey(s.getRollNo()))
					endResult.add((TestResultInfo)map.get(s.getRollNo()));
				else
					endResult.add(new TestResultInfo(s.getName(), s.getRollNo(), 0, false));
			}
			
		}else {
			List<MCQTestResult> results = mcqAnsRepo.getTestResult(testId);
			
			for(MCQTestResult result : results) {
				if(!map.containsKey(result.getRollNo())) {
					int score = result.getCorrectOption().equals(result.getAnswer()) ? test.getMarks() : test.getNegativeMarks();
					map.put(result.getRollNo(), new TestResultInfo(result.getName(), result.getRollNo(), score, true));
				}else {
					TestResultInfo trf = map.get(result.getRollNo());
					int score = result.getCorrectOption().equals(result.getAnswer()) ? trf.getScore() + test.getMarks() : trf.getScore() - test.getNegativeMarks();
					trf.setScore(score);
					map.put(result.getRollNo(), trf);
				}
			}
			
			for(Student s : list) {
				if(map.containsKey(s.getRollNo()))
					endResult.add((TestResultInfo)map.get(s.getRollNo()));
				else
					endResult.add(new TestResultInfo(s.getName(), s.getRollNo(), 0, false));
			}
			
		}
		
		return endResult;
	}
	
}
