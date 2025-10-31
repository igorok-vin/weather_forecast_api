package com.weathernetworkapi.clientmanager.admin.user;

import java.util.List;

import com.skyapi.weathernetworkapi.common.clientmanager.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRestController {

	private UserService service;

	@Autowired
	public UserRestController(UserService service) {
		this.service = service;
	}	
	
	@PostMapping("/users/search")
	public ResponseEntity<?> search(String keyword) {
		System.out.println("User search keyword: " + keyword);
		
		List<User> result = service.searchAutoComplete(keyword);
		
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.ok(result);
	}
}
