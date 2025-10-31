package com.skyapi.weathernetworkapi.clientapp;

import com.skyapi.weathernetworkapi.common.clientmanager.ClientApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Rollback(value = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ClientAppRepositoryTest {

    @Autowired
    private ClientAppRepository clientAppRepository;

    @Test
    @WithMockUser(value = "spring")
    public void testFindByClientId404NotFound() {
        String clientId = "zzcd";
        Optional<ClientApp> clientApp = clientAppRepository.findByClientId(clientId);

        assertThat(clientApp.isPresent()).isFalse();
    }

    @Test
    public void testFindByClientIdSuccess() {
        String clientId = "gr4TbWEFGFpfZvRrbeyB";
        Optional<ClientApp> clientApp = clientAppRepository.findByClientId(clientId);

        assertThat(clientApp.isPresent()).isTrue();
    }
}
