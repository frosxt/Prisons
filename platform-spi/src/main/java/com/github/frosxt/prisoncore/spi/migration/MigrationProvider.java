package com.github.frosxt.prisoncore.spi.migration;

import java.util.List;

public interface MigrationProvider {
    List<ModuleMigration> migrations(String moduleId);
}
