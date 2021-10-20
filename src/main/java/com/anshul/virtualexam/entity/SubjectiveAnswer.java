package com.anshul.virtualexam.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import com.anshul.virtualexam.utility.AnswerId;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity(name = "Subjective_Answer")
@IdClass(AnswerId.class)
public class SubjectiveAnswer {

	@Id
	@Column(name = "roll_no", nullable = false)
	private String rollNo;
	
	@Column(name = "test_id", nullable = false)
	private int testId;
	
	@Id
	@Column(name = "question_id", nullable = false)
	private String questionId;
	
	@Column(nullable = true)
	private String answer;
	
	@Column(nullable = false)
	private int score = -1;
	
}
