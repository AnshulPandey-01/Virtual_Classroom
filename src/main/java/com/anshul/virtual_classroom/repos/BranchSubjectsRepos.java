package com.anshul.virtual_classroom.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.BranchSubjects;

@Repository
public interface BranchSubjectsRepos extends JpaRepository<BranchSubjects, String> {
	
	@Modifying
	@Query(value = "UPDATE branch_subjects SET branch = :newBranch, subjects = string_to_array(:subjects, '|,|') where branch = :oldBranch", nativeQuery = true)
	void updateBranchAndSubjects(@Param("oldBranch") String oldBranch, @Param("newBranch") String newBranch, @Param("subjects") String subjects);
	
}
