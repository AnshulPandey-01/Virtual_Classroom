package com.anshul.virtualexam.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anshul.virtualexam.entity.Admin;

public interface AdminRepo extends JpaRepository<Admin, String> {

}
