package com.anshul.virtual_classroom.entity;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.array.StringArrayType;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@ToString
@Entity
public class BranchSubjects {
	
	@Id
	private String branch;
	
	@Type(type = "string-array")
	@Column(nullable = false, columnDefinition = "text[]")
	private String[] subjects;

	public String subjectsToString() {
		StringBuilder sb = new StringBuilder();
		
		Arrays.asList(subjects).forEach(sub -> {
			sb.append(sub + "|,|");
		});
		sb.setLength(sb.length()-3);
		
		return sb.toString();
	}
	
}
