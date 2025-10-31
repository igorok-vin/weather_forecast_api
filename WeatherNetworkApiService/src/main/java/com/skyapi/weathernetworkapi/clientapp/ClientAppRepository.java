package com.skyapi.weathernetworkapi.clientapp;

import com.skyapi.weathernetworkapi.common.clientmanager.ClientApp;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClientAppRepository extends CrudRepository<ClientApp, Integer> {

    @Query("SELECT ca FROM ClientApp ca WHERE ca.clientId = ?1 AND ca.enabled = true AND ca.trashed = false")
    public Optional<ClientApp> findByClientId(String clientId);
}
