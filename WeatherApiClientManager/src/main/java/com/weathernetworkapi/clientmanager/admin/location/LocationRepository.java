package com.weathernetworkapi.clientmanager.admin.location;

import java.util.List;

import com.skyapi.weathernetworkapi.common.Location;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface LocationRepository extends CrudRepository<Location, String> {

	@Query("""
			SELECT NEW Location(l.code, l.cityName, l.regionName, l.countryName, l.countryCode)
			FROM Location l WHERE l.enabled = true
			AND l.trashed = false AND (l.code LIKE %?1% OR l.cityName LIKE %?1%
			OR l.countryName LIKE %?1% OR l.countryCode LIKE %?1%)			
			""")
	public List<Location> search(String keyword);
	
	@Query("SELECT l FROM Location l WHERE l.enabled=true AND l.trashed=false AND l.code=?1")
	public Location findByCodeEnabledUntrashed(String code);
}
