package com.anshul.virtual_classroom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class Test {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "test_id")
	private int testId;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false)
	private String password;
	
	@Column(name = "is_subjective", nullable = false, columnDefinition="boolean")
	private boolean isSubjective;
	
	@Column(nullable = false)
	private int duration; // duration in minutes
	
	@Column(name = "schedule_on", nullable = false)
	private String scheduleOn;
	
	@Column(name = "result_on", nullable = false)
	private String resultOn;
	
	@Column(name = "created_by", nullable = false)
	private String createdBy;
	
	@Column(name = "subject_code", nullable = false)
	private String subjectCode;
	
	@Column(nullable = false)
	private String branch;
	
	@Column(nullable = false)
	private int sem;
	
	@Column(nullable = false)
	private String section;
	
	@Column(nullable = true)
	private int marks;
	
	@Column(name = "negative_marks", nullable = true)
	private int negativeMarks;
	
}
