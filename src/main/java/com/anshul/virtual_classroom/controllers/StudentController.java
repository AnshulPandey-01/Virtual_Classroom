package com.anshul.virtual_classroom.controllers;

import java.util.Date;
import java.util.HashMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.virtual_classroom.entity.MCQAnswer;
import com.anshul.virtual_classroom.entity.MCQTest;
import com.anshul.virtual_classroom.entity.Student;
import com.anshul.virtual_classroom.entity.SubjectiveAnswer;
import com.anshul.virtual_classroom.entity.Test;
import com.anshul.virtual_classroom.repos.MCQAnswerRepo;
import com.anshul.virtual_classroom.repos.MCQTestRepo;
import com.anshul.virtual_classroom.repos.StudentRepo;
import com.anshul.virtual_classroom.repos.SubjectiveAnswerRepo;
import com.anshul.virtual_classroom.repos.SubjectiveTestRepo;
import com.anshul.virtual_classroom.repos.TestRepo;
import com.anshul.virtual_classroom.utility.MCQTestData;
import com.anshul.virtual_classroom.utility.PastTests;
import com.anshul.virtual_classroom.utility.ScheduledTests;
import com.anshul.virtual_classroom.utility.SubjectiveTestData;
import com.anshul.virtual_classroom.utility.TestContainer;
import com.anshul.virtual_classroom.utility.TestDetails;
import com.anshul.virtual_classroom.utility.TestInfo;
import com.anshul.virtual_classroom.utility.TestStats;


@CrossOrigin//(origins ="http://localhost:4500")
@RestController
@RequestMapping("student")
public class StudentController {
	
	static final long ONE_MINUTE_IN_MILLIS = 60000;
	
	@Autowired
	private StudentRepo sRepo;
	@Autowired
	private TestRepo tRepo;
	@Autowired
	private MCQTestRepo mcqRepo;
	@Autowired
	private SubjectiveTestRepo subRepo;
	@Autowired
	private MCQAnswerRepo mAnsRepo;
	@Autowired
	private SubjectiveAnswerRepo sAnsRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="/login", consumes= {"application/json"})
	public ResponseEntity<List<String>> checkStudentLogin(@RequestBody Student s) {
		List<String> list = new ArrayList<>();
		list.add("STUDENT");
		
		try {
			Student student = sRepo.getOneByEmail(s.getEmail());
			if(student==null) {
				list.add("Incorrect Email");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			}else if(passwordEcorder.matches(s.getPassword(), student.getPassword())) {
				list.add(student.getName());
				list.add(student.getEmail());
				list.add(student.getRollNo());
				return new ResponseEntity<>(list, HttpStatus.OK);
			}else {
				list.add("Incorrect Password");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			}
		}catch(Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/change_password", consumes= {"application/json"})
	public ResponseEntity<List<String>> changePassword(@RequestBody Map<?, ?> a){
		List<String> list = new ArrayList<>();
		try {
			Student student = sRepo.getOne((String)a.get("email"));
			if(passwordEcorder.matches((String)a.get("password"), student.getPassword())) {
				student.setPassword(passwordEcorder.encode((String)a.get("newPassword")));
				sRepo.save(student);
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
	
	@GetMapping("/tests/{rollNo}")
	public ResponseEntity<List<ScheduledTests>> getStudentTests(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.getOne(rollNo);
		List<Test> tests = tRepo.getUpComingTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<ScheduledTests> s = new ArrayList<>();
		for(Test t : tests) {
			if(! (sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) ) {
				ScheduledTests st = new ScheduledTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(), t.getDuration(), t.getScheduleOn(), t.getResultOn(), t.getNegativeMarks());
				
				if(t.isSubjective()) {
					TestDetails td = subRepo.getNoOfQuestionsAndMaxMarks(t.getTestId());
					st.setNoOfQuestions(td.getNoOfQuestions());
					st.setTotalMarks(td.getTotalMarks());
				}else {
					st.setNoOfQuestions(mcqRepo.getNoOfQuestions(t.getTestId()));
					st.setTotalMarks(st.getNoOfQuestions() * t.getMarks());
				}
				s.add(st);
			}
		}
		
		return new ResponseEntity<>(s, HttpStatus.OK);
	}
	
	private boolean testTimeCheck(String dateTime, int duration){
		try {
			//System.out.println(dateTime + " | " + duration);
			
			Date scheduleTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateTime);
			Date endTime = new Date(scheduleTime.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			Date currentTime = new Date(System.currentTimeMillis());
			
			//System.out.println(currentTime + " | " + scheduleTime + " | " + endTime);
			
			return currentTime.before(endTime);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="/authenticate_test", consumes= {"application/json"})
	public ResponseEntity<List<Boolean>> checkTest(@RequestBody Test t) throws ParseException {
		List<Boolean> list = new ArrayList<>();
		try {
			Test test = tRepo.getOne(t.getTestId());
			
			if(test.getPassword().equals(t.getPassword()) && checkTestTime(test.getScheduleOn(), test.getDuration())) {
				list.add(true);
				list.add(test.isSubjective());
				return new ResponseEntity<>(list, HttpStatus.OK);
			}else {
				list.add(false);
				list.add(null);
				return new ResponseEntity<>(list, HttpStatus.FORBIDDEN);
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
			list.add(false);
			list.add(null);
			return new ResponseEntity<>(list, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/test/{testId}")
	public ResponseEntity<List<TestContainer>> getTestQuestions(@PathVariable("testId") int testId){
		Test t = tRepo.getByTestId(testId);
		
		if(checkTestTime(t.getScheduleOn(), t.getDuration())) {
			List<TestContainer> test;
			if(t.isSubjective()) {
				test = subRepo.findByTestId(testId);
			}else{
				test = mcqRepo.findByTestId(testId);
				for(int i = 0; i<test.size(); i++) {
					MCQTest mcq = (MCQTest)test.get(i);
					mcq.setCorrectOption(String.valueOf(t.getMarks()));
					test.set(i, mcq);
				}
			}
			return new ResponseEntity<>(test, HttpStatus.OK);
		}
		
		return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
	}
	
	private boolean checkTestTime(String dateTime, int duration){
		try {
			//System.out.println(dateTime + " | " + duration);
			
			Date currentDate = new Date(System.currentTimeMillis());
			Date scheduleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateTime);
			Date endDate = new Date(scheduleDate.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			
			//System.out.println(currentDate + " | " + scheduleDate + " | " + endDate);
			
			return scheduleDate.before(currentDate) && endDate.after(currentDate);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="/test/mcq_answer", consumes= {"application/json"})
	public ResponseEntity<List<Boolean>> submitMCQTest(@RequestBody List<MCQAnswer> answers) {
		List<Boolean> list = new ArrayList<>();
		try {
			mAnsRepo.saveAll(answers);
			list.add(true);
			return new ResponseEntity<>(list, HttpStatus.OK);
		}catch(Exception e) {
			System.out.println(e.getMessage());
			list.add(false);
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/test/sub_answer", consumes= {"application/json"})
	public ResponseEntity<List<Boolean>> submitSubTest(@RequestBody List<SubjectiveAnswer> answers) {
		List<Boolean> list = new ArrayList<>();
		try {
			sAnsRepo.saveAll(answers);
			list.add(true);
			return new ResponseEntity<>(list, HttpStatus.OK);
		}catch(Exception e) {
			System.out.println(e.getMessage());
			list.add(false);
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/tests/past/{rollNo}")
	public ResponseEntity<?> getStudentPastTests(@PathVariable("rollNo") String rollNo){
		try {
			Student student = sRepo.getOne(rollNo);
			List<Test> tests = tRepo.getPastTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
			
			List<PastTests> pTests = new ArrayList<>();
			
			for(Test t : tests) {
				if(mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) {
					PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(),  t.getResultOn(), true);
					pTests.add(pt);
				}else if(!testTimeCheck(t.getScheduleOn(), t.getDuration())) {
					PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(),  t.getResultOn(), false);
					pTests.add(pt);
				}
			}
			
			return new ResponseEntity<>(pTests, HttpStatus.OK);
		}catch(Exception e) {
			Map<String, String> m = new HashMap<>();
			m.put("searchResult", "Student not found");
			return new ResponseEntity<>(m, HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/test/{testId}/result_available")
	public ResponseEntity<List<Boolean>> isResultAvailable(@PathVariable("testId") int testId){
		List<Boolean> list = new ArrayList<>();
		
		Test t = tRepo.getByTestId(testId);
		if(t==null) {
			list.add(false);
			return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
		}
		
		boolean isResultDeclared = !testTimeCheck(t.getResultOn(), 0);
		list.add(isResultDeclared);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	@GetMapping("/test/{testId}/rollNo/{rollNo}")
	public ResponseEntity<TestInfo> getTestResult(@PathVariable("rollNo") String rollNo, @PathVariable("testId") int testId){
		Test t = tRepo.getByTestId(testId);
		TestInfo info = null;
		
		if(!testTimeCheck(t.getResultOn(), 0)) {
			if(t.isSubjective()) {
				TestDetails td = subRepo.getNoOfQuestionsAndMaxMarks(testId);
				info = new TestInfo(t.getTitle(), t.getCreatedBy(), t.getSubjectCode(), t.getScheduleOn(), t.getDuration(), td.getNoOfQuestions(), td.getTotalMarks(), -1);
				
				List<SubjectiveTestData> ansList = sAnsRepo.getAnswers(testId, rollNo);
				int total = 0;
				for(int i = 0; i<ansList.size(); i++) {
					total += ansList.get(i).getScore();
					info.ansData.add(ansList.get(i));
				}
				
				info.setTotalMarks(total);
			}else {
				int totalQuestions = mcqRepo.getNoOfQuestions(testId);
				info = new TestInfo(t.getTitle(), t.getCreatedBy(), t.getSubjectCode(), t.getScheduleOn(), t.getDuration(), totalQuestions, t.getMarks() * totalQuestions, t.getNegativeMarks());
				List<MCQTestData> ansList = mAnsRepo.getAnswers(testId, rollNo);
				
				int total = 0;
				for(int i = 0; i<ansList.size(); i++) {
					info.ansData.add(ansList.get(i));
					if(ansList.get(i).getCorrectOption().equals(ansList.get(i).getAnswer()))
						total += t.getMarks();
					else if(ansList.get(i).getAnswer()!=null)
						total -= t.getNegativeMarks();
				}
				
				info.setTotalMarks(total);
			}
		}
		
		return new ResponseEntity<>(info, HttpStatus.OK);
	}
	
	@GetMapping("/tests/stats")
	public ResponseEntity<TestStats> getTestStats(@RequestParam("rollNo") String rollNo,
			@RequestParam("subject") String subjectCode, @RequestParam("type") String type,
			@RequestParam("from") String from, @RequestParam("to") String to){
		System.out.println(rollNo + " | " + subjectCode + " | " + type + " | " + from + " | " + to);
		Student student = sRepo.getOne(rollNo);
		
		try {
			List<Test> tests = tRepo.getTestsByScheduleOnBetween(student.getBranch(), student.getSem(), student.getSection(), from, to);
			List<Test> requiredTests = new ArrayList<>();
			
			for(Test test : tests) {
				if(subjectCode.equals(test.getSubjectCode()) || subjectCode.equals("ALL")) {
					String testType = test.isSubjective() ? "subjective" : "mcq";
					if(type.equals(testType) || type.equals("both")) {
						requiredTests.add(test);
					}
				}
			}
			
			System.out.println(requiredTests.toString());
			TestStats response = new TestStats();
			
			// for user
			for(Test test : requiredTests) {
				if(test.isSubjective() && sAnsRepo.existsByRollNoAndTestId(rollNo, test.getTestId())) {
					TestDetails td = subRepo.getNoOfQuestionsAndMaxMarks(test.getTestId());
					int total = sAnsRepo.getTotalScore(test.getTestId(), rollNo);
					
					double percent = (double)total/td.getTotalMarks() * 100;
					
					response.getUserPercent().add(percent);
					response.getDates().add(test.getScheduleOn().split(" ")[0]);
					response.getIDs().add(test.getTestId());
					response.getTitles().add(test.getTitle());
				}else if(!test.isSubjective() && mAnsRepo.existsByRollNoAndTestId(rollNo, test.getTestId())) {
					List<MCQTestData> ansList = mAnsRepo.getAnswers(test.getTestId(), rollNo);
					int total = 0;
					for(MCQTestData data : ansList) {
						if(data.getCorrectOption().equals(data.getAnswer())) {
							total += test.getMarks();
						}else if(data.getAnswer()!=null) {
							total -= test.getNegativeMarks();
						}
					}
					
					int maxMarks = test.getMarks() * mcqRepo.getNoOfQuestions(test.getTestId());
					double percent = (double)total/maxMarks * 100;
					
					response.getUserPercent().add(percent);
					response.getDates().add(test.getScheduleOn().split(" ")[0]);
					response.getIDs().add(test.getTestId());
					response.getTitles().add(test.getTitle());
				}
			}
			
			// for all
			List<Student> students = sRepo.findBySemAndBranchAndSection(student.getSem(), student.getBranch(), student.getSection());
			for(Test test : requiredTests) {
				int total = 0, count = 0;
				for(Student s : students) {
					if(test.isSubjective() && sAnsRepo.existsByRollNoAndTestId(s.getRollNo(), test.getTestId())) {
						count++;
						total += sAnsRepo.getTotalScore(test.getTestId(), s.getRollNo());						
					}else if(!test.isSubjective() && mAnsRepo.existsByRollNoAndTestId(s.getRollNo(), test.getTestId())) {
						count++;
						List<MCQTestData> ansList = mAnsRepo.getAnswers(test.getTestId(), rollNo);
						for(MCQTestData data : ansList) {
							if(data.getCorrectOption().equals(data.getAnswer())) {
								total += test.getMarks();
							}else if(data.getAnswer()!=null) {
								total -= test.getNegativeMarks();
							}
						}
					}
				}
				
				if(count==0) continue;
				
				int maxMarksForAll = 0;
				if(test.isSubjective()) {
					maxMarksForAll = count * subRepo.getNoOfQuestionsAndMaxMarks(test.getTestId()).getTotalMarks();
				}else {
					maxMarksForAll = count * test.getMarks() * mcqRepo.getNoOfQuestions(test.getTestId());
				}
				double avgPercent = (double)total/maxMarksForAll * 100;
				response.getAveragePercent().add(avgPercent);
			}
			
			return new ResponseEntity<>(response, HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		
	}
	
}
