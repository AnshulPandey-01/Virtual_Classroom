package com.anshul.examportal.student;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.text.DateFormatter;

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
import com.anshul.examportal.test.sub_test.SubTestRepo;


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
	public List<ScheduledTests> getStudentTests(@PathVariable("rollNo") String rollNo) throws ParseException{
		Student student = sRepo.getByRollNo(rollNo);
		List<Test> tests = tRepo.getBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<ScheduledTests> s = new ArrayList<>();
		for(Test t : tests) {
			/* do the test check for removing past tests
			Date scheduleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(t.getScheduleOn());
			Date currentDate = new Date(System.currentTimeMillis());
			System.out.println(currentDate.before(scheduleDate) + " " + scheduleDate + " | " + currentDate);
			*/
			
			ScheduledTests st = new ScheduledTests(t.getTitle(), t.getSubjectCode(), t.getIsSubjective(), t.getDuration(), t.getScheduleOn(), t.getResultOn(), t.getNegativeMarks());
			
			if(t.getIsSubjective()) {
				TestDetails td = subRepo.getNoOfQuestions(t.getTestId());
				st.setNoOfQuestions(td.getNoOfQuestions());
				st.setTotalMarks(td.getTotalMarks());
			}else {
				st.setNoOfQuestions(mcqRepo.getNoOfQuestions(t.getTestId()));
				st.setTotalMarks(st.getNoOfQuestions() * t.getMarks());
			}
			s.add(st);
		}
		
		return s;
	}
	
	@PostMapping("/student/authenticate_test")
	public List<Boolean> checkTest(@RequestBody Test t) {
		List<Boolean> list = new ArrayList<>();
		
		Test test = tRepo.getByTestId(t.getTestId());
		if(test!=null && test.getPassword().equals(t.getPassword())) {
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
	public List<TestContainer> getTestQuestions(@PathVariable("testId") int testId) throws ParseException{
		Test t = tRepo.getByTestId(testId);
		
		/* add time check
		if(scheduledDateTime(t.getScheduleOn().split(" "), t.getDuration())) {
			
		}
		*/
		
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
	
	private boolean scheduledDateTime(String[] dateTime, int duration) throws ParseException{
		System.out.println("["  + dateTime[0] + ", " + dateTime[1] + "] " + duration);
		
		String currentdate = LocalDate.now().toString();
		int currentHour = LocalTime.now().getHour();
		int currentminute = LocalTime.now().getMinute();
		
		System.out.println(currentdate + " " + currentHour + ":" + currentminute);
		
		//if(dateTime[0].equals(currentdate)) {
			Date scheduleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime[0] + " " + dateTime[1]);
			Date endDate = new Date(scheduleDate.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			Date currentDate = new Date(System.currentTimeMillis());
			System.out.println(currentDate.before(endDate) + " " + endDate + " | " + currentDate);
		//}
		
		return false;
	}
	
}
