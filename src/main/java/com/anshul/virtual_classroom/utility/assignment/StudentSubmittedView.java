package com.anshul.virtual_classroom.utility.assignment;

public interface StudentSubmittedView {
	
	public String getAssignmentUniqueKey();
	public String getTitle();
	public String getScheduleOn();
	public String getDueOn();
	public String getSubjectCode();
	public boolean getLateSubmission();
	public int getScore();
	public int getMaxMarks();
	
}
