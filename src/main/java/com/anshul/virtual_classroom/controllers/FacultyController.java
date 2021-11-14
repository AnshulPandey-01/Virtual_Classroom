package com.anshul.virtual_classroom.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Respond;
import com.anshul.virtual_classroom.response.test.PastTests;
import com.anshul.virtual_classroom.utility.ChangePassword;
import com.anshul.virtual_classroom.utility.mcq.MCQData;
import com.anshul.virtual_classroom.utility.mcq.MCQTestData;
import com.anshul.virtual_classroom.utility.mcq.MCQTestResult;
import com.anshul.virtual_classroom.utility.subjective.SubjectiveTestData;
import com.anshul.virtual_classroom.utility.subjective.SubjectiveTestResult;

import javassist.NotFoundException;


@CrossOrigin
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
			Faculty faculty = fRepo.getById(f.getEmail());
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
	public ResponseEntity<List<String>> changePassword(@RequestBody ChangePassword a){
		List<String> list = new ArrayList<>();
		try {
			Faculty faculty = fRepo.getById((String)a.getEmail());
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
	
	@GetMapping("/{faculty}/tests")
	public ResponseEntity<Response> getTests(@PathVariable("faculty") String name){
		try {
			Faculty faculty = fRepo.findById(name).orElse(null);
			if(Objects.isNull(faculty)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Faculty not found"), HttpStatus.NOT_FOUND);
			}
			
			List<Test> tests = tRepo.getScheduledTestsByFaculty(name);
			if(Objects.isNull(tests) || tests.size()==0) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "No scheduled tests"), HttpStatus.NOT_FOUND);
			}
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), tests), HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create_test", consumes = {"application/json"})
	public ResponseEntity<Response> addTest(@RequestBody Test t) {
		Map<String, Integer> res = new HashMap<>();
		try{
			Test test = tRepo.save(t);
			res.put("TestId", test.getTestId());
			return new ResponseEntity<>(new Response(Respond.success.toString(), res), HttpStatus.CREATED);
		}catch(Exception e){
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create_mcq_test", consumes = {"application/json"})
	public ResponseEntity<Response> addMCQTest(@RequestBody List<MCQTest> m_test) {
		for(MCQTest mt : m_test) 
			mt.setQuestionId(mt.getTestId() + "-" + mt.getQuestionId());
		
		try {
			mcqRepo.saveAll(m_test);
			sendMails(m_test.get(0).getTestId());
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Created Successfully"), HttpStatus.CREATED);
		}catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create_sub_test", consumes = {"application/json"})
	public ResponseEntity<Response> addSUBTest(@RequestBody List<SubjectiveTest> s_test) {
		for(SubjectiveTest st : s_test) 
			st.setQuestionId(st.getTestId() + "-" + st.getQuestionId());
		
		try {
			subRepo.saveAll(s_test);
			sendMails(s_test.get(0).getTestId());
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Created Successfully"), HttpStatus.CREATED);
		}catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private void sendMails(int testId) throws NotFoundException {
		Test t = tRepo.findById(testId).orElse(null);
		if(Objects.isNull(t)) {
			throw new NotFoundException("Invalid test id");
		}
		
		String mailBody = "Title: " + t.getTitle() + "\n" +
				"Schedule On: " + t.getScheduleOn() + "\n" +
				"Test ID: " + t.getTestId() + "\n" + 
				"Password: " + t.getPassword() + "\n";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom("adgamesindia@gmail.com");
		mailMessage.setSubject("New test from " + t.getCreatedBy());
		mailMessage.setText(mailBody);
		
		List<Student> students = sRepo.findBySemAndBranchAndSection(t.getSem(), t.getBranch(), t.getSection());
		if(Objects.isNull(students) ||  students.size()==0) return ;
		
		for(Student s : students) {
			mailMessage.setTo(s.getEmail());
			mailSender.send(mailMessage);
		}
	}
	
	@GetMapping("/{faculty}/past_tests")
	public ResponseEntity<Response> getFacultyPastTests(@PathVariable("faculty") String name){
		Faculty faculty = fRepo.findById(name).orElse(null);
		if(Objects.isNull(faculty)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Faculty not found"), HttpStatus.NOT_FOUND);
		}
		
		List<Test> list = tRepo.getPastTestsByFaculty(name);
		if(Objects.isNull(list) || list.size()==0) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "No previous test record"), HttpStatus.NOT_FOUND);
		}
		
		List<PastTests> pTests = new ArrayList<>();
		for(Test t : list)
			pTests.add(new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(), t.getResultOn()));
		
		return new ResponseEntity<>(new Response(Respond.success.toString(), pTests), HttpStatus.OK);
	}
	
	@GetMapping("/past_tests/{testId}/attendance")
	public ResponseEntity<Response> getTestAttendance(@PathVariable("testId") int testId){		
		try {
			List<Map<String, Object>> endResult = new ArrayList<>();
			
			Test test = tRepo.getById(testId);
			if(Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
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
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), endResult), HttpStatus.OK);
		}catch(Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/past_tests/{testId}/answer/{rollNo}")
	public ResponseEntity<Response> getPastTestStudentAnswer(@PathVariable("testId") int testId, @PathVariable("rollNo") String rollNo){
		try {
			Map<String, Object> map = new HashMap<>();
			
			Student student = sRepo.findById(rollNo).orElse(null);
			if(Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid student roll no"), HttpStatus.NOT_FOUND);
			}
			
			Test test = tRepo.findById(testId).orElse(null);
			if(Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
			map.put("rollNo", student.getRollNo());
			map.put("name", student.getName());
			map.put("title", test.getTitle());
			map.put("subjectCode", test.getSubjectCode());
			map.put("subjective", test.isSubjective());
			
			if(test.isSubjective()) {
				Date currentDate = new Date(System.currentTimeMillis());
				Date resultDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(test.getResultOn());
				if(resultDate.after(currentDate))
					map.put("isEditable", true);
				else
					map.put("isEditable", false);
				
				map.put("maxMarks", subRepo.getNoOfQuestionsAndMaxMarks(testId).getMaxMarks());
				
				List<SubjectiveTestData> ansList = subAnsRepo.getAnswers(testId, rollNo);
				map.put("ansList", ansList);
				
				int score = 0;
				for(int i = 0; i<ansList.size(); i++) {
					if(!(ansList.get(i).getScore()==-1))
						score += ansList.get(i).getScore();
				}
				map.put("score", score);
			}else {
				map.put("isEditable", false);
				map.put("maxMarks", mcqRepo.getNoOfQuestions(testId) * test.getMarks());
				
				List<MCQTestData> list = mcqAnsRepo.getAnswers(testId, rollNo);
				
				List<MCQData> ansList = new ArrayList<>();
				int score = 0;
				for(int i = 0; i<list.size(); i++) {
					ansList.add(new MCQData(list.get(i)));
					if(list.get(i).getCorrectOption().equals(list.get(i).getAnswer()))
						score += test.getMarks();
					else if(list.get(i).getAnswer()!=null)
						score -= test.getNegativeMarks();
				}
				map.put("ansList", ansList);
				map.put("score", score);
			}
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), map), HttpStatus.OK);
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/past_tests/{testId}/check/{rollNo}", consumes = {"application/json"})
	public ResponseEntity<Response> markTheTest(@PathVariable("testId") int testId, @PathVariable("rollNo") String rollNo, @RequestBody List<SubjectiveAnswer> marksList) {		
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
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Updated Successfully"), HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
