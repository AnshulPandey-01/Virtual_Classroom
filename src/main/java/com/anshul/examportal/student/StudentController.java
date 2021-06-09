package com.anshul.examportal.student;

import java.util.Date;
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
import org.springframework.web.bind.annotation.RestController;

import com.anshul.examportal.test.PastTests;
import com.anshul.examportal.test.ScheduledTests;
import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.TestContainer;
import com.anshul.examportal.test.TestDetails;
import com.anshul.examportal.test.TestRepo;
import com.anshul.examportal.test.answer.MCQData;
import com.anshul.examportal.test.answer.SubjectiveData;
import com.anshul.examportal.test.answer.TestInfo;
import com.anshul.examportal.test.mcq.MCQTest;
import com.anshul.examportal.test.mcq.MCQTestRepo;
import com.anshul.examportal.test.mcq.answer.MCQAnswer;
import com.anshul.examportal.test.mcq.answer.MCQAnswerRepo;
import com.anshul.examportal.test.subjective.SubTestRepo;
import com.anshul.examportal.test.subjective.answer.SubAnswerRepo;
import com.anshul.examportal.test.subjective.answer.SubjectiveAnswer;


@CrossOrigin//(origins ="http://localhost:4500")
@RestController
public class StudentController {
	
	static final long ONE_MINUTE_IN_MILLIS = 60000;
	
	@Autowired
	private StudentRepo sRepo;
	@Autowired
	private TestRepo tRepo;
	@Autowired
	private MCQTestRepo mcqRepo;
	@Autowired
	private SubTestRepo subRepo;
	@Autowired
	private MCQAnswerRepo mAnsRepo;
	@Autowired
	private SubAnswerRepo sAnsRepo;
	
	private static BCryptPasswordEncoder passwordEcorder = new BCryptPasswordEncoder();
	
	
	@PostMapping(path="student_login", consumes= {"application/json"})
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
	
	@PostMapping(path="/change_password/STUDENT", consumes= {"application/json"})
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
	
	@GetMapping("/student_tests/{rollNo}")
	public List<ScheduledTests> getStudentTests(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.getOne(rollNo);
		List<Test> tests = tRepo.getUpComingTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<ScheduledTests> s = new ArrayList<>();
		for(Test t : tests) {
			if(! (sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) ) {
				ScheduledTests st = new ScheduledTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.getIsSubjective(), t.getDuration(), t.getScheduleOn(), t.getResultOn(), t.getNegativeMarks());
				
				if(t.getIsSubjective()) {
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
		
		return s;
	}
	
	private boolean testTimeCheck(String dateTime, int duration){
		try {
			//System.out.println(dateTime + " | " + duration);
			
			Date scheduleTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
			Date endTime = new Date(scheduleTime.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			Date currentTime = new Date(System.currentTimeMillis());
			
			//System.out.println(currentTime + " | " + scheduleTime + " | " + endTime);
			
			return currentTime.before(endTime);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="/student/authenticate_test", consumes= {"application/json"})
	public List<Boolean> checkTest(@RequestBody Test t) throws ParseException {
		List<Boolean> list = new ArrayList<>();
		try {
			Test test = tRepo.getOne(t.getTestId());
			
			if(test.getPassword().equals(t.getPassword()) && checkTestTime(test.getScheduleOn(), test.getDuration())) {
				list.add(true);
				list.add(test.getIsSubjective());
				return list;
			}else {
				list.add(false);
				list.add(null);
				return list;
			}
		}catch(Exception e) {
			System.out.println(e.getMessage());
			list.add(false);
			list.add(null);
			return list;
		}
	}
	
	@GetMapping("/Test/{testId}")
	public List<TestContainer> getTestQuestions(@PathVariable("testId") int testId){
		Test t = tRepo.getByTestId(testId);
		
		if(checkTestTime(t.getScheduleOn(), t.getDuration())) {
			List<TestContainer> test;
			if(t.getIsSubjective()) {
				test = subRepo.findByTestId(testId);
			}else{
				test = mcqRepo.findByTestId(testId);
				for(int i = 0; i<test.size(); i++) {
					MCQTest mcq = (MCQTest)test.get(i);
					mcq.setCorrectOption(String.valueOf(t.getMarks()));
					test.set(i, mcq);
				}
			}
			return test;
		}
		
		return null;
	}
	
	private boolean checkTestTime(String dateTime, int duration){
		try {
			//System.out.println(dateTime + " | " + duration);
			
			Date currentDate = new Date(System.currentTimeMillis());
			Date scheduleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
			Date endDate = new Date(scheduleDate.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			
			//System.out.println(currentDate + " | " + scheduleDate + " | " + endDate);
			
			return scheduleDate.before(currentDate) && endDate.after(currentDate);
		}catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="/test/mcq_answer", consumes= {"application/json"})
	public boolean submitMCQTest(@RequestBody List<MCQAnswer> answers) {
		try {
			mAnsRepo.saveAll(answers);
			return true;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping(path="/test/sub_answer", consumes= {"application/json"})
	public boolean submitSubTest(@RequestBody List<SubjectiveAnswer> answers) {
		try {
			sAnsRepo.saveAll(answers);
			return true;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@GetMapping("/student_tests/past/{rollNo}")
	public List<PastTests> getStudentPastTests(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.getOne(rollNo);
		List<Test> tests = tRepo.getPastTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<PastTests> pTests = new ArrayList<>();
		for(Test t : tests) {
			if(mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) {
				PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.getIsSubjective(),  t.getResultOn(), true);
				pTests.add(pt);
			}else if(!testTimeCheck(t.getScheduleOn(), t.getDuration())) {
				PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.getIsSubjective(),  t.getResultOn(), false);
				pTests.add(pt);
			}
		}
		
		return pTests;
	}
	
	@GetMapping("/test/{rollNo}/{testId}")
	public TestInfo getTestResult(@PathVariable("rollNo") String rollNo, @PathVariable("testId") int testId){
		Test t = tRepo.getByTestId(testId);
		TestInfo info = null;
		
		if(!testTimeCheck(t.getResultOn(), 0)) {
			if(t.getIsSubjective()) {
				TestDetails td = subRepo.getNoOfQuestionsAndMaxMarks(testId);
				info = new TestInfo(t.getTitle(), t.getCreatedBy(), t.getSubjectCode(), t.getScheduleOn(), t.getDuration(), td.getNoOfQuestions(), td.getTotalMarks(), -1);
				
				List<SubjectiveData> ansList = sAnsRepo.getAnswers(testId, rollNo);
				int total = 0;
				for(int i = 0; i<ansList.size(); i++) {
					total += ansList.get(i).getScore();
					info.ansData.add(ansList.get(i));
				}
				
				info.setTotalMarks(total);
			}else {
				int totalQuestions = mcqRepo.getNoOfQuestions(testId);
				info = new TestInfo(t.getTitle(), t.getCreatedBy(), t.getSubjectCode(), t.getScheduleOn(), t.getDuration(), totalQuestions, t.getMarks() * totalQuestions, t.getNegativeMarks());
				List<MCQData> ansList = mAnsRepo.getAnswers(testId, rollNo);
				
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
		
		return info;
	}
	
}
