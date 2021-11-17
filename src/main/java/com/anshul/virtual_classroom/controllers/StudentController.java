package com.anshul.virtual_classroom.controllers;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import com.anshul.virtual_classroom.repos.BranchSubjectsRepos;
import com.anshul.virtual_classroom.repos.MCQAnswerRepo;
import com.anshul.virtual_classroom.repos.MCQTestRepo;
import com.anshul.virtual_classroom.repos.StudentRepo;
import com.anshul.virtual_classroom.repos.SubjectiveAnswerRepo;
import com.anshul.virtual_classroom.repos.SubjectiveTestRepo;
import com.anshul.virtual_classroom.repos.TestRepo;
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Respond;
import com.anshul.virtual_classroom.response.test.PastTests;
import com.anshul.virtual_classroom.response.test.ScheduledTests;
import com.anshul.virtual_classroom.response.test.TestInfo;
import com.anshul.virtual_classroom.response.test.TestStats;
import com.anshul.virtual_classroom.utility.ChangePassword;
import com.anshul.virtual_classroom.utility.mcq.MCQData;
import com.anshul.virtual_classroom.utility.mcq.MCQTestData;
import com.anshul.virtual_classroom.utility.mcq.MCQTestInfo;
import com.anshul.virtual_classroom.utility.subjective.SubjectiveTestData;
import com.anshul.virtual_classroom.utility.test.TestContainer;
import com.anshul.virtual_classroom.utility.test.TestDetails;

@CrossOrigin
@RestController
@RequestMapping("student")
public class StudentController {
	
	private static final long ONE_MINUTE_IN_MILLIS = 60000;
	
	@Autowired
	private BranchSubjectsRepos bsRepo;
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
			if (Objects.isNull(student)) {
				list.add("Incorrect Email");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			} else if(passwordEcorder.matches(s.getPassword(), student.getPassword())) {
				list.add(student.getName());
				list.add(student.getEmail());
				list.add(student.getRollNo());
				return new ResponseEntity<>(list, HttpStatus.OK);
			} else {
				list.add("Incorrect Password");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			}
		} catch (Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/change_password", consumes= {"application/json"})
	public ResponseEntity<List<String>> changePassword(@RequestBody ChangePassword a){
		List<String> list = new ArrayList<>();
		try {
			Student student = sRepo.getById(a.getEmail());
			if (passwordEcorder.matches(a.getPassword(), student.getPassword())) {
				student.setPassword(passwordEcorder.encode(a.getNewPassword()));
				sRepo.save(student);
				list.add("Password Changed Successfully");
				return new ResponseEntity<>(list, HttpStatus.OK);
			} else {
				list.add("Incorrect Password");
				return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
			}
		} catch (EntityNotFoundException e) {
			list.add("Incorrect Email");
			return new ResponseEntity<>(list, HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			list.add(e.getMessage());
			return new ResponseEntity<>(list, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/subjects")
	public ResponseEntity<Response> getStudentSubjects(@RequestParam("rollNo") String rollNo){
		Student student = sRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		String[] subjects = bsRepo.getOneByBranch(student.getBranch()).getSubjects();
		return new ResponseEntity<>(new Response(Respond.success.toString(), subjects), HttpStatus.OK);
	}
	
	@GetMapping("/{rollNo}/tests")
	public ResponseEntity<Response> getStudentTests(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		List<Test> tests = tRepo.getUpComingTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
		if (Objects.isNull(tests) || tests.size()==0) {
			return new ResponseEntity<>(new Response(Respond.success.toString(), "No scheduled tests"), HttpStatus.OK);
		}
		
		List<ScheduledTests> s = new ArrayList<>();
		for (Test t : tests) {
			if (! (sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) ) {
				ScheduledTests st = new ScheduledTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(), t.getDuration(), t.getScheduleOn(), t.getResultOn(), t.getNegativeMarks());
				
				if(t.isSubjective()) {
					TestDetails td = subRepo.getNoOfQuestionsAndMaxMarks(t.getTestId());
					st.setNoOfQuestions(td.getNoOfQuestions());
					st.setTotalMarks(td.getMaxMarks());
				} else {
					st.setNoOfQuestions(mcqRepo.getNoOfQuestions(t.getTestId()));
					st.setTotalMarks(st.getNoOfQuestions() * t.getMarks());
				}
				s.add(st);
			}
		}
		
		return new ResponseEntity<>(new Response(Respond.success.toString(), s), HttpStatus.OK);
	}
	
	private boolean testTimeCheck(String dateTime, int duration){
		try {
			//System.out.println(dateTime + " | " + duration);
			
			Date scheduleTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateTime);
			Date endTime = new Date(scheduleTime.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			Date currentTime = new Date(System.currentTimeMillis());
			
			//System.out.println(currentTime + " | " + scheduleTime + " | " + endTime);
			
			return currentTime.before(endTime);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="/authenticate_test", consumes= {"application/json"})
	public ResponseEntity<Response> checkTest(@RequestBody Test t) throws ParseException {
		try {
			Test test = tRepo.findById(t.getTestId()).orElse(null);
			if (Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
			if (checkTestTime(test.getScheduleOn(), test.getDuration())) {
				if (test.getPassword().equals(t.getPassword())) {
					return new ResponseEntity<>(new Response(Respond.success.toString(), test.isSubjective()), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(new Response(Respond.error.toString(), "Incorrect password"), HttpStatus.FORBIDDEN);
				}
			} else if (!testTimeCheck(test.getScheduleOn(), test.getDuration())) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Test has been finished"), HttpStatus.FORBIDDEN);
			} else {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Test hasn't started yet"), HttpStatus.FORBIDDEN);
			}
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Something went wrong"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/test/{testId}")
	public ResponseEntity<Response> getTestQuestions(@PathVariable("testId") int testId){
		Test test = tRepo.findById(testId).orElse(null);
		if (Objects.isNull(test)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
		}
		
		if (checkTestTime(test.getScheduleOn(), test.getDuration())) {
			List<TestContainer> questions;
			if(test.isSubjective()) {
				questions = subRepo.findByTestId(testId);
			} else{
				questions = mcqRepo.findByTestId(testId);
				for(int i = 0; i<questions.size(); i++) {
					MCQTest mcq = (MCQTest)questions.get(i);
					mcq.setCorrectOption("-1");
					questions.set(i, mcq);
				}
			}
			return new ResponseEntity<>(new Response(Respond.success.toString(), questions), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Respond.error.toString(), "Test is not started yet"), HttpStatus.FORBIDDEN);
	}
	
	private boolean checkTestTime(String dateTime, int duration){
		try {
			//System.out.println(dateTime + " | " + duration);
			
			Date currentDate = new Date(System.currentTimeMillis());
			Date scheduleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateTime);
			Date endDate = new Date(scheduleDate.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			
			//System.out.println(currentDate + " | " + scheduleDate + " | " + endDate);
			
			return scheduleDate.before(currentDate) && endDate.after(currentDate);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="/test/submit/mcq", consumes= {"application/json"})
	public ResponseEntity<Response> submitMCQTest(@RequestBody List<MCQAnswer> answers) {
		try {
			mAnsRepo.saveAll(answers);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Test submited successfully"), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/test/submit/subjective", consumes= {"application/json"})
	public ResponseEntity<Response> submitSubTest(@RequestBody List<SubjectiveAnswer> answers) {
		try {
			sAnsRepo.saveAll(answers);
			return new ResponseEntity<>(new Response(Respond.success.toString(), "Test submited successfully"), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/{rollNo}/past_tests")
	public ResponseEntity<Response> getStudentPastTests(@PathVariable("rollNo") String rollNo){
		try {
			Student student = sRepo.findById(rollNo).orElse(null);
			if (Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "Student not found"), HttpStatus.NOT_FOUND);
			}
			
			List<Test> tests = tRepo.getPastTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
			if (Objects.isNull(tests) || tests.size()==0) {
				return new ResponseEntity<>(new Response(Respond.error.toString(), "You haven't given any test in past"), HttpStatus.OK);
			}
			
			List<PastTests> pTests = new ArrayList<>();
			
			for (Test t : tests) {
				if(mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) {
					PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(),  t.getResultOn(), true);
					pTests.add(pt);
				} else if(!testTimeCheck(t.getScheduleOn(), t.getDuration())) {
					PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(),  t.getResultOn(), false);
					pTests.add(pt);
				}
			}
			
			return new ResponseEntity<>(new Response(Respond.success.toString(), pTests), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Student not found"), HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/test/{testId}/result_available")
	public ResponseEntity<List<Boolean>> isResultAvailable(@PathVariable("testId") int testId){
		List<Boolean> list = new ArrayList<>();
		
		Test test = tRepo.findById(testId).orElse(null);
		if (Objects.isNull(test)) {
			list.add(false);
			return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
		}
		
		boolean isResultDeclared = !testTimeCheck(test.getResultOn(), 0);
		list.add(isResultDeclared);
		return new ResponseEntity<>(list, HttpStatus.OK);
	}
	
	@GetMapping("/{rollNo}/test/{testId}/result")
	public ResponseEntity<Response> getTestResult(@PathVariable("rollNo") String rollNo, @PathVariable("testId") int testId){
		Student student = sRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		Test test = tRepo.findById(testId).orElse(null);
		if (Objects.isNull(test)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Invalid test id"), HttpStatus.NOT_FOUND);
		}
		
		TestInfo info = null;
		if (!testTimeCheck(test.getResultOn(), 0)) {
			if(test.isSubjective()) {
				TestDetails td = subRepo.getNoOfQuestionsAndMaxMarks(testId);
				info = new TestInfo(test.getTitle(), test.getCreatedBy(), test.getSubjectCode(), test.getScheduleOn(), test.getDuration(), td.getNoOfQuestions(), td.getMaxMarks(), -1);
				
				List<SubjectiveTestData> ansList = sAnsRepo.getAnswers(testId, rollNo);
				int total = 0;
				for(int i = 0; i<ansList.size(); i++) {
					total += ansList.get(i).getScore();
					info.ansData.add(ansList.get(i));
				}
				
				info.setTotalMarks(total);
			} else {
				int totalQuestions = mcqRepo.getNoOfQuestions(testId);
				info = new TestInfo(test.getTitle(), test.getCreatedBy(), test.getSubjectCode(), test.getScheduleOn(), test.getDuration(), totalQuestions, test.getMarks() * totalQuestions, test.getNegativeMarks());
				List<MCQTestData> ansList = mAnsRepo.getAnswers(testId, rollNo);
				
				int total = 0;
				for (int i = 0; i<ansList.size(); i++) {
					info.ansData.add(new MCQData(ansList.get(i)));
					if (ansList.get(i).getCorrectOption().equals(ansList.get(i).getAnswer()))
						total += test.getMarks();
					else if (ansList.get(i).getAnswer()!=null)
						total -= test.getNegativeMarks();
				}
				
				info.setTotalMarks(total);
			}
		}
		
		return new ResponseEntity<>(new Response(Respond.success.toString(), info), HttpStatus.OK);
	}
	
	@GetMapping("/tests/stats")
	public ResponseEntity<Response> getTestStats(@RequestParam("rollNo") String rollNo,
			@RequestParam("subject") String subjectCode, @RequestParam("type") String type,
			@RequestParam("from") String from, @RequestParam("to") String to){
		Student student = sRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Respond.error.toString(), "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		try {
			List<Test> tests = tRepo.getTestsByScheduleOnBetween(student.getBranch(), student.getSem(), student.getSection(), from, to);
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
					int count = sAnsRepo.getAllStudents(test.getTestId());
					if (count==0) {
						percent = 0;
						avgPercent = 0;
					} else {
						TestDetails td = subRepo.getNoOfQuestionsAndMaxMarks(test.getTestId());
						
						Integer total = sAnsRepo.getTotalScore(test.getTestId(), rollNo);
						total = total == null ? 0 : total;
						percent = (double)total/td.getMaxMarks() * 100;
						
						Integer allScore = sAnsRepo.getSumOfAllStudentsScore(test.getTestId());
						allScore = allScore == null ? 0 : allScore;
						avgPercent = (double)allScore/(count * td.getMaxMarks()) * 100;
					}
				} else {
					int count = mAnsRepo.getAllStudents(test.getTestId());
					if (count==0) {
						percent = 0;
						avgPercent = 0;
					} else {
						int allScore = 0, score = 0;
						int noOfQuestions = mcqRepo.getNoOfQuestions(test.getTestId());
						int maxMarks = test.getMarks() * noOfQuestions;
						
						List<MCQTestInfo> allAnsList = mAnsRepo.getAllStudentsAnswers(test.getTestId());
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
