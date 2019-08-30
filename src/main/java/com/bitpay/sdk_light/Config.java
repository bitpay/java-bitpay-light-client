package com.bitpay.sdk_light;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Config {
    private String _environment;
    private JsonNode _envConfig;

    public Config() {
    }

    @JsonIgnore
    public String getEnvironment() {
        return _environment;
    }

    @JsonProperty("Environment")
    public void setEnvironment(String environment) {
        this._environment = environment;
    }

    @JsonIgnore
    public JsonNode getEnvConfig(String env) {
        return _envConfig.path(env);
    }

    @JsonProperty("EnvConfig")
    public void setEnvConfig(JsonNode envConfig) {
        this._envConfig = envConfig;
    }
}