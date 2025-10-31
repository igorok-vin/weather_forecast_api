package com.weathernetworkapi.clientmanager.admin.location;

import java.util.List;

import com.skyapi.weathernetworkapi.common.Location;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

	private LocationRepository repo;

	public LocationService(LocationRepository repo) {
		this.repo = repo;
	}
	
	public List<Location> searchAutoComplete(String keyword) {
		return repo.search(keyword);
	}
	
	public Location get(String code) throws LocationNotFoundException {
		Location location = repo.findByCodeEnabledUntrashed(code);
		if (location == null) {
			throw new LocationNotFoundException("No location found with the given code: " + code);
		}
		
		return location;
	}
}
