package com.authservice.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.authservice.dto.APIResponse;
import com.authservice.dto.LoginDto;
import com.authservice.dto.UserDto;
import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import com.authservice.service.AuthService;
import com.authservice.service.JwtService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authManager;

	@Autowired
	private AuthService authService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository repository;

	@PostMapping("/register")
	public ResponseEntity<APIResponse<String>> register(@RequestBody UserDto dto) {
		APIResponse<String> response = authService.register(dto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<APIResponse<String>> login(@RequestBody LoginDto dto) {
		APIResponse<String> response = new APIResponse<>();

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());
		try {
			Authentication authenticate = authManager.authenticate(token);

			if (authenticate.isAuthenticated()) {
				String jwtToken = jwtService.generateToken(dto.getUsername(),
						authenticate.getAuthorities().iterator().next().getAuthority());

				response.setMessage("Login Successful");
				response.setStatus(200);
				response.setData(jwtToken);
				return ResponseEntity.ok(response);
			}
		} catch (Exception e) {
			// Fall through to failure response
		}

		response.setMessage("Authentication Failed");
		response.setStatus(401);
		response.setData("Invalid username or password");
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@GetMapping("/get-user")
	public ResponseEntity<UserDto> getUserByUserName(@RequestParam String username) {
		User user = repository.findByUsername(username);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// SECURE: Map Entity to DTO to avoid leaking the hashed password
		UserDto safeUserDto = new UserDto();
		BeanUtils.copyProperties(user, safeUserDto);
		safeUserDto.setPassword(null); // Explicitly clear password field

		return ResponseEntity.ok(safeUserDto);
	}
}