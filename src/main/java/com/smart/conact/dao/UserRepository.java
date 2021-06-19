package com.smart.conact.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.conact.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	@Query("SELECT u from User u WHERE u.email = :email")
	public User getUserByUserName(@Param("email")String email);
}
