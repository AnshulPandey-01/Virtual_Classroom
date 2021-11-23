package com.anshul.virtual_classroom.utility.service;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.anshul.virtual_classroom.entity.Post;
import com.anshul.virtual_classroom.entity.Student;
import com.anshul.virtual_classroom.entity.Test;
import com.anshul.virtual_classroom.repos.StudentRepo;

@Service
public class MailUtilityService {
	
	private static final String SENDER = "adgamesindia@gmail.com";
	
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private StudentRepo studentRepo;
	
	public void sendMails(Test test) {
		String subject = "New Test from " + test.getCreatedBy();
		String mailBody = "Title: " + test.getTitle() + "\n" +
				"Schedule On: " + test.getScheduleOn() + "\n" +
				"Test ID: " + test.getTestId() + "\n" + 
				"Password: " + test.getPassword() + "\n";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(SENDER);
		mailMessage.setSubject(subject);
		mailMessage.setText(mailBody);
		
		List<Student> students = studentRepo.findBySemAndBranchAndSection(test.getSem(), test.getBranch(), test.getSection());
		if (Objects.isNull(students) ||  students.size()==0) return ;
		
		for (Student s : students) {
			mailMessage.setTo(s.getEmail());
			mailSender.send(mailMessage);
		}
	}
	
	public void sendMails(Post post, HttpServletRequest request) {
		List<Student> students = studentRepo.findBySemAndBranchAndSection(post.getSem(), post.getBranch(), post.getSection());
		if (Objects.isNull(students) ||  students.size()==0) return ;
		
		final String HOST_URL = ServletUriComponentsBuilder.fromRequestUri(request).replacePath(null).build().toUriString();
		String subject, mailBody;
		
		if (post.isAssignment()) {
			subject = "New Assignment from " + post.getCreatedBy();
			mailBody = "Title: " + post.getTitle() + "\n" + 
					"Subject code: " + post.getSubjectCode() + "\n" +
					"Starting on: " + post.getAssignTime() + "\n" +
					"Due on: " + post.getDueTime() + "\n" +
					"Marks: " + post.getMarks();
		} else {
			subject = "New Post from " + post.getCreatedBy();
			mailBody = "Title: " + post.getTitle() + "\n" + 
					"Subject code: " + post.getSubjectCode();
			if(Objects.nonNull(post.getAttachment())) {
				mailBody += "\n" + "View Attachment: " + HOST_URL + "/post/attachment/" + post.getUniqueKey();
			}
		}
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(SENDER);
		mailMessage.setSubject(subject);
		mailMessage.setText(mailBody);
		
		for (Student s : students) {
			mailMessage.setTo(s.getEmail());
			mailSender.send(mailMessage);
		}
	}
	
}
