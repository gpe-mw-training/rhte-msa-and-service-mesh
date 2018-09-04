package com.redhat.developer.demos.catalog;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CatalogVerticleTest {

    @Test
    public void parseContainerIdFromHostname() {
        assertThat(CatalogVerticle.parseContainerIdFromHostname("catalog-v1-abcdef"), equalTo("abcdef"));
        assertThat(CatalogVerticle.parseContainerIdFromHostname("catalog-v2-abcdef"), equalTo("abcdef"));
        assertThat(CatalogVerticle.parseContainerIdFromHostname("catalog-v10-abcdef"), equalTo("abcdef"));
        assertThat(CatalogVerticle.parseContainerIdFromHostname("unknown"), equalTo("unknown"));
        assertThat(CatalogVerticle.parseContainerIdFromHostname("localhost"), equalTo("localhost"));
    }

}
