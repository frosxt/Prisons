package com.github.frosxt.prisoncore.spi.config;

import java.nio.file.Path;
import java.util.Map;

public interface ConfigSource {
    Map<String, Object> load(Path path) throws Exception;
    void save(Path path, Map<String, Object> data) throws Exception;
}
