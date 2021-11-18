package com.anshul.virtual_classroom.controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.virtual_classroom.entity.MCQAnswer;
import com.anshul.virtual_classroom.entity.MCQTest;
import com.anshul.virtual_classroom.entity.Student;
import com.anshul.virtual_classroom.entity.SubjectiveAnswer;
import com.anshul.virtual_classroom.entity.SubjectiveTest;
import com.anshul.virtual_classroom.entity.Test;
import com.anshul.virtual_classroom.repos.MCQAnswerRepo;
import com.anshul.virtual_classroom.repos.MCQTestRepo;
import com.anshul.virtual_classroom.repos.StudentRepo;
import com.anshul.virtual_classroom.repos.SubjectiveAnswerRepo;
import com.anshul.virtual_classroom.repos.SubjectiveTestRepo;
import com.anshul.virtual_classroom.repos.TestRepo;
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Respond;
import com.anshul.virtual_classroom.response.test.TestStats;
import com.anshul.virtual_classroom.utility.TimeUtilityService;
import com.anshul.virtual_classroom.utility.mcq.MCQTestInfo;
import com.anshul.virtual_classroom.utility.mcq.MCQTestResult;
import com.anshul.virtual_classroom.utility.subjective.SubjectiveTestResult;
import com.anshul.virtual_classroom.utility.test.TestContainer;
import com.anshul.virtual_classroom.utility.test.TestDetails;

import javassist.NotFoundException;

@CrossOrigin
@RestController
@RequestMapping("test")
public class TestController {
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private StudentRepo studentRepo;
	@Autowired
	private TestRepo testRepo;
	@Autowired
	private MCQTestRepo mcqTestRepo;
	@Autowired
	private SubjectiveTestRepo subTestRepo;
	@Autowired
	private MCQAnswerRepo mcqAnsRepo;
	@Autowired
	private SubjectiveAnswerRepo subAnsRepo;
	
	TimeUtilityService timeUtility;
	
	public TestController(TimeUtilityService timeUtility) {
		this.timeUtility = timeUtility;
	}
	
	@PostMapping(path="/create", consumes = {"application/json"})
	public ResponseEntity<Response> addTest(@RequestBody Test t) {
		Map<String, Integer> res = new HashMap<>();
		try {
			Test test = testRepo.save(t);
			res.put("TestId", test.getTestId());
			return new ResponseEntity<>(new Response(Respond.success.toString(), res), HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create/mcq", consumes = {"application/json"})
	public ResponseEntity<Response> addMCQTest(@RequestBody List<MCQTest> m_test) {
		if(!testRepo.existsById(m_test.get(0).getTestId())) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Please first create the test"), HttpStatus.NOT_FOUND);
		}
		
		if(mcqTestRepo.existsByTestId(m_test.get(0).getTestId()) || subTestRepo.existsByTestId(m_test.get(0).getTestId())) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Questions for this test already exists"), HttpStatus.CONFLICT);
		}
		
		for(MCQTest mt : m_test) 
			mt.setQuestionId(mt.getTestId() + "-" + mt.getQuestionId());
		
		try {
			// sendMails(m_test.get(0).getTestId());
			mcqTestRepo.saveAll(m_test);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Created Successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/create/subjective", consumes = {"application/json"})
	public ResponseEntity<Response> addSUBTest(@RequestBody List<SubjectiveTest> s_test) {
		if(!testRepo.existsById(s_test.get(0).getTestId())) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Please first create the test"), HttpStatus.NOT_FOUND);
		}
		
		if(mcqTestRepo.existsByTestId(s_test.get(0).getTestId()) || subTestRepo.existsByTestId(s_test.get(0).getTestId())) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Questions for this test already exists"), HttpStatus.CONFLICT);
		}
		
		for(SubjectiveTest st : s_test) 
			st.setQuestionId(st.getTestId() + "-" + st.getQuestionId());
		
		try {
			// sendMails(s_test.get(0).getTestId());
			subTestRepo.saveAll(s_test);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Created Successfully"), HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private void sendMails(int testId) throws NotFoundException {
		Test t = testRepo.findById(testId).orElse(null);
		if (Objects.isNull(t)) {
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
		
		List<Student> students = studentRepo.findBySemAndBranchAndSection(t.getSem(), t.getBranch(), t.getSection());
		if (Objects.isNull(students) ||  students.size()==0) return ;
		
		for (Student s : students) {
			mailMessage.setTo(s.getEmail());
			mailSender.send(mailMessage);
		}
	}

	@PostMapping(path="/authenticate", consumes= {"application/json"})
	public ResponseEntity<Response> checkTest(@RequestBody Test t) throws ParseException {
		try {
			Test test = testRepo.findById(t.getTestId()).orElse(null);
			if (Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
			if (timeUtility.checkTestTime(test.getScheduleOn(), test.getDuration())) {
				if (test.getPassword().equals(t.getPassword())) {
					return new ResponseEntity<>(new Response(Respond.success.toString(), test.isSubjective()), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(new Response(Respond.error.toString(), "Incorrect password"), HttpStatus.FORBIDDEN);
				}
			} else if (!timeUtility.testTimeCheck(test.getScheduleOn(), test.getDuration())) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Test has been finished"), HttpStatus.FORBIDDEN);
			} else {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Test hasn't started yet"), HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/{testId}")
	public ResponseEntity<Response> getTestQuestions(@PathVariable("testId") int testId){
		Test test = testRepo.findById(testId).orElse(null);
		if (Objects.isNull(test)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
		}
		
		if (timeUtility.checkTestTime(test.getScheduleOn(), test.getDuration())) {
			List<TestContainer> questions;
			if (test.isSubjective()) {
				questions = subTestRepo.findByTestId(testId);
			} else {
				questions = mcqTestRepo.findByTestId(testId);
				for (int i = 0; i<questions.size(); i++) {
					MCQTest mcq = (MCQTest)questions.get(i);
					mcq.setCorrectOption("-1");
					questions.set(i, mcq);
				}
			}
			return new ResponseEntity<>(new Response(Respond.success.toString(), questions), HttpStatus.OK);
		} else if (!timeUtility.testTimeCheck(test.getScheduleOn(), test.getDuration())) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Test has been finished"), HttpStatus.FORBIDDEN);
		} else {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Test is not started yet"), HttpStatus.FORBIDDEN);
		}
	}
	
	@PostMapping(path="/submit/mcq", consumes= {"application/json"})
	public ResponseEntity<Response> submitMCQTest(@RequestBody List<MCQAnswer> answers) {
		try {
			mcqAnsRepo.saveAll(answers);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Test submited successfully"), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/submit/subjective", consumes= {"application/json"})
	public ResponseEntity<Response> submitSubTest(@RequestBody List<SubjectiveAnswer> answers) {
		try {
			subAnsRepo.saveAll(answers);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Test submited successfully"), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/{testId}/result_available")
	public ResponseEntity<List<Boolean>> isResultAvailable(@PathVariable("testId") int testId){
		List<Boolean> list = new ArrayList<>();
		
		Test test = testRepo.findById(testId).orElse(null);
		if (Objects.isNull(test)) {
			list.add(false);
			return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
		}
		
		boolean isResultDeclared = !timeUtility.testTimeCheck(test.getResultOn(), 0);
		list.add(isResultDeclared);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	@GetMapping("/{testId}/attendance")
	public ResponseEntity<Response> getTestAttendance(@PathVariable("testId") int testId){		
		try {
			List<Map<String, Object>> endResult = new ArrayList<>();
			
			Test test = testRepo.findById(testId).orElse(null);
			if (Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
			List<Student> list = studentRepo.findBySemAndBranchAndSection(test.getSem(), test.getBranch(), test.getSection());
			
			Map<String, Map<String, Object>> map = new HashMap<>();
			
			if (test.isSubjective()) {
				List<SubjectiveTestResult> results = subAnsRepo.getTestResult(testId);
				
				for (SubjectiveTestResult result : results) {
					if (!map.containsKey(result.getRollNo())) {
						Map<String, Object> temp = new HashMap<>();
						temp.put("name", result.getName());
						temp.put("rollNo", result.getRollNo());
						temp.put("score", result.getScore());
						temp.put("isPresent", true);
						map.put(result.getRollNo(), temp);
					} else if (result.getScore()==-1 && (int)map.get(result.getRollNo()).get("score")!=-1) {
						map.get(result.getRollNo()).put("score", result.getScore());
					} else if((int)map.get(result.getRollNo()).get("score")!=-1) {
						int prevScore = (int)map.get(result.getRollNo()).get("score");
						map.get(result.getRollNo()).put("score", prevScore + result.getScore());
					}
				}
				
				for (Student s : list) {
					if (map.containsKey(s.getRollNo())) {
						endResult.add(map.get(s.getRollNo()));
					} else {
						Map<String, Object> temp = new HashMap<>();
						temp.put("name", s.getName());
						temp.put("rollNo", s.getRollNo());
						temp.put("score", 0);
						temp.put("isPresent", false);
						endResult.add(temp);
					}
				}
				
			} else {
				List<MCQTestResult> results = mcqAnsRepo.getTestResult(testId);
				
				for (MCQTestResult result : results) {
					if (!map.containsKey(result.getRollNo())) {
						int score = result.getCorrectOption().equals(result.getAnswer()) ? test.getMarks() : test.getNegativeMarks();
						Map<String, Object> temp = new HashMap<>();
						temp.put("name", result.getName());
						temp.put("rollNo", result.getRollNo());
						temp.put("score", score);
						temp.put("isPresent", true);
						map.put(result.getRollNo(), temp);
					} else {
						int prevScore = (int)map.get(result.getRollNo()).get("score");
						int score = result.getCorrectOption().equals(result.getAnswer()) ? prevScore + test.getMarks() : prevScore - test.getNegativeMarks();
						map.get(result.getRollNo()).put("score", score);
					}
				}
				
				for (Student s : list) {
					if (map.containsKey(s.getRollNo())) {
						endResult.add(map.get(s.getRollNo()));
					} else {
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
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/stats")
	public ResponseEntity<Response> getTestStats(@RequestParam("rollNo") String rollNo,
			@RequestParam("subject") String subjectCode, @RequestParam("type") String type,
			@RequestParam("from") String from, @RequestParam("to") String to){
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		try {
			List<Test> tests = testRepo.getTestsByScheduleOnBetween(student.getBranch(), student.getSem(), student.getSection(), from, to);
			if (Objects.isNull(tests) || tests.size()==0) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "No test available in given range"), HttpStatus.NOT_FOUND);
			}
			
			List<Test> requiredTests = new ArrayList<>();
			
			for (Test test : tests) {
				if (subjectCode.equals(test.getSubjectCode()) || subjectCode.equals("ALL")) {
					String testType = test.isSubjective() ? "subjective" : "mcq";
					if (type.equals(testType) || type.equals("both")) {
						requiredTests.add(test);
					}
				}
			}
			
			TestStats response = new TestStats();
			
			for (Test test : requiredTests) {
				double percent, avgPercent;
				if (test.isSubjective()) {
					int count = subAnsRepo.getAllStudents(test.getTestId());
					if (count==0) {
						percent = 0;
						avgPercent = 0;
					} else {
						TestDetails td = subTestRepo.getNoOfQuestionsAndMaxMarks(test.getTestId());
						
						Integer total = subAnsRepo.getTotalScore(test.getTestId(), rollNo);
						total = total == null ? 0 : total;
						percent = (double)total/td.getMaxMarks() * 100;
						
						Integer allScore = subAnsRepo.getSumOfAllStudentsScore(test.getTestId());
						allScore = allScore == null ? 0 : allScore;
						avgPercent = (double)allScore/(count * td.getMaxMarks()) * 100;
					}
				} else {
					int count = mcqAnsRepo.getAllStudents(test.getTestId());
					if (count==0) {
						percent = 0;
						avgPercent = 0;
					} else {
						int allScore = 0, score = 0;
						int noOfQuestions = mcqTestRepo.getNoOfQuestions(test.getTestId());
						int maxMarks = test.getMarks() * noOfQuestions;
						
						List<MCQTestInfo> allAnsList = mcqAnsRepo.getAllStudentsAnswers(test.getTestId());
						for (MCQTestInfo data : allAnsList) {
							if (data.getCorrectOption().equals(data.getAnswer())) {
								score += data.getRollNo().equals(rollNo) ? test.getMarks() : 0;
								allScore += test.getMarks();
							} else if (data.getAnswer()!=null) {
								score -= data.getRollNo().equals(rollNo) ? test.getNegativeMarks() : 0;
								allScore -= test.getNegativeMarks();
							}
						}
						
						percent = (double)score/maxMarks * 100;
						avgPercent = (double)allScore/(count * maxMarks) * 100;
					}
				}
				
				response.getUserPercent().add(percent);
				response.getAveragePercent().add(avgPercent);
				response.getDates().add(test.getScheduleOn().split(" ")[0]);
				response.getIDs().add(test.getTestId());
				response.getTitles().add(test.getTitle());
			}
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), response), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.NOT_FOUND);
		}
		
	}
	
}
