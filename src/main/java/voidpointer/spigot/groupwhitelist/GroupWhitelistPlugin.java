package voidpointer.spigot.groupwhitelist;

import co.aikar.commands.PaperCommandManager;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import voidpointer.spigot.groupwhitelist.command.GroupWhitelistCommand;
import voidpointer.spigot.groupwhitelist.config.LocaleConfig;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;
import voidpointer.spigot.groupwhitelist.config.loader.ConfigLoader;
import voidpointer.spigot.groupwhitelist.config.reload.AutoReloadConfigService;
import voidpointer.spigot.groupwhitelist.event.WhitelistGroupRemoveEvent;
import voidpointer.spigot.groupwhitelist.listener.*;
import voidpointer.spigot.groupwhitelist.locale.Locale;
import voidpointer.spigot.groupwhitelist.service.WhitelistService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class GroupWhitelistPlugin extends JavaPlugin implements WhitelistConfig.SecretSettings.SecretChecksumLoader {
    public static final MessageDigest SHA512;

    static {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("sha512");
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            log.error("Could find SHA-512 algorithm, secret checks will fail.", noSuchAlgorithmException);
        }
        SHA512 = md;
    }

    private AutoReloadConfigService autoReloadConfigService;

    private ConfigLoader<WhitelistConfig> whitelistConfigLoader;
    private WhitelistConfig whitelistConfig;
    private LocaleConfig localeConfig;

    private WhitelistService whitelistService;

    private PingListener pingListener;
    private PlayerListener playerListener;
    private WhitelistListener whitelistListener;
    private GroupWhitelistCommand groupWhitelistCommand;

    @Override public void onLoad() {
        loadConfiguration();
        getSLF4JLogger().info("Loaded");
    }

    @Override public void onEnable() {
        whitelistService = new WhitelistService(whitelistConfig, whitelistConfigLoader);
        /* events */
        this.playerListener = new PlayerListener(whitelistConfig, localeConfig).register(this);
        this.whitelistListener = new WhitelistListener(this, whitelistConfig, localeConfig).register(this);
        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null)
            this.pingListener = new ProtocolPingListener(whitelistConfig).register(this);
        else
            this.pingListener = new PaperPingListener(whitelistConfig).register(this);
        /* commands */
        var cmdManager = new PaperCommandManager(this);
        cmdManager.registerDependency(WhitelistConfig.class, whitelistConfig);
        cmdManager.registerDependency(WhitelistService.class, whitelistService);
        cmdManager.registerDependency(Locale.class, localeConfig);
        groupWhitelistCommand = new GroupWhitelistCommand();
        cmdManager.registerCommand(groupWhitelistCommand);

        getSLF4JLogger().info("Enabled");
    }

    @Override public void onDisable() {
        if (autoReloadConfigService != null)
            autoReloadConfigService.shutdown();
        else
            getSLF4JLogger().error("Auto reload config service was not initialized, skipped shutdown");
        if (whitelistListener != null)
            whitelistListener.shutdown();
        getSLF4JLogger().info("Disabled");
    }

    private void loadConfiguration() {
        getSLF4JLogger().trace("Loading configuration");
        /* TODO migration */
        this.whitelistConfigLoader = new ConfigLoader<>(getDataFolder().toPath(), WhitelistConfig.class);
        this.whitelistConfig = onWhitelistConfigLoaded(whitelistConfigLoader.loadAndSaveDefaultIfNotExists());
        var localeConfigLoader = new ConfigLoader<>(getDataFolder().toPath(), LocaleConfig.class);
        this.localeConfig = localeConfigLoader.loadAndSaveDefaultIfNotExists();

        autoReloadConfigService = new AutoReloadConfigService();
        if (autoReloadConfigService.startWatchingForModifications()) {
            autoReloadConfigService.subscribeToReload(whitelistConfigLoader, this::onConfigReload);
            autoReloadConfigService.subscribeToReload(localeConfigLoader, this::onLocaleReload);
        }
    }

    private void onConfigReload(final WhitelistConfig whitelistConfig) {
        onWhitelistConfigLoaded(whitelistConfig);
        if (pingListener != null)
            pingListener.setWhitelistConfig(whitelistConfig);
        if (playerListener != null)
            playerListener.setWhitelistConfig(whitelistConfig);
        if (whitelistListener != null)
            whitelistListener.setWhitelistConfig(whitelistConfig);
        if (whitelistService != null)
            whitelistService.setWhitelistConfig(whitelistConfig);
        if (groupWhitelistCommand != null)
            groupWhitelistCommand.setWhitelistConfig(whitelistConfig);
        Set<String> newlyWhitelistedGroups = whitelistConfig.whitelistGroups();
        Set<String> removedGroups = this.whitelistConfig.whitelistGroups().stream()
                .filter((group) -> !newlyWhitelistedGroups.contains(group))
                .collect(Collectors.toSet());
        if (!removedGroups.isEmpty()) {
            var removedGroupsEvent = new WhitelistGroupRemoveEvent(removedGroups, newlyWhitelistedGroups);
            getServer().getPluginManager().callEvent(removedGroupsEvent);
        }
        this.whitelistConfig = onWhitelistConfigLoaded(whitelistConfig);
        getSLF4JLogger().info("Reloaded whitelist config");
    }

    private void onLocaleReload(final LocaleConfig localeConfig) {
        if (playerListener != null)
            playerListener.setLocale(localeConfig);
        if (whitelistListener != null)
            whitelistListener.setLocale(localeConfig);
        getSLF4JLogger().info("Reloaded locale config");
    }

    private WhitelistConfig onWhitelistConfigLoaded(WhitelistConfig whitelistConfig) {
        if (whitelistConfig.secretSettings().type() != WhitelistConfig.SecretSettings.Type.FILE)
            return whitelistConfig;
        WhitelistConfig.SecretSettings secretSettings = whitelistConfig.secretSettings();
        String pathToSecretFile = secretSettings.pathToSecretFile();
        if (pathToSecretFile != null)
            updateCachedChecksum(secretSettings, sha512(Path.of(pathToSecretFile)));
        return whitelistConfig;
    }

    private byte @Nullable[] sha512(@NotNull Path pathToSecretFile) {
        if (SHA512 == null)
            return null;
        if (!Files.isReadable(pathToSecretFile)) {
            getSLF4JLogger().warn("Secret file is not readable {}", pathToSecretFile.toAbsolutePath());
            return null;
        }
        if (!Files.isRegularFile(pathToSecretFile)) {
            getSLF4JLogger().warn("Secret file at {} is not a regular file (a directory?)", pathToSecretFile.toAbsolutePath());
            return null;
        }
        try {
            return SHA512.digest(Files.readAllBytes(pathToSecretFile));
        } catch (IOException ioException) {
            getSLF4JLogger().warn("Secret file reading failed", ioException);
            return null;
        }
    }
}
