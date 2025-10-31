package com.skyapi.weathernetworkapi.ip2location;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class IP2LocationTest {
    private String dataBaseFile = "ip2locationdatabase/IP2LOCATIONLITEDB3.BIN";

    @Test
    public void testInvalidIP() throws IOException {
        IP2Location ip2Location = new IP2Location();
        ip2Location.Open(dataBaseFile);

        String ipAddress = "abs";
        IPResult ipResult = ip2Location.IPQuery(ipAddress);

        assertThat(ipResult.getStatus()).isEqualTo("INVALID_IP_ADDRESS");
        System.out.println(ipResult);
    }

    @Test
    public void testValidIP() throws IOException {
        IP2Location ip2Location = new IP2Location();
        ip2Location.Open(dataBaseFile);

        String ipAddress = "108.30.178.78"; //New York IP address
        IPResult ipResult = ip2Location.IPQuery(ipAddress);

        assertThat(ipResult.getStatus()).isEqualTo("OK");
        assertThat(ipResult.getCity()).isEqualTo("New York City");
        System.out.println(ipResult);
    }

    @Test
    public void testValidIP2() throws IOException {
        IP2Location ip2Location = new IP2Location();
        ip2Location.Open(dataBaseFile);

        String ipAddress = "103.48.198.141"; //Delhi IP address
        IPResult ipResult = ip2Location.IPQuery(ipAddress);

        assertThat(ipResult.getStatus()).isEqualTo("OK");
        assertThat(ipResult.getCity()).isEqualTo("Delhi");
        System.out.println(ipResult);
    }
}
