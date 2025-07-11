package dev.zerojdk.domain.model.catalog.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Asset(String browserDownloadUrl) { }
