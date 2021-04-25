package com.anshul.examportal.mcq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MCQ_Q_Controller {
	
	@Autowired
	MCQ_Q_Repo repo;
	
	@CrossOrigin
	@GetMapping("/mcq-questions")
	public List<MCQ_Questions> getQuestions(){
		List<MCQ_Questions> questions = repo.findAll();
		return questions;
	}
}
