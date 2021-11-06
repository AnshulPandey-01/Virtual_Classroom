package com.anshul.virtual_classroom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.anshul.virtual_classroom.utility.TestContainer;

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
	
	@Column(name = "q_options", nullable = false)
	private String[] options;
	
	@Column(name = "correct_option", nullable = false)
	private String correctOption;
	
}
