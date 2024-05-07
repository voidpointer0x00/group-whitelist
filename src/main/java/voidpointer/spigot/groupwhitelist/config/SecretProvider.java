package voidpointer.spigot.groupwhitelist.config;

public interface SecretProvider {
    boolean testSecretHash(byte[] secretHash);
}
