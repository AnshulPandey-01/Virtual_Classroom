package com.anshul.examportal.student;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.examportal.test.ScheduledTests;
import com.anshul.examportal.test.Test;
import com.anshul.examportal.test.TestContainer;
import com.anshul.examportal.test.TestDetails;
import com.anshul.examportal.test.TestRepo;
import com.anshul.examportal.test.mcq_test.MCQTest;
import com.anshul.examportal.test.mcq_test.MCQTestRepo;
import com.anshul.examportal.test.mcq_test.mcq_answer.MCQAnswer;
import com.anshul.examportal.test.mcq_test.mcq_answer.MCQAnswerRepo;
import com.anshul.examportal.test.sub_test.SubTestRepo;
import com.anshul.examportal.test.sub_test.sub_answer.SubAnswerRepo;
import com.anshul.examportal.test.sub_test.sub_answer.SubjectiveAnswer;


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
	public List<String> checkStudentLogin(@RequestBody Student s) {
		
		List<String> list = new ArrayList<>(2);
		list.add("STUDENT");
		
		Student student = sRepo.getByEmail(s.getEmail());
		if(student!=null) {
			if(passwordEcorder.matches(s.getPassword(), student.getPassword())) {
				list.add(student.getName());
				list.add(student.getRollNo());
				return list;
			}
		}
		
		list.add("false");
		return list;
	}
	
	@GetMapping("/student_tests/{rollNo}")
	public List<ScheduledTests> getStudentTests(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.getByRollNo(rollNo);
		List<Test> tests = tRepo.getBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<ScheduledTests> s = new ArrayList<>();
		for(Test t : tests) {
			if(isSchecduledTest(t.getScheduleOn(), t.getDuration())) {
				ScheduledTests st = new ScheduledTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.getIsSubjective(), t.getDuration(), t.getScheduleOn(), t.getResultOn(), t.getNegativeMarks());
				
				if(t.getIsSubjective() && !sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) {
					TestDetails td = subRepo.getNoOfQuestions(t.getTestId());
					st.setNoOfQuestions(td.getNoOfQuestions());
					st.setTotalMarks(td.getTotalMarks());
				}else if(!mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) {
					st.setNoOfQuestions(mcqRepo.getNoOfQuestions(t.getTestId()));
					st.setTotalMarks(st.getNoOfQuestions() * t.getMarks());
				}
				s.add(st);
			}
		}
		
		return s;
	}
	
	private boolean isSchecduledTest(String dateTime, int duration){
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
	
	@PostMapping("/student/authenticate_test")
	public List<Boolean> checkTest(@RequestBody Test t) throws ParseException {
		System.out.println(t.toString());
		Test test = tRepo.getByTestId(t.getTestId());
		
		List<Boolean> list = new ArrayList<>();
		boolean checkTime = true;
		checkTime = testTime(test.getScheduleOn(), test.getDuration());
		
		if(test!=null && test.getPassword().equals(t.getPassword()) && checkTime) {
			list.add(true);
			list.add(test.getIsSubjective());
			return list;
		}else {
			list.add(false);
			list.add(null);
			return list;
		}
	}
	
	@GetMapping("/Test/{testId}")
	public List<TestContainer> getTestQuestions(@PathVariable("testId") int testId){
		Test t = tRepo.getByTestId(testId);
		boolean timeCheck = true;
		timeCheck = testTime(t.getScheduleOn(), t.getDuration());
		if(timeCheck) {
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
	
	private boolean testTime(String dateTime, int duration){
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
	
	@PostMapping("/test/mcq_answer")
	public boolean submitMCQTest(@RequestBody List<MCQAnswer> answers) {
		try {
			mAnsRepo.saveAll(answers);
			return true;
		}catch(Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	@PostMapping("/test/sub_answer")
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
	public List<ScheduledTests> getStudentPastTests(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.getByRollNo(rollNo);
		List<Test> tests = tRepo.getBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<ScheduledTests> s = new ArrayList<>();
		for(Test t : tests) {
			if(!isSchecduledTest(t.getScheduleOn(), t.getDuration()) || mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) ) {
				ScheduledTests st = new ScheduledTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.getIsSubjective(),  t.getResultOn());
				s.add(st);
			}
		}
		
		return s;
	}
	
}
