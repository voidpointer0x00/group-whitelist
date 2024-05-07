package voidpointer.spigot.groupwhitelist.config.loader;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.function.Supplier;

@Data
@Slf4j
public class ConfigLoader<ConfigT> {
    private Path path;
    private Class<ConfigT> configClass;
    private Supplier<ConfigT> defaultSupplier;

    /**
     * @throws IllegalArgumentException if the default constructor of the configuration class
     *      does not exist or is not accessible from this class.
     */
    public ConfigLoader(final Path pathToDataFolder, final Class<ConfigT> configClass)
            throws IllegalArgumentException {
        String kebabCasedClassName = configClass.getSimpleName().replaceAll("([a-z])([A-Z])", "$1-$2")
                .toLowerCase();
        this.path = pathToDataFolder.resolve(kebabCasedClassName + ".conf");
        this.configClass = configClass;

        final Constructor<ConfigT> constructor;
        try {
            constructor = configClass.getConstructor();
        } catch (final NoSuchMethodException defaultConstructorUndefined) {
            throw new IllegalArgumentException(configClass.getName() + " does not declare a default constructor");
        }
        if (!constructor.canAccess(null))
            throw new IllegalArgumentException(configClass.getName() + "'s default constructor must be accessible");

        this.defaultSupplier = () -> {
            try {
                return constructor.newInstance();
            } catch (final InstantiationException ex) {
                log.error("Could not instantiate an abstract config " + configClass.getName(), ex);
            } catch (final IllegalAccessException ex) {
                log.error("Could not access default constructor of " + configClass.getName(), ex);
            } catch (final InvocationTargetException ex) {
                log.error("Config class constructor threw an exception " + configClass.getName(), ex);
            }
            return null;
        };
    }

    public ConfigT loadAndSaveDefaultIfNotExists() {
        return path.toFile().exists()
                ? HoconConfigLoader.load(path, configClass, defaultSupplier)
                : HoconConfigLoader.loadAndSave(path, configClass, defaultSupplier);
    }

    public void save(final ConfigT configT) {
        HoconConfigLoader.save(path, configT);
    }
}
