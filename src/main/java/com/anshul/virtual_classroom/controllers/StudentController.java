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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anshul.virtual_classroom.entity.Student;
import com.anshul.virtual_classroom.entity.SubjectiveAnswer;
import com.anshul.virtual_classroom.entity.Test;
import com.anshul.virtual_classroom.repos.BranchSubjectsRepos;
import com.anshul.virtual_classroom.repos.MCQAnswerRepo;
import com.anshul.virtual_classroom.repos.MCQTestRepo;
import com.anshul.virtual_classroom.repos.PostRepo;
import com.anshul.virtual_classroom.repos.StudentRepo;
import com.anshul.virtual_classroom.repos.SubjectiveAnswerRepo;
import com.anshul.virtual_classroom.repos.SubjectiveTestRepo;
import com.anshul.virtual_classroom.repos.TestRepo;
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Status;
import com.anshul.virtual_classroom.response.models.PastTests;
import com.anshul.virtual_classroom.response.models.ScheduledTests;
import com.anshul.virtual_classroom.response.models.TestInfo;
import com.anshul.virtual_classroom.utility.ChangePassword;
import com.anshul.virtual_classroom.utility.mcq.MCQData;
import com.anshul.virtual_classroom.utility.mcq.MCQTestData;
import com.anshul.virtual_classroom.utility.post.PostStudentView;
import com.anshul.virtual_classroom.utility.service.TimeUtilityService;
import com.anshul.virtual_classroom.utility.subjective.SubjectiveTestData;
import com.anshul.virtual_classroom.utility.test.TestDetails;

@CrossOrigin
@RestController
@RequestMapping("student")
public class StudentController {
	
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
	@Autowired
	private PostRepo postRepo;
	
	@Autowired
	private TimeUtilityService timeUtility;
	
	private BCryptPasswordEncoder passwordEcorder;
	
	public StudentController() {
		this.passwordEcorder = new BCryptPasswordEncoder();
	}
	
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
			return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		String[] subjects = bsRepo.getOneByBranch(student.getBranch()).getSubjects();
		return new ResponseEntity<>(new Response(Status.success, subjects), HttpStatus.OK);
	}
	
	@GetMapping("/{rollNo}/tests")
	public ResponseEntity<Response> getStudentTests(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		List<Test> tests = tRepo.getUpComingTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
		if (Objects.isNull(tests) || tests.size()==0) {
			return new ResponseEntity<>(new Response(Status.success, "No scheduled tests"), HttpStatus.OK);
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
		
		return new ResponseEntity<>(new Response(Status.success, s), HttpStatus.OK);
	}
	
	@GetMapping("/{rollNo}/past_tests")
	public ResponseEntity<Response> getStudentPastTests(@PathVariable("rollNo") String rollNo){
		try {
			Student student = sRepo.findById(rollNo).orElse(null);
			if (Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
			}
			
			List<Test> tests = tRepo.getPastTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
			if (Objects.isNull(tests) || tests.size()==0) {
				return new ResponseEntity<>(new Response(Status.success, "You haven't given any test in past"), HttpStatus.OK);
			}
			
			List<PastTests> pTests = new ArrayList<>();
			
			for (Test t : tests) {
				if(mAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || sAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) {
					PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(),  t.getResultOn(), true);
					pTests.add(pt);
				} else if(!timeUtility.testTimeCheck(t.getScheduleOn(), t.getDuration())) {
					PastTests pt = new PastTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(),  t.getResultOn(), false);
					pTests.add(pt);
				}
			}
			
			return new ResponseEntity<>(new Response(Status.success, pTests), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/{rollNo}/test/{testId}/result")
	public ResponseEntity<Response> getTestResult(@PathVariable("rollNo") String rollNo, @PathVariable("testId") int testId){
		Student student = sRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		Test test = tRepo.findById(testId).orElse(null);
		if (Objects.isNull(test)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid test id"), HttpStatus.NOT_FOUND);
		}
		
		TestInfo info = null;
		if (!timeUtility.testTimeCheck(test.getResultOn(), 0)) {
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
		
		return new ResponseEntity<>(new Response(Status.success, info), HttpStatus.OK);
	}
	
	@GetMapping("/{rollNo}/past_tests/{testId}/answer")
	public ResponseEntity<Response> getPastTestStudentAnswer(@PathVariable("testId") int testId, @PathVariable("rollNo") String rollNo){
		try {
			Map<String, Object> map = new HashMap<>();
			
			Student student = sRepo.findById(rollNo).orElse(null);
			if (Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
			}
			
			Test test = tRepo.findById(testId).orElse(null);
			if (Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Status.error, "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
			map.put("rollNo", student.getRollNo());
			map.put("name", student.getName());
			map.put("title", test.getTitle());
			map.put("subjectCode", test.getSubjectCode());
			map.put("subjective", test.isSubjective());
			
			if (test.isSubjective()) {
				Date currentDate = new Date(System.currentTimeMillis());
				Date resultDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(test.getResultOn());
				if (resultDate.after(currentDate))
					map.put("isEditable", true);
				else
					map.put("isEditable", false);
				
				map.put("maxMarks", subRepo.getNoOfQuestionsAndMaxMarks(testId).getMaxMarks());
				
				List<SubjectiveTestData> ansList = sAnsRepo.getAnswers(testId, rollNo);
				map.put("ansList", ansList);
				
				int score = 0;
				for (int i = 0; i<ansList.size(); i++) {
					if (!(ansList.get(i).getScore()==-1))
						score += ansList.get(i).getScore();
				}
				map.put("score", score);
			} else {
				map.put("isEditable", false);
				map.put("maxMarks", mcqRepo.getNoOfQuestions(testId) * test.getMarks());
				
				List<MCQTestData> list = mAnsRepo.getAnswers(testId, rollNo);
				
				List<MCQData> ansList = new ArrayList<>();
				int score = 0;
				for (int i = 0; i<list.size(); i++) {
					ansList.add(new MCQData(list.get(i)));
					if (list.get(i).getCorrectOption().equals(list.get(i).getAnswer()))
						score += test.getMarks();
					else if (list.get(i).getAnswer()!=null)
						score -= test.getNegativeMarks();
				}
				map.put("ansList", ansList);
				map.put("score", score);
			}
			
			return new ResponseEntity<>(new Response(Status.success, map), HttpStatus.OK);
		} catch(Exception e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping(path="/{rollNo}/past_tests/{testId}/check", consumes = {"application/json"})
	public ResponseEntity<Response> markTheTest(@PathVariable("rollNo") String rollNo, @PathVariable("testId") int testId, @RequestBody List<SubjectiveAnswer> marksList) {		
		try {
			Student student = sRepo.findById(rollNo).orElse(null);
			if (Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
			}
			
			Test test = tRepo.findById(testId).orElse(null);
			if (Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Status.error, "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
			List<SubjectiveAnswer> ansList = sAnsRepo.findByRollNoAndTestId(rollNo, testId);
			if (Objects.isNull(ansList) || ansList.isEmpty()) {
				return new ResponseEntity<>(new Response(Status.error, "Student hasn't given the test"), HttpStatus.NOT_FOUND);
			}
			
			Map<String, SubjectiveAnswer> map = new HashMap<>();
			for (SubjectiveAnswer ans : ansList) 
				map.put(ans.getQuestionId(), ans);
			
			for (SubjectiveAnswer marks : marksList) {
				SubjectiveAnswer ans = map.get(marks.getQuestionId());
				ans.setScore(marks.getScore());
			}
			
			List<SubjectiveAnswer> updatedList = new ArrayList<>(map.values());
			sAnsRepo.saveAll(updatedList);
			
			return new ResponseEntity<>(new Response(Status.success, "Updated Successfully"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{rollNo}/posts")
	public ResponseEntity<Response> getPosts(@PathVariable("rollNo") String rollNo){
		Student student = sRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
		}
		
		List<PostStudentView> posts = postRepo.getPostsBySBS(student.getSem(), student.getBranch(), student.getSection());
		if(Objects.isNull(posts) || posts.isEmpty()) {
			return new ResponseEntity<>(new Response(Status.success, "No posts available"), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Status.success, posts), HttpStatus.OK);
	}
	
}
