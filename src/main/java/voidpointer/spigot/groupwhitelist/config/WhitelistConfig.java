package voidpointer.spigot.groupwhitelist.config;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.model.group.Group;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import voidpointer.spigot.groupwhitelist.GroupWhitelistPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.security.MessageDigest.isEqual;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static voidpointer.spigot.groupwhitelist.GroupWhitelistPlugin.SHA512;

@Getter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
@ConfigSerializable
@Slf4j
public final class WhitelistConfig implements SecretProvider {
    @Comment("A list of groups that are allowed on the server")
    private Set<String> whitelistGroups = Set.of("whitelisted");

    @Comment("Whether to filter joining players based on whitelist group")
    private boolean isEnabled = false;

    @Comment("Whether to automatically kick players if their group was removed from the whitelist")
    private boolean shouldKickRemovedGroups = true;

    @Comment("Whether if the plugin should hide server status in players server list")
    private boolean hideStatus = false;

    @Comment("""
            Settings for secret key used to show status and authenticate to players that provide the same key.
            Requires ProtocolLib and a custom mod installed on client.""")
    private SecretSettings secretSettings = new SecretSettings();

    @Getter
    @Accessors(fluent = true)
    @ConfigSerializable
    public static final class SecretSettings {
        public enum Type {
            STRING, FILE
        }

        @Comment("Whether if the plugin should show server status if the player sends a matching secret")
        private boolean showStatusWithSecret = false;
        @Comment("""
        STRING  - reads a string right from this configuration.
        FILE    - reads bytes from a file specified by this configuration.""")
        private Type type = Type.STRING;
        @Comment("Secret to be used with STRING.")
        private String secret = RandomStringUtils.random(32, 0, 0, true, true, null, new SecureRandom());
        @Comment("File with secret string to be used if FILE type is selected.")
        private String pathToSecretFile = GroupWhitelistPlugin.getPlugin(GroupWhitelistPlugin.class)
                .getDataFolder().toPath().resolve("gwl-secret").toString();
        @Comment("Whether if plugin should disallow login from players without the secret key.")
        private boolean authenticateWithSecret = false;

        @Getter(AccessLevel.PRIVATE)
        @Setter(AccessLevel.PRIVATE)
        private transient volatile byte[] cachedSecretFileChecksum = null;

        public SecretSettings() {
            var pathToSecret = Path.of(pathToSecretFile);
            if (!Files.exists(pathToSecret)) {
                var parent = pathToSecret.toAbsolutePath().getParent();
                if (parent != null && !parent.toFile().exists() && !parent.toFile().mkdirs()) {
                    log.error("Could not create {}: cannot #mkdirs() on parent", pathToSecret);
                    return;
                }
                try {
                    Files.writeString(pathToSecret, RandomStringUtils.random(32, 0, 0, true, true, null, new SecureRandom()));
                } catch (IOException ioException) {
                    log.error("Could not generate default secret", ioException);
                }
            }
        }

        public interface SecretChecksumLoader {
            default void updateCachedChecksum(@NotNull SecretSettings settings, byte @Nullable[] checksum) {
                settings.cachedSecretFileChecksum(checksum);
            }
        }
    }

    @Override
    public boolean testSecretHash(byte[] secretHash) {
        return switch (secretSettings.type) {
            case STRING -> SHA512 != null && isEqual(secretHash, SHA512.digest(secretSettings.secret.getBytes(UTF_8)));
            case FILE -> isEqual(secretHash, secretSettings.cachedSecretFileChecksum);
        };
    }

    /**
     * Turns on this whitelist and returns
     * {@code true} if the whitelist was not on.
     */
    public boolean turnOn() {
        return !isEnabled && (isEnabled = true);
    }

    /**
     * Turns off this whitelist and returns
     * {@code true} if the whitelist was not off.
     */
    public boolean turnOff() {
        //noinspection ConstantValue
        return isEnabled && !(isEnabled = false);
    }

    public boolean isGroupWhitelisted(final Group group) {
        return whitelistGroups != null && whitelistGroups.contains(group.getName());
    }

    public Set<String> whitelistGroups() {
        return whitelistGroups == null ? emptySet() : unmodifiableSet(whitelistGroups);
    }

    /**
     * Adds a given group to this whitelist and returns
     * {@code true} if whitelist <b>did not</b> contain
     * the given group before.
     */
    public boolean addGroup(final String groupName) {
        try {
            return this.whitelistGroups.add(groupName);
        } catch (final UnsupportedOperationException unsupportedOperationException) {
            Set<String> whitelistGroups = ConcurrentHashMap.newKeySet();
            whitelistGroups.addAll(this.whitelistGroups);
            boolean isNewlyAdded = whitelistGroups.add(groupName);
            this.whitelistGroups = whitelistGroups;
            return isNewlyAdded;
        }
    }

    /**
     * Removes a given group from this whitelist and returns
     * {@code true} if the given group was whitelisted before.
     */
    public boolean removeGroup(final String groupName) {
        try {
            return this.whitelistGroups.remove(groupName);
        } catch (final UnsupportedOperationException unsupportedOperationException) {
            Set<String> whitelistGroups = ConcurrentHashMap.newKeySet();
            whitelistGroups.addAll(this.whitelistGroups);
            boolean wasRemoved = whitelistGroups.remove(groupName);
            this.whitelistGroups = whitelistGroups;
            return wasRemoved;
        }
    }
}
