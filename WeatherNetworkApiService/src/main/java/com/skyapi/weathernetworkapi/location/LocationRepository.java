package com.skyapi.weathernetworkapi.location;

import com.skyapi.weathernetworkapi.common.Location;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends FilterableLocationRepository, CrudRepository<Location, String>, PagingAndSortingRepository<Location, String> {

   @Query("SELECT l FROM Location l WHERE l.trashed = false")
   @Deprecated
    public List<Location> findAllUntrashed();

   @Deprecated
    @Query("SELECT l FROM Location l WHERE l.trashed = false")
    public Page<Location> findAllUntrashed(Pageable pageable);

   @Query("SELECT l FROM Location l WHERE l.trashed = false AND l.code=?1")
   public Location findByCode(String code);

    @Modifying
    @Query("UPDATE Location l SET l.trashed = true WHERE l.code = ?1")
    public void trashedByCode(String code);

    @Query("SELECT l FROM Location l WHERE l.countryCode=?1 AND l.cityName=?2 AND l.trashed=false")
    public Location findByCountryCodeAndCityName(String countryCode, String cityName);
}
