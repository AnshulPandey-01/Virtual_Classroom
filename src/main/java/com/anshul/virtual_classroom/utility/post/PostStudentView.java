package com.anshul.virtual_classroom.utility.post;

public interface PostStudentView {
	
	public String getUniqueKey();
	public String getTitle();
	public String getContent();
	public String getCreatedBy();
	public String getCreatedAt();
	public String getSubjectCode();
	public boolean getAttachment();
	
}
