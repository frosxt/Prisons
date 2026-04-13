package com.github.frosxt.prisoncore.runtime.bukkit;

import com.github.frosxt.prisoncore.api.platform.PlatformInfo;
import com.github.frosxt.prisoncore.command.api.CommandService;
import com.github.frosxt.prisoncore.command.bukkit.BukkitCommandService;
import com.github.frosxt.prisoncore.commons.bukkit.event.BukkitListenerHost;
import com.github.frosxt.prisoncore.commons.bukkit.item.PersistentDataAdapter;
import com.github.frosxt.prisoncore.config.api.ConfigService;
import com.github.frosxt.prisoncore.config.yaml.YamlConfigService;
import com.github.frosxt.prisoncore.kernel.Kernel;
import com.github.frosxt.prisoncore.kernel.KernelConfig;
import com.github.frosxt.prisoncore.kernel.config.CoreConfig;
import com.github.frosxt.prisoncore.kernel.container.session.SessionScopeManager;
import com.github.frosxt.prisoncore.menu.api.MenuService;
import com.github.frosxt.prisoncore.menu.bukkit.BukkitMenuListener;
import com.github.frosxt.prisoncore.menu.bukkit.BukkitMenuService;
import com.github.frosxt.prisoncore.message.api.MessageCatalog;
import com.github.frosxt.prisoncore.message.api.MessageService;
import com.github.frosxt.prisoncore.message.bukkit.BukkitMessageService;
import com.github.frosxt.prisoncore.message.core.DefaultMessageCatalog;
import com.github.frosxt.prisoncore.message.core.MessageDefinitionRegistry;
import com.github.frosxt.prisoncore.placeholder.api.PlaceholderService;
import com.github.frosxt.prisoncore.placeholder.bridge.PapiBridge;
import com.github.frosxt.prisoncore.placeholder.core.DefaultPlaceholderService;
import com.github.frosxt.prisoncore.player.api.PlayerProfile;
import com.github.frosxt.prisoncore.player.api.PlayerProfileService;
import com.github.frosxt.prisoncore.player.core.DefaultPlayerProfileService;
import com.github.frosxt.prisoncore.player.repository.JsonPlayerProfileRepository;
import com.github.frosxt.prisoncore.player.repository.MongoPlayerProfileRepository;
import com.github.frosxt.prisoncore.player.repository.SqlPlayerProfileRepository;
import com.github.frosxt.prisoncore.player.repository.SqlitePlayerProfileRepository;
import com.github.frosxt.prisoncore.runtime.bukkit.adapter.BukkitPlatformInfo;
import com.github.frosxt.prisoncore.runtime.bukkit.adapter.DefaultBukkitListenerHost;
import com.github.frosxt.prisoncore.runtime.bukkit.admin.CoreAdminCommands;
import com.github.frosxt.prisoncore.runtime.bukkit.bridge.BukkitEventBridge;
import com.github.frosxt.prisoncore.runtime.bukkit.listener.PlayerSessionListener;
import com.github.frosxt.prisoncore.scheduler.api.TaskOrchestrator;
import com.github.frosxt.prisoncore.scheduler.bukkit.BukkitTaskOrchestrator;
import com.github.frosxt.prisoncore.spi.storage.StorageBackend;
import com.github.frosxt.prisoncore.storage.api.Repository;
import com.github.frosxt.prisoncore.storage.json.JsonStorageBackendFactory;
import com.github.frosxt.prisoncore.storage.mongo.MongoBackend;
import com.github.frosxt.prisoncore.storage.mongo.MongoStorageBackendFactory;
import com.github.frosxt.prisoncore.storage.sql.SqlBackend;
import com.github.frosxt.prisoncore.storage.sql.SqlStorageBackendFactory;
import com.github.frosxt.prisoncore.storage.sqlite.SqliteBackend;
import com.github.frosxt.prisoncore.storage.sqlite.SqliteStorageBackendFactory;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class BukkitKernelBootstrap {
    private Kernel kernel;
    private BukkitTaskOrchestrator taskOrchestrator;

    public void onLoad(final JavaPlugin plugin) {
        final Logger logger = plugin.getLogger();
        final Path dataFolder = plugin.getDataFolder().toPath();
        final Path modulesDir = dataFolder.resolve("modules");

        final KernelConfig config = new KernelConfig(dataFolder, modulesDir, logger);
        this.kernel = new Kernel(config);

        logger.info("[PrisonCore] Kernel created. Loading...");
    }

    public void onEnable(final JavaPlugin plugin) {
        if (kernel == null) {
            plugin.getLogger().severe("[PrisonCore] Kernel was not created during onLoad!");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        try {
            saveDefaultResources(plugin);
            PersistentDataAdapter.setNamespace("prisoncore");
            registerBukkitServices(plugin);
            kernel.setCoreConfig(loadCoreConfig(plugin));
            kernel.bootstrap();
            registerPlayerProfileService(plugin);
            kernel.activate();
            registerCoreAdminCommands();
            registerBukkitListeners(plugin);
        } catch (final Exception e) {
            plugin.getLogger().severe("[PrisonCore] Failed to start kernel!");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private void saveDefaultResources(final JavaPlugin plugin) {
        plugin.getDataFolder().mkdirs();
        if (!new File(plugin.getDataFolder(), "messages.yml").exists()) {
            try {
                plugin.saveResource("messages.yml", false);
            } catch (final IllegalArgumentException ignored) {
                // Resource not bundled — will use defaults from MessageKey
            }
        }
    }

    private CoreConfig loadCoreConfig(final JavaPlugin plugin) {
        final File configFile = new File(plugin.getDataFolder(), "core.yml");
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.saveResource("core.yml", false);

            if (!configFile.exists()) {
                final YamlConfiguration defaults = new YamlConfiguration();
                defaults.set("debug", false);
                defaults.set("storage.backend", "json");
                defaults.set("economy.default-currency", "money");
                try {
                    defaults.save(configFile);
                } catch (final IOException e) {
                    plugin.getLogger().warning("[PrisonCore] Failed to create core.yml: " + e.getMessage());
                }
            }
        }

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
        final boolean debug = yaml.getBoolean("debug", false);
        final String backend = yaml.getString("storage.backend", "json");
        final String defaultCurrency = yaml.getString("economy.default-currency", "money");

        final LinkedHashMap<String, String> storageProps = new LinkedHashMap<>();
        final ConfigurationSection storageSection = yaml.getConfigurationSection("storage");
        if (storageSection != null) {
            for (final String key : storageSection.getKeys(false)) {
                if (!"backend".equals(key)) {
                    storageProps.put(key, storageSection.getString(key, ""));
                }
            }
        }

        return new CoreConfig(debug, backend, storageProps, defaultCurrency);
    }

    private void registerBukkitServices(final JavaPlugin plugin) {
        final Path dataFolder = plugin.getDataFolder().toPath();
        final Logger logger = plugin.getLogger();

        taskOrchestrator = new BukkitTaskOrchestrator(plugin);
        kernel.registerService(TaskOrchestrator.class, taskOrchestrator);

        kernel.registerService(BukkitListenerHost.class, new DefaultBukkitListenerHost(plugin));

        final ConfigService configService = new YamlConfigService(dataFolder, logger);
        kernel.registerService(ConfigService.class, configService);

        final MessageDefinitionRegistry messageDefaults = new MessageDefinitionRegistry();
        final MessageCatalog messageCatalog = new DefaultMessageCatalog(configService, messageDefaults, logger);
        kernel.registerService(MessageCatalog.class, messageCatalog);

        final MessageService messageService = new BukkitMessageService(messageCatalog);
        kernel.registerService(MessageService.class, messageService);
        kernel.registerService(CommandService.class, new BukkitCommandService(messageService));
        kernel.registerService(MenuService.class, new BukkitMenuService());
        kernel.registerService(PlatformInfo.class,
                new BukkitPlatformInfo(kernel, plugin.getDescription().getVersion()));

        kernel.storageRegistry().registerFactory(new JsonStorageBackendFactory());
        kernel.storageRegistry().registerFactory(new SqliteStorageBackendFactory(logger));
        kernel.storageRegistry().registerFactory(new SqlStorageBackendFactory(logger));
        kernel.storageRegistry().registerFactory(new MongoStorageBackendFactory(logger));

        final PlaceholderService placeholderService = new DefaultPlaceholderService();
        kernel.registerService(PlaceholderService.class, placeholderService);

        if (PapiBridge.isAvailable()) {
            placeholderService.registerResolver("papi", PapiBridge.createResolver());
            logger.info("[PrisonCore] PlaceholderAPI bridge enabled.");
        }
    }

    private void registerPlayerProfileService(final JavaPlugin plugin) {
        final Path dataFolder = plugin.getDataFolder().toPath();
        final Logger logger = plugin.getLogger();
        final String backend = kernel.coreConfig().storageBackend();

        final ExecutorService playerIo = Executors.newFixedThreadPool(2, r -> {
            final Thread t = new Thread(r, "PrisonCore-PlayerIO");
            t.setDaemon(true);
            return t;
        });

        final Repository<UUID, PlayerProfile> profileRepo = createProfileRepository(backend, dataFolder, logger, playerIo);
        final DefaultPlayerProfileService profileService = new DefaultPlayerProfileService(profileRepo, playerIo, logger);
        kernel.registerService(PlayerProfileService.class, profileService);
    }

    private Repository<UUID, PlayerProfile> createProfileRepository(
            final String backend, final Path dataFolder, final Logger logger,
            final ExecutorService playerIo) {
        final Map<String, Object> storageConfig = new LinkedHashMap<>(kernel.coreConfig().storageProperties());

        switch (backend) {
            case "sqlite": {
                try {
                    final StorageBackend sb = kernel.storageRegistry().getOrCreate("sqlite", storageConfig);
                    logger.info("[PrisonCore] Using SQLite backend for player profiles");
                    return new SqlitePlayerProfileRepository((SqliteBackend) sb, playerIo, logger);
                } catch (final Exception e) {
                    logger.warning("[PrisonCore] SQLite backend failed, falling back to JSON: " + e.getMessage());
                    break;
                }
            }
            case "sql": {
                try {
                    final StorageBackend sb = kernel.storageRegistry().getOrCreate("sql", storageConfig);
                    logger.info("[PrisonCore] Using SQL backend for player profiles");
                    return new SqlPlayerProfileRepository((SqlBackend) sb, playerIo, logger);
                } catch (final Exception e) {
                    logger.warning("[PrisonCore] SQL backend failed, falling back to JSON: " + e.getMessage());
                    break;
                }
            }
            case "mongo": {
                try {
                    final StorageBackend sb = kernel.storageRegistry().getOrCreate("mongo", storageConfig);
                    logger.info("[PrisonCore] Using MongoDB backend for player profiles");
                    return new MongoPlayerProfileRepository((MongoBackend) sb, playerIo, logger);
                } catch (final Exception e) {
                    logger.warning("[PrisonCore] MongoDB backend failed, falling back to JSON: " + e.getMessage());
                    break;
                }
            }
            case "json":
            default:
                break;
        }

        final Path profilesDir = dataFolder.resolve("data").resolve("players");
        return new JsonPlayerProfileRepository(profilesDir, logger);
    }

    private void registerCoreAdminCommands() {
        new CoreAdminCommands(kernel).register();
    }

    private void registerBukkitListeners(final JavaPlugin plugin) {
        final BukkitEventBridge eventBridge = new BukkitEventBridge(kernel.eventBus());
        Bukkit.getPluginManager().registerEvents(eventBridge, plugin);

        final MenuService menuService = kernel.container().resolve(MenuService.class);
        if (menuService instanceof final BukkitMenuService bukkitMenu) {
            Bukkit.getPluginManager().registerEvents(new BukkitMenuListener(bukkitMenu), plugin);
        }

        final PlayerProfileService profileService = kernel.container().resolve(PlayerProfileService.class);
        final SessionScopeManager sessionMgr = kernel.sessionScopeManager();
        Bukkit.getPluginManager().registerEvents(
                new PlayerSessionListener(profileService, sessionMgr, plugin), plugin);
    }

    public void onDisable(final JavaPlugin plugin) {
        if (kernel != null) {
            try {
                kernel.shutdown();
            } catch (final Exception e) {
                plugin.getLogger().severe("[PrisonCore] Error during kernel shutdown!");
                e.printStackTrace();
            }
        }
        if (taskOrchestrator != null) {
            taskOrchestrator.shutdown();
        }
    }

    public Kernel kernel() {
        return kernel;
    }
}
