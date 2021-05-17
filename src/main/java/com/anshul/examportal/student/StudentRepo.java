package com.anshul.examportal.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.lang.String;

import javax.transaction.Transactional;

public interface StudentRepo extends JpaRepository<Student, String> {
	
	Student getOneByEmail(String email);
	
	@Transactional
	@Modifying
	@Query(value = "delete from mcq_answer where roll_no = :rollNo", nativeQuery = true)
	void deleteFromMCQ(@Param("rollNo") String rollNo);
	
	@Transactional
	@Modifying
	@Query(value = "delete from subjective_answer where roll_no = :rollNo", nativeQuery = true)
	void deleteFromSubjective(@Param("rollNo") String rollNo);
	
}
