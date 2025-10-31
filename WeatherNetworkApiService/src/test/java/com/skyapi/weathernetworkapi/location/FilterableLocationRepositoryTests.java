package com.skyapi.weathernetworkapi.location;

import com.skyapi.weathernetworkapi.common.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class FilterableLocationRepositoryTests {

    @Autowired
    private LocationRepository locationRepository;

    @Test
    public void testListWithDefaults() {
        int pageSize = 5;
        int pageNumber = 0;
        String sortField = "code";

        Sort sort = Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Location> page = locationRepository.listWithFilter(pageable, Collections.EMPTY_MAP);
        List<Location> content = page.getContent();

        System.out.println("Total elements: " + page.getTotalElements());
        assertThat(content.size()).isEqualTo(pageSize);
        System.out.println("pageable.getOffset() :" + pageable.getOffset());
        System.out.println("content.size(): " + content.size());
        assertThat(page.getTotalElements()).isGreaterThan(pageable.getOffset() + content.size());

        assertThat(content).isSortedAccordingTo(new Comparator<Location>() {

            @Override
            public int compare(Location l1, Location l2) {
                return l1.getCode().compareTo(l2.getCode());
            }
        });
        content.forEach(System.out::println);
    }

    @Test
    public void testListNoFilterSortedByCityName() {
        int pageSize = 5;
        int pageNumber = 0;
        String sortField = "cityName";

        Sort sort = Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Location> page = locationRepository.listWithFilter(pageable, Collections.EMPTY_MAP);
        List<Location> content = page.getContent();

        assertThat(content.size()).isEqualTo(pageSize);
        content.forEach(System.out::println);
        System.out.println("pageable.getOffset() :" + pageable.getOffset());
        System.out.println("content.size(): " + content.size());
        assertThat(page.getTotalElements()).isGreaterThan(pageable.getOffset() + content.size());
    }

    @Test
    public void testListFilteredCountryCodeSortedByCityName() {
        int pageSize = 2;
        int pageNumber = 0;
        String sortField = "cityName";
        String countryCode = "US";

        Sort sort = Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Map<String, Object> filterFields = new HashMap<>();
        filterFields.put("countryCode", countryCode);

        Page<Location> page = locationRepository.listWithFilter(pageable, filterFields);
        List<Location> content = page.getContent();

        assertThat(content.size()).isEqualTo(pageSize);
        content.forEach(location -> assertThat(location.getCountryCode()).isEqualTo(countryCode));
        content.forEach(System.out::println);
        System.out.println("pageable.getOffset() :" + pageable.getOffset());
        System.out.println("content.size(): " + content.size());
        assertThat(page.getTotalElements()).isEqualTo((long) (pageable.getOffset() + content.size()));
    }

    @Test
    public void testListFilteredCountryCodeAndEnabledSortedByCityName() {
        int pageSize = 2;
        int pageNumber = 0;
        String sortField = "cityName";
        String countryCode = "US";
        boolean enabled = true;

        Sort sort = Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Map<String, Object> filterFields = new HashMap<>();
        filterFields.put("countryCode", countryCode);
        filterFields.put("enabled", enabled);

        Page<Location> page = locationRepository.listWithFilter(pageable, filterFields);
        List<Location> content = page.getContent();

        assertThat(content.size()).isEqualTo(pageSize);
        content.forEach(location ->
        {
            assertThat(location.getCountryCode()).isEqualTo(countryCode);
            assertThat(location.isEnabled()).isEqualTo(enabled);
        });
        content.forEach(System.out::println);

        System.out.println("pageable.getOffset() :" + pageable.getOffset());
        System.out.println("content.size(): " + content.size());
        assertThat(page.getTotalElements()).isEqualTo(pageable.getOffset() + content.size());
    }
}
