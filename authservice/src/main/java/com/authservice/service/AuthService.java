package com.authservice.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.authservice.dto.APIResponse;
import com.authservice.dto.UserDto;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public APIResponse<String> register(UserDto userDto){

		APIResponse<String> response = new APIResponse<>();

		if(userRepository.existsByUsername(userDto.getUsername())) {
			response.setMessage("Registration Failed");
			response.setStatus(409); // 409 Conflict (Standard REST code)
			response.setData("User with this username already exists");
			return response;
		}

		if(userRepository.existsByEmail(userDto.getEmail())) {
			response.setMessage("Registration Failed");
			response.setStatus(409); // 409 Conflict
			response.setData("Registration with this Email ID already exists");
			return response;
		}

		String encryptedPassword = passwordEncoder.encode(userDto.getPassword());

		User user = new User();
		BeanUtils.copyProperties(userDto, user);
		user.setPassword(encryptedPassword);

		// SECURE: Force public registrations to be ROLE_USER.
		// Never trust the role sent from the frontend JSON.
		user.setRole("ROLE_USER");

		userRepository.save(user);

		response.setMessage("Registration Completed");
		response.setStatus(201); // 201 Created
		response.setData("User has been registered successfully");
		return response;
	}
}