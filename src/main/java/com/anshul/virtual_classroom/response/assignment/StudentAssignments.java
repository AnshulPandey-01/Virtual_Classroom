package com.anshul.virtual_classroom.response.assignment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class StudentAssignments {
	
	private String uniqueKey;
	private String title;
	private String content;
	private String createdBy;
	private String subjectCode;
	private String scheduleOn;
	private String dueOn;
	private int marks;
	
}
