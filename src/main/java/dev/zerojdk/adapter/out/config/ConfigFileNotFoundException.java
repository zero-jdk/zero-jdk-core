package dev.zerojdk.adapter.out.config;

public class ConfigFileNotFoundException extends RuntimeException {
    public ConfigFileNotFoundException(Exception cause) {
        super(cause);
    }
}
