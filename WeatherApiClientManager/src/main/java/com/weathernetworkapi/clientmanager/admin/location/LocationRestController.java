package com.weathernetworkapi.clientmanager.admin.location;

import java.util.List;

import com.skyapi.weathernetworkapi.common.Location;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LocationRestController {

	private LocationService service;

	public LocationRestController(LocationService service) {
		this.service = service;
	}
	
	@PostMapping("/locations/search")
	public ResponseEntity<?> search(String keyword) {
		System.out.println("Location search keyword: " + keyword);
		
		List<Location> result = service.searchAutoComplete(keyword);
		System.out.println(result);
		
		if (result.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.ok(result);
	}
}
