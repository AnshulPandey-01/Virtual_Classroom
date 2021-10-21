package com.anshul.virtual_classroom.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anshul.virtual_classroom.entity.Admin;

public interface AdminRepo extends JpaRepository<Admin, String> {

}
