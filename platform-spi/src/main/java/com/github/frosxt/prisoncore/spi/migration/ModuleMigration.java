package com.github.frosxt.prisoncore.spi.migration;

public interface ModuleMigration {
    String moduleId();
    int fromVersion();
    int toVersion();
    void apply(MigrationContext context) throws Exception;
}
