package dev.zerojdk.domain.model.catalog.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Release(String tagName, List<Asset> assets) {
}
