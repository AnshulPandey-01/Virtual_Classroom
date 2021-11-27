package com.anshul.virtual_classroom.controllers;

import java.io.IOException;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.anshul.virtual_classroom.entity.AssignmentSubmission;
import com.anshul.virtual_classroom.entity.Post;
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
import com.anshul.virtual_classroom.repos.AssignmentSubmissionRepo;
import com.anshul.virtual_classroom.repos.TestRepo;
import com.anshul.virtual_classroom.response.Response;
import com.anshul.virtual_classroom.response.Response.Status;
import com.anshul.virtual_classroom.response.assignment.StudentAssignments;
import com.anshul.virtual_classroom.response.test.PastTests;
import com.anshul.virtual_classroom.response.test.ScheduledTests;
import com.anshul.virtual_classroom.response.test.TestInfo;
import com.anshul.virtual_classroom.utility.ChangePassword;
import com.anshul.virtual_classroom.utility.assignment.StudentSubmittedView;
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
	@Autowired
	private PostRepo postRepo;
	@Autowired
	private AssignmentSubmissionRepo assignmentSubRepo;
	
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
			Student student = studentRepo.getOneByEmail(s.getEmail());
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
			Student student = studentRepo.getById(a.getEmail());
			if (passwordEcorder.matches(a.getPassword(), student.getPassword())) {
				student.setPassword(passwordEcorder.encode(a.getNewPassword()));
				studentRepo.save(student);
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
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		String[] subjects = bsRepo.getOneByBranch(student.getBranch()).getSubjects();
		return new ResponseEntity<>(new Response(Status.success, subjects), HttpStatus.OK);
	}
	
	@GetMapping("/{rollNo}/tests")
	public ResponseEntity<Response> getStudentTests(@PathVariable("rollNo") String rollNo){
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		List<Test> tests = testRepo.getUpComingTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
		if (Objects.isNull(tests) || tests.size()==0) {
			return new ResponseEntity<>(new Response(Status.success, "No scheduled tests"), HttpStatus.OK);
		}
		
		List<ScheduledTests> s = new ArrayList<>();
		for (Test t : tests) {
			if (! (subAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || mcqAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) ) {
				ScheduledTests st = new ScheduledTests(t.getTestId(), t.getTitle(), t.getSubjectCode(), t.isSubjective(), t.getDuration(), t.getScheduleOn(), t.getResultOn(), t.getNegativeMarks());
				
				if(t.isSubjective()) {
					TestDetails td = subTestRepo.getNoOfQuestionsAndMaxMarks(t.getTestId());
					st.setNoOfQuestions(td.getNoOfQuestions());
					st.setTotalMarks(td.getMaxMarks());
				} else {
					st.setNoOfQuestions(mcqTestRepo.getNoOfQuestions(t.getTestId()));
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
			Student student = studentRepo.findById(rollNo).orElse(null);
			if (Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
			}
			
			List<Test> tests = testRepo.getPastTestsBySBS(student.getSem(), student.getBranch(), student.getSection());
			if (Objects.isNull(tests) || tests.size()==0) {
				return new ResponseEntity<>(new Response(Status.success, "You haven't given any test in past"), HttpStatus.OK);
			}
			
			List<PastTests> pTests = new ArrayList<>();
			
			for (Test t : tests) {
				if(mcqAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId()) || subAnsRepo.existsByRollNoAndTestId(rollNo, t.getTestId())) {
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
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Student not found"), HttpStatus.NOT_FOUND);
		}
		
		Test test = testRepo.findById(testId).orElse(null);
		if (Objects.isNull(test)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid test id"), HttpStatus.NOT_FOUND);
		}
		
		TestInfo info = null;
		if (!timeUtility.testTimeCheck(test.getResultOn(), 0)) {
			if(test.isSubjective()) {
				TestDetails td = subTestRepo.getNoOfQuestionsAndMaxMarks(testId);
				info = new TestInfo(test.getTitle(), test.getCreatedBy(), test.getSubjectCode(), test.getScheduleOn(), test.getDuration(), td.getNoOfQuestions(), td.getMaxMarks(), -1);
				
				List<SubjectiveTestData> ansList = subAnsRepo.getAnswers(testId, rollNo);
				int total = 0;
				for(int i = 0; i<ansList.size(); i++) {
					total += ansList.get(i).getScore();
					info.ansData.add(ansList.get(i));
				}
				
				info.setTotalMarks(total);
			} else {
				int totalQuestions = mcqTestRepo.getNoOfQuestions(testId);
				info = new TestInfo(test.getTitle(), test.getCreatedBy(), test.getSubjectCode(), test.getScheduleOn(), test.getDuration(), totalQuestions, test.getMarks() * totalQuestions, test.getNegativeMarks());
				List<MCQTestData> ansList = mcqAnsRepo.getAnswers(testId, rollNo);
				
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
			
			Student student = studentRepo.findById(rollNo).orElse(null);
			if (Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
			}
			
			Test test = testRepo.findById(testId).orElse(null);
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
				
				map.put("maxMarks", subTestRepo.getNoOfQuestionsAndMaxMarks(testId).getMaxMarks());
				
				List<SubjectiveTestData> ansList = subAnsRepo.getAnswers(testId, rollNo);
				map.put("ansList", ansList);
				
				int score = 0;
				for (int i = 0; i<ansList.size(); i++) {
					if (!(ansList.get(i).getScore()==-1))
						score += ansList.get(i).getScore();
				}
				map.put("score", score);
			} else {
				map.put("isEditable", false);
				map.put("maxMarks", mcqTestRepo.getNoOfQuestions(testId) * test.getMarks());
				
				List<MCQTestData> list = mcqAnsRepo.getAnswers(testId, rollNo);
				
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
			Student student = studentRepo.findById(rollNo).orElse(null);
			if (Objects.isNull(student)) {
				return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
			}
			
			Test test = testRepo.findById(testId).orElse(null);
			if (Objects.isNull(test)) {
				return new ResponseEntity<>(new Response(Status.error, "Invalid test id"), HttpStatus.NOT_FOUND);
			}
			
			List<SubjectiveAnswer> ansList = subAnsRepo.findByRollNoAndTestId(rollNo, testId);
			if (ansList.isEmpty()) {
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
			subAnsRepo.saveAll(updatedList);
			
			return new ResponseEntity<>(new Response(Status.success, "Updated Successfully"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Status.error, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Transactional(readOnly=true)
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{rollNo}/assignments/ongoing")
	public ResponseEntity<Response> getOngoingAssignments(@PathVariable("rollNo") String rollNo) {
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
		}
		
		List<Post> assignments = postRepo.getOngoingAssignmentsBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<StudentAssignments> studentAssignments = new ArrayList<>();
		for(Post asv : assignments) {
			if(!assignmentSubRepo.existsByAssignmentIdAndRollNo(asv.getId(), rollNo)) {
				studentAssignments.add(new StudentAssignments(asv.getUniqueKey(), asv.getTitle(), asv.getContent(), asv.getCreatedBy(), asv.getSubjectCode(), asv.getAssignTime(), asv.getDueTime(), asv.getMarks()));
			}
		}
		
		if(studentAssignments.isEmpty()) {
			return new ResponseEntity<>(new Response(Status.success, "No assignment scheduled"), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Status.success, studentAssignments), HttpStatus.OK);
	}
	
	@Transactional(readOnly=true)
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{rollNo}/assignments/missed")
	public ResponseEntity<Response> getDueAssignments(@PathVariable("rollNo") String rollNo) {
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
		}
		
		List<Post> assignments = postRepo.getDueAssignmentsBySBS(student.getSem(), student.getBranch(), student.getSection());
		
		List<StudentAssignments> dueAssignments = new ArrayList<>();
		for(Post asv : assignments) {
			if(!assignmentSubRepo.existsByAssignmentIdAndRollNo(asv.getId(), rollNo)) {
				dueAssignments.add(new StudentAssignments(asv.getUniqueKey(), asv.getTitle(), asv.getContent(), asv.getCreatedBy(), asv.getSubjectCode(), asv.getAssignTime(), asv.getDueTime(), asv.getMarks()));
			}
		}
		
		if(dueAssignments.isEmpty()) {
			return new ResponseEntity<>(new Response(Status.success, "No assignment scheduled"), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Status.success, dueAssignments), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{rollNo}/assignments/completed")
	public ResponseEntity<Response> getCompletedAssignments(@PathVariable("rollNo") String rollNo) {
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
		}
		
		List<StudentSubmittedView> completed = assignmentSubRepo.getSubmittedAssignments(rollNo);
		return new ResponseEntity<>(new Response(Status.success, completed), HttpStatus.OK);		
	}
	
	@Transactional
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{rollNo}/assignment/submission")
	public ResponseEntity<Response> submitAssignment(@PathVariable("rollNo") String rollNo,
			@RequestParam("assignmentUniqueKey") String assignmentUniqueKey, @RequestParam(name = "attachment") MultipartFile attachment) {
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
		}
		
		Post post = postRepo.findByUniqueKeyAndIsAssignment(assignmentUniqueKey, Boolean.TRUE).orElse(null);
		if(Objects.isNull(post)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid assignment"), HttpStatus.BAD_REQUEST);
		}
		
		if(assignmentSubRepo.existsByAssignmentIdAndRollNo(post.getId(), rollNo)) {
			return new ResponseEntity<>(new Response(Status.error, "You have already submitted assignment"), HttpStatus.CONFLICT);
		}
		
		boolean isLate = !timeUtility.checkTimeInBetween(post.getAssignTime(), post.getDueTime());
		
		byte[] file;
		try {
			file = attachment.getBytes();
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(new Response(Status.error, "An error occured please try again"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		AssignmentSubmission submission = new AssignmentSubmission(post.getId(), rollNo, file, timeUtility.getCurrentTime(), isLate);
		assignmentSubRepo.save(submission);
		
		return new ResponseEntity<>(new Response(Status.success, "Assignment submitted successfully"), HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/{rollNo}/posts")
	public ResponseEntity<Response> getPosts(@PathVariable("rollNo") String rollNo) {
		Student student = studentRepo.findById(rollNo).orElse(null);
		if (Objects.isNull(student)) {
			return new ResponseEntity<>(new Response(Status.error, "Invalid student roll no"), HttpStatus.NOT_FOUND);
		}
		
		List<PostStudentView> posts = postRepo.getPostsBySBS(student.getSem(), student.getBranch(), student.getSection());
		if(posts.isEmpty()) {
			return new ResponseEntity<>(new Response(Status.success, "No posts available"), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new Response(Status.success, posts), HttpStatus.OK);
	}
	
}
