package com.anshul.virtual_classroom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.anshul.virtual_classroom.utility.TestContainer;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "Subjective_Test")
public class SubjectiveTest implements TestContainer {
	
	@Id
	@Column(name = "question_id")
	private String questionId;

	@Column(name = "test_id", nullable = false)
	private int testId;
	
	@Column(nullable = false)
	private String question;
	
	@Column(nullable = false)
	private int marks;
	
}
