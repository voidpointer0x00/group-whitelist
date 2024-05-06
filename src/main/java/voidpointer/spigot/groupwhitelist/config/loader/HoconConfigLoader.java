package voidpointer.spigot.groupwhitelist.config.loader;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;
import java.util.function.Supplier;

@Slf4j
public class HoconConfigLoader {
    static <T> T loadAndSave(Path src, Class<T> type, Supplier<T> defaultSupplier) {
        var loader = HoconConfigurationLoader.builder().path(src).build();
        return save(loader, src, load(loader, src, type, defaultSupplier));
    }

    static <ConfigT> ConfigT load(Path pathToLoad, Class<ConfigT> type, Supplier<ConfigT> defaultSupplier) {
        return load(HoconConfigurationLoader.builder().path(pathToLoad).build(), pathToLoad, type, defaultSupplier);
    }

    static <ConfigT> void save(Path pathToSave, ConfigT configT) {
        save(HoconConfigurationLoader.builder().path(pathToSave).build(), pathToSave, configT);
    }

    static <ConfigT> ConfigT load(
            HoconConfigurationLoader hoconLoader,
            Path pathToLoad,
            Class<ConfigT> type,
            Supplier<ConfigT> defaultSupplier
    ) {
        CommentedConfigurationNode rootNode;
        try {
            rootNode = hoconLoader.load();
            log.trace("{} loaded", pathToLoad);
        } catch (final ConfigurateException ex) {
            log.warn("Could not parse {}: {}", type.getName(), ex.getMessage());
            return defaultSupplier.get();
        }
        ConfigT config;
        try {
            config = rootNode.get(type);
        } catch (final SerializationException ex) {
            log.warn("Could not deserialize {}: {}", type.getName(), ex.getMessage());
            return defaultSupplier.get();
        }
        return config == null ? defaultSupplier.get() : config;
    }

    @Contract("_, !null, null -> param3")
    static <ConfigT> ConfigT save(@NotNull HoconConfigurationLoader hoconLoader, Path destination, ConfigT config) {
        CommentedConfigurationNode rootNode = hoconLoader.createNode();
        try {
            rootNode.set(config);
        } catch (final SerializationException ex) {
            log.warn("Could not serialize {}: {}", config.getClass().getName(), ex.getMessage());
            return config;
        }
        try {
            hoconLoader.save(rootNode);
            log.trace("{} saved", destination);
        } catch (final ConfigurateException ex) {
            log.warn("Could not save {}: {}", config.getClass().getName(), ex.getMessage());
        }
        return config;
    }
}
