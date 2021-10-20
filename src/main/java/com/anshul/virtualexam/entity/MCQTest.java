package com.anshul.virtualexam.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.anshul.virtualexam.utility.TestContainer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity(name = "MCQ_Test")
public class MCQTest implements TestContainer {
	
	@Id
	@Column(name = "question_id")
	private String questionId;
	
	@Column(name = "test_id", nullable = false)
	private int testId;
	
	@Column(nullable = false)
	private String question;
	
	@Column(nullable = false)
	private String option1;
	
	@Column(nullable = false)
	private String option2;
	
	@Column(nullable = true)
	private String option3;
	
	@Column(nullable = true)
	private String option4;
	
	@Column(name = "correct_option", nullable = false)
	private String correctOption;
	
}
