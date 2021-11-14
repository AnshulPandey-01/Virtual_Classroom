package com.anshul.virtual_classroom.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.BranchSubjects;

@Repository
public interface BranchSubjectsRepos extends JpaRepository<BranchSubjects, Integer> {
	
	BranchSubjects getOneByBranch(String branch);
	
}
