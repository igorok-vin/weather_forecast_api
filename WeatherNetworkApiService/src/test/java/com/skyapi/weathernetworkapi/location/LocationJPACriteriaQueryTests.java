package com.skyapi.weathernetworkapi.location;

import com.skyapi.weathernetworkapi.SecurityConfigurationDataJPATests;
import com.skyapi.weathernetworkapi.SecurityConfigurationWebMvcTests;
import com.skyapi.weathernetworkapi.common.Location;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(SecurityConfigurationDataJPATests.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LocationJPACriteriaQueryTests {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void testCriteriaQuery() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Location> query = builder.createQuery(Location.class);

        //starting point. equivalent to: select * from
        Root<Location> root = query.from(Location.class);

        //prepare WHERE clause
        Predicate predicate = builder.equal(root.get("countryCode"), "US");

        //add WHERE
        query.where(predicate);

        //add ORDER BY
        query.orderBy(builder.asc(root.get("cityName")));

        TypedQuery<Location> typedQuery = entityManager.createQuery(query);
        //add Pagination
        typedQuery.setFirstResult(0);
        typedQuery.setMaxResults(3);
        List<Location> locations = typedQuery.getResultList();

        assertThat(locations).isNotEmpty();
        locations.forEach(System.out::println);
    }

    @Test
    public void testCorrespondingJPQLToCriteriaQuery() {
        String jpql = "FROM Location WHERE countryCode = 'US' ORDER BY cityName ASC";
        TypedQuery<Location> typedQuery = entityManager.createQuery(jpql, Location.class);
        List<Location> locations = typedQuery.getResultList();
        assertThat(locations).isNotEmpty();
        locations.forEach(System.out::println);
    }
}
