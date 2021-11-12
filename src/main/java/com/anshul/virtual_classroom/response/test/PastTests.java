package com.anshul.virtual_classroom.response.test;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PastTests {
	
	private int testId;
	private String title;
	private String subjectCode;
	private boolean isSubjective;
	private String resultOn;
	private boolean isPresent;
	
	public PastTests(int testId, String title, String subjectCode, boolean isSubjective, String resultOn) {
		this.testId = testId;
		this.title = title;
		this.subjectCode = subjectCode;
		this.isSubjective = isSubjective;
		this.resultOn = resultOn;
	}
	
	public PastTests(int testId, String title, String subjectCode, boolean isSubjective, String resultOn, boolean isPresent) {
		this(testId, title, subjectCode, isSubjective, resultOn);
		this.isPresent = isPresent;
	}
	
}
