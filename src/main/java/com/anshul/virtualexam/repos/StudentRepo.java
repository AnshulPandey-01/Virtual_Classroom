package com.anshul.virtualexam.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.lang.String;
import java.util.List;

import javax.transaction.Transactional;

import com.anshul.virtualexam.entity.Student;

public interface StudentRepo extends JpaRepository<Student, String> {
	
	Student getOneByEmail(String email);
	
	@Query(value = "select count(*) from student where roll_no= :rollNo or email= :email", nativeQuery = true)
	int checkStudentExists(@Param("rollNo") String rollNo, @Param("email") String email);
	
	List<Student> findBySemAndBranchAndSection(int sem, String branch, String section);
	
	@Transactional
	@Modifying
	@Query(value = "delete from mcq_answer where roll_no = :rollNo", nativeQuery = true)
	void deleteFromMCQ(@Param("rollNo") String rollNo);
	
	@Transactional
	@Modifying
	@Query(value = "delete from subjective_answer where roll_no = :rollNo", nativeQuery = true)
	void deleteFromSubjective(@Param("rollNo") String rollNo);
	
}
