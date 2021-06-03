package com.anshul.examportal.faculty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import com.anshul.examportal.student.Student;
import com.anshul.examportal.student.StudentRepo;
import com.anshul.examportal.test.PastTests;
import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.TestRepo;
import com.anshul.examportal.test.answer.MCQData;
import com.anshul.examportal.test.answer.MCQTestResult;
import com.anshul.examportal.test.answer.SubTestResult;
import com.anshul.examportal.test.answer.SubjectiveData;
import com.anshul.examportal.test.mcq.MCQTest;
import com.anshul.examportal.test.mcq.MCQTestRepo;
import com.anshul.examportal.test.mcq.answer.MCQAnswerRepo;
import com.anshul.examportal.test.subjective.SubTestRepo;
import com.anshul.examportal.test.subjective.SubjectiveTest;
import com.anshul.examportal.test.subjective.answer.SubAnswerRepo;
import com.anshul.examportal.test.subjective.answer.SubjectiveAnswer;


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
	public ResponseEntity<List<String>> changePassword(@RequestBody Map<?, ?> a){
		List<String> list = new ArrayList<>();
		try {
			Faculty faculty = fRepo.getOne((String)a.get("email"));
			if(passwordEcorder.matches((String)a.get("password"), faculty.getPassword())) {
				faculty.setPassword(passwordEcorder.encode((String)a.get("newPassword")));
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
	public ResponseEntity<List<Test>> getTests(@PathVariable("faculty") String name){
		try {
			List<Test> list = tRepo.getScheduledTestsByFaculty(name);
			return new ResponseEntity<>(list, HttpStatus.OK);
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
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
	public ResponseEntity<?> addMCQTest(@RequestBody List<MCQTest> m_test) {
		for(MCQTest mt : m_test) 
			mt.setQuestionId(mt.getTestId() + "-" + mt.getQuestionId());
		
		try {
			mcqRepo.saveAll(m_test);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create_sub_test", consumes = {"application/json"})
	public ResponseEntity<?> addSUBTest(@RequestBody List<SubjectiveTest> s_test) {
		for(SubjectiveTest st : s_test) 
			st.setQuestionId(st.getTestId() + "-" + st.getQuestionId());
		
		try {
			subRepo.saveAll(s_test);
			return new ResponseEntity<>(HttpStatus.CREATED);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
	public ResponseEntity<List<Map<String, Object>>> getTestAttendance(@PathVariable("testId") int testId){
		List<Map<String, Object>> endResult = new ArrayList<>();
		
		try {
			Test test = tRepo.getOne(testId);
			List<Student> list = sRepo.findBySemAndBranchAndSection(test.getSem(), test.getBranch(), test.getSection());
			
			Map<String, Map<String, Object>> map = new HashMap<>();
			
			if(test.getIsSubjective()) {
				List<SubTestResult> results = subAnsRepo.getTestResult(testId);
				
				for(SubTestResult result : results) {
					if(!map.containsKey(result.getRollNo())) {
						Map<String, Object> temp = new HashMap<>();
						temp.put("name", result.getName());
						temp.put("rollNo", result.getRollNo());
						temp.put("score", result.getScore());
						temp.put("isPresent", true);
						map.put(result.getRollNo(), temp);
					}else if(result.getScore()==-1 && (int)map.get(result.getRollNo()).get("score")!=-1){
						map.get(result.getRollNo()).put("score", result.getScore());
					}else if((int)map.get(result.getRollNo()).get("score")!=-1) {
						int prevScore = (int)map.get(result.getRollNo()).get("score");
						map.get(result.getRollNo()).put("score", prevScore + result.getScore());
					}
				}
				
				for(Student s : list) {
					if(map.containsKey(s.getRollNo()))
						endResult.add(map.get(s.getRollNo()));
					else {
						Map<String, Object> temp = new HashMap<>();
						temp.put("name", s.getName());
						temp.put("rollNo", s.getRollNo());
						temp.put("score", 0);
						temp.put("isPresent", false);
						endResult.add(temp);
					}
				}
				
			}else {
				List<MCQTestResult> results = mcqAnsRepo.getTestResult(testId);
				
				for(MCQTestResult result : results) {
					if(!map.containsKey(result.getRollNo())) {
						int score = result.getCorrectOption().equals(result.getAnswer()) ? test.getMarks() : test.getNegativeMarks();
						Map<String, Object> temp = new HashMap<>();
						temp.put("name", result.getName());
						temp.put("rollNo", result.getRollNo());
						temp.put("score", score);
						temp.put("isPresent", true);
						map.put(result.getRollNo(), temp);
					}else {
						int prevScore = (int)map.get(result.getRollNo()).get("score");
						int score = result.getCorrectOption().equals(result.getAnswer()) ? prevScore + test.getMarks() : prevScore - test.getNegativeMarks();
						map.get(result.getRollNo()).put("score", score);
					}
				}
				
				for(Student s : list) {
					if(map.containsKey(s.getRollNo()))
						endResult.add(map.get(s.getRollNo()));
					else {
						Map<String, Object> temp = new HashMap<>();
						temp.put("name", s.getName());
						temp.put("rollNo", s.getRollNo());
						temp.put("score", 0);
						temp.put("isPresent", false);
						endResult.add(temp);
					}
				}
				
			}
			
			return new ResponseEntity<>(endResult, HttpStatus.OK);
		}catch(EntityNotFoundException e) {
			Map<String, Object> m = new HashMap<>();
			m.put("Error", "Incorrect testId");
			endResult.add(m);
			return new ResponseEntity<>(endResult, HttpStatus.BAD_REQUEST);
		}catch(Exception e) {
			Map<String, Object> m = new HashMap<>();
			m.put("Error", e.getMessage());
			endResult.add(m);
			return new ResponseEntity<>(endResult, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/faculty/past_tests/answer/{testId}/{rollNo}")
	public ResponseEntity<Map<?, ?>> getPastTestStudentAnswer(@PathVariable("testId") int testId, @PathVariable("rollNo") String rollNo){
		Map<String, Object> map = new HashMap<>();
		try {
			Student student = sRepo.getOne(rollNo);
			Test test = tRepo.getOne(testId);
			
			map.put("rollNo", student.getRollNo());
			map.put("name", student.getName());
			map.put("title", test.getTitle());
			map.put("subjectCode", test.getSubjectCode());
			map.put("isSubjective", test.getIsSubjective());
			
			if(test.getIsSubjective()) {
				Date currentDate = new Date(System.currentTimeMillis());
				Date resultDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(test.getResultOn());
				if(resultDate.after(currentDate))
					map.put("isEditable", true);
				else
					map.put("isEditable", false);
				
				List<SubjectiveData> ansList = subAnsRepo.getAnswers(testId, rollNo);
				map.put("ansList", ansList);
				
				int totalMarks = 0;
				for(int i = 0; i<ansList.size(); i++) {
					if(!(ansList.get(i).getScore()==-1))
						totalMarks += ansList.get(i).getScore();
				}
				map.put("totalMarks", totalMarks);
			}else {
				map.put("isEditable", false);
				
				List<MCQData> ansList = mcqAnsRepo.getAnswers(testId, rollNo);
				map.put("ansList", ansList);
				
				int totalMarks = 0;
				for(int i = 0; i<ansList.size(); i++) {
					if(ansList.get(i).getCorrectOption().equals(ansList.get(i).getAnswer()))
						totalMarks += test.getMarks();
					else if(ansList.get(i).getAnswer()!=null)
						totalMarks -= test.getNegativeMarks();
				}
				map.put("totalMarks", totalMarks);
			}
			
			return new ResponseEntity<>(map, HttpStatus.OK);
		}catch(Exception e) {
			System.out.println(e.getMessage());
			map.put("error", e.getMessage());
			return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/faculty/past_tests/check/{testId}/{rollNo}", consumes = {"application/json"})
	public ResponseEntity<List<String>> markTheTest(@PathVariable("testId") int testId, @PathVariable("rollNo") String rollNo, @RequestBody List<SubjectiveAnswer> marksList) {
		List<String> list = new ArrayList<>();
		
		try {
			List<SubjectiveAnswer> ansList = subAnsRepo.findByRollNoAndTestId(rollNo, testId);
			
			Map<String, SubjectiveAnswer> map = new HashMap<>();
			for(SubjectiveAnswer ans : ansList) 
				map.put(ans.getQuestionId(), ans);
			
			for(SubjectiveAnswer marks : marksList) {
				SubjectiveAnswer ans = map.get(marks.getQuestionId());
				ans.setScore(marks.getScore());
			}
			
			List<SubjectiveAnswer> updatedList = new ArrayList<>(map.values());		
			subAnsRepo.saveAll(updatedList);
			
			list.add("Updated Successfully");
			return new ResponseEntity<>(list, HttpStatus.OK);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
