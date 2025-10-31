package com.weathernetworkapi.clientmanager.admin.location;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.skyapi.weathernetworkapi.common.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class LocationRepositoryTests {

	@Autowired private LocationRepository repo;
	
	@Test
	public void testSearchFound() {
		String keyword = "LACA";
		List<Location> result = repo.search(keyword);
		
		assertThat(result).isNotEmpty();
		result.forEach(System.out::println);
	}
	
	@Test
	public void testSearchNotFound() {
		String keyword = "KAu";
		List<Location> result = repo.search(keyword);
		
		assertThat(result).isEmpty();
	}	
	
	@Test
	public void testFindByCodeNotFound() {
		String code = "XYZ123";
		Location location = repo.findByCodeEnabledUntrashed(code);
		
		assertThat(location).isNull();
	}
	
	@Test
	public void testFindByCodeFound() {
		String code = "ES";
		Location location = repo.findByCodeEnabledUntrashed(code);
		
		assertThat(location).isNotNull();
		System.out.println(location);
	}	
}
