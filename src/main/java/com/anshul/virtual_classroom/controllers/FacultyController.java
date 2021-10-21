package com.anshul.virtual_classroom.controllers;

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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.virtual_classroom.entity.Faculty;
import com.anshul.virtual_classroom.entity.MCQTest;
import com.anshul.virtual_classroom.entity.Student;
import com.anshul.virtual_classroom.entity.SubjectiveAnswer;
import com.anshul.virtual_classroom.entity.SubjectiveTest;
import com.anshul.virtual_classroom.entity.Test;
import com.anshul.virtual_classroom.repos.FacultyRepo;
import com.anshul.virtual_classroom.repos.MCQAnswerRepo;
import com.anshul.virtual_classroom.repos.MCQTestRepo;
import com.anshul.virtual_classroom.repos.StudentRepo;
import com.anshul.virtual_classroom.repos.SubjectiveAnswerRepo;
import com.anshul.virtual_classroom.repos.SubjectiveTestRepo;
import com.anshul.virtual_classroom.repos.TestRepo;
import com.anshul.virtual_classroom.utility.MCQTestData;
import com.anshul.virtual_classroom.utility.MCQTestResult;
import com.anshul.virtual_classroom.utility.PastTests;
import com.anshul.virtual_classroom.utility.SubjectiveTestData;
import com.anshul.virtual_classroom.utility.SubjectiveTestResult;


@CrossOrigin//(origins ="http://localhost:4500")
@RestController
@RequestMapping("faculty")
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
	private SubjectiveTestRepo subRepo;
	@Autowired
	private MCQAnswerRepo mcqAnsRepo;
	@Autowired
	private SubjectiveAnswerRepo subAnsRepo;
	@Autowired
	private JavaMailSender mailSender;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="/login", consumes= {"application/json"})
	public ResponseEntity<List<String>> checkFacultyLogin(@RequestBody Faculty f) {
		List<String> list = new ArrayList<>();
		list.add("FACULTY");
		
		try {
			Faculty faculty = fRepo.getOne(f.getEmail());
			if(faculty.isAllowed()) {
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
	
	@PostMapping(path="/change_password", consumes= {"application/json"})
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
	public ResponseEntity<?> addTest(@RequestBody Test t) {
		Map<String, Object> m = new HashMap<>();
		try{
			Test test = tRepo.save(t);
			m.put("TestId", test.getTestId());
			return new ResponseEntity<>(m, HttpStatus.CREATED);
		}catch(Exception e){
			m.put("Error", e.getMessage());
			return new ResponseEntity<>(m, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create_mcq_test", consumes = {"application/json"})
	public ResponseEntity<?> addMCQTest(@RequestBody List<MCQTest> m_test) {
		for(MCQTest mt : m_test) 
			mt.setQuestionId(mt.getTestId() + "-" + mt.getQuestionId());
		
		Map<String, String> m = new HashMap<>();
		try {
			mcqRepo.saveAll(m_test);
			sendMails(m_test.get(0).getTestId());
			m.put("Message", "Created Successfully");
			return new ResponseEntity<>(m, HttpStatus.CREATED);
		}catch (Exception e) {
			m.put("Error", e.getMessage());
			return new ResponseEntity<>(m, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create_sub_test", consumes = {"application/json"})
	public ResponseEntity<?> addSUBTest(@RequestBody List<SubjectiveTest> s_test) {
		for(SubjectiveTest st : s_test) 
			st.setQuestionId(st.getTestId() + "-" + st.getQuestionId());
		
		Map<String, String> m = new HashMap<>();
		try {
			subRepo.saveAll(s_test);
			sendMails(s_test.get(0).getTestId());
			m.put("Message", "Created Successfully");
			return new ResponseEntity<>(m, HttpStatus.CREATED);
		}catch (Exception e) {
			m.put("Error", e.getMessage());
			return new ResponseEntity<>(m, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private void sendMails(int testId) {
		Test t = tRepo.getByTestId(testId);
		String mailBody = "Title: " + t.getTitle() + "\n" +
				"Schedule On: " + t.getScheduleOn() + "\n" +
				"Test ID: " + t.getTestId() + "\n" + 
				"Password: " + t.getPassword() + "\n";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom("adgamesindia@gmail.com");
		mailMessage.setSubject("New test from " + t.getCreatedBy());
		mailMessage.setText(mailBody);
		
		List<Student> students = sRepo.findBySemAndBranchAndSection(t.getSem(), t.getBranch(), t.getSection());
		for(Student s : students) {
			mailMessage.setTo(s.getEmail());
			mailSender.send(mailMessage);
		}
	}
	
	@GetMapping("/past_tests/{faculty}")
	public ResponseEntity<List<PastTests>> getFacultyPastTests(@PathVariable("faculty") String name){
		List<Test> list = tRepo.getPastTestsByFaculty(name);
		List<PastTests> pTests = new ArrayList<>();
		for(Test t : list)
			pTests.add(new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(), t.getResultOn()));
		
		return new ResponseEntity<>(pTests, HttpStatus.OK);
	}
	
	@GetMapping("/past_tests/attendance/{testId}")
	public ResponseEntity<List<Map<String, Object>>> getTestAttendance(@PathVariable("testId") int testId){
		List<Map<String, Object>> endResult = new ArrayList<>();
		
		try {
			Test test = tRepo.getOne(testId);
			List<Student> list = sRepo.findBySemAndBranchAndSection(test.getSem(), test.getBranch(), test.getSection());
			
			Map<String, Map<String, Object>> map = new HashMap<>();
			
			if(test.isSubjective()) {
				List<SubjectiveTestResult> results = subAnsRepo.getTestResult(testId);
				
				for(SubjectiveTestResult result : results) {
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
	
	@GetMapping("/past_tests/answer/{testId}/{rollNo}")
	public ResponseEntity<Map<?, ?>> getPastTestStudentAnswer(@PathVariable("testId") int testId, @PathVariable("rollNo") String rollNo){
		Map<String, Object> map = new HashMap<>();
		try {
			Student student = sRepo.getOne(rollNo);
			Test test = tRepo.getOne(testId);
			
			map.put("rollNo", student.getRollNo());
			map.put("name", student.getName());
			map.put("title", test.getTitle());
			map.put("subjectCode", test.getSubjectCode());
			map.put("isSubjective", test.isSubjective());
			
			if(test.isSubjective()) {
				Date currentDate = new Date(System.currentTimeMillis());
				Date resultDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(test.getResultOn());
				if(resultDate.after(currentDate))
					map.put("isEditable", true);
				else
					map.put("isEditable", false);
				
				List<SubjectiveTestData> ansList = subAnsRepo.getAnswers(testId, rollNo);
				map.put("ansList", ansList);
				
				int totalMarks = 0;
				for(int i = 0; i<ansList.size(); i++) {
					if(!(ansList.get(i).getScore()==-1))
						totalMarks += ansList.get(i).getScore();
				}
				map.put("totalMarks", totalMarks);
			}else {
				map.put("isEditable", false);
				
				List<MCQTestData> ansList = mcqAnsRepo.getAnswers(testId, rollNo);
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
	
	@PostMapping(path="/past_tests/check/{testId}/{rollNo}", consumes = {"application/json"})
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
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
