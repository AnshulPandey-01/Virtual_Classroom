package com.anshul.examportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.anshul.examportal.mcq.MCQ_Q_Repo;
import com.anshul.examportal.mcq.MCQ_Questions;

@Controller
public class HomeController {
	
	@Autowired
	MCQ_Q_Repo repo;
	
	@RequestMapping("/")
	public String home() {
		return "add_questions";
	}
	
	@PostMapping("add-question")
	public String addQuestion(@ModelAttribute MCQ_Questions q) {
		//repo.save(q);
		System.out.println(q.toString());
		return "result";
	}
	
}
