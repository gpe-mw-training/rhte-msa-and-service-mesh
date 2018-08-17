package com.redhat.coolstore.api.gateway;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ActuatorEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void healtCheckEndpoint() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange("/health", HttpMethod.GET, null, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(HttpStatus.SC_OK));
        JsonNode node = new ObjectMapper(new JsonFactory()).readTree(response.getBody());
        assertThat(node.get("status").asText(), equalTo("UP"));
    }

    @Test
    public void infoEndpointIsNotEnabled() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange("/info", HttpMethod.GET, null, String.class);
        assertThat(response.getStatusCodeValue(), equalTo(HttpStatus.SC_NOT_FOUND));
    }

    @Test
    public void pauseEndpointIsNotEnabled() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange("/pause", HttpMethod.POST, null, String.class);
        assertThat(response.getStatusCodeValue(), anyOf(equalTo(HttpStatus.SC_NOT_FOUND), equalTo(HttpStatus.SC_UNAUTHORIZED)));
    }

    @Test
    public void restartEndpointIsNotEnabled() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange("/restart", HttpMethod.POST, null, String.class);
        assertThat(response.getStatusCodeValue(), anyOf(equalTo(HttpStatus.SC_NOT_FOUND), equalTo(HttpStatus.SC_UNAUTHORIZED)));
    }

    @Test
    public void resumeEndpointIsNotEnabled() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange("/resume", HttpMethod.POST, null, String.class);
        assertThat(response.getStatusCodeValue(), anyOf(equalTo(HttpStatus.SC_NOT_FOUND), equalTo(HttpStatus.SC_UNAUTHORIZED)));
    }

    @Test
    public void refreshEndpointIsNotEnabled() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange("/refresh", HttpMethod.POST, null, String.class);
        assertThat(response.getStatusCodeValue(), anyOf(equalTo(HttpStatus.SC_NOT_FOUND), equalTo(HttpStatus.SC_UNAUTHORIZED)));
    }
}
