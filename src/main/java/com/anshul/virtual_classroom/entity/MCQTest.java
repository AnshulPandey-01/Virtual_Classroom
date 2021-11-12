package com.anshul.virtual_classroom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.anshul.virtual_classroom.utility.test.TestContainer;
import com.vladmihalcea.hibernate.type.array.StringArrayType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@TypeDefs({
    @TypeDef(
        name = "string-array",
        typeClass = StringArrayType.class
    )
})

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
	
	@Type(type = "string-array")
	@Column(nullable = true, columnDefinition = "text[]")
	private String[] options;
	
	@Column(name = "correct_option", nullable = false)
	private String correctOption;
	
}
