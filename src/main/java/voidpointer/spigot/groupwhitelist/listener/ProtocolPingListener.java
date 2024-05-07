package voidpointer.spigot.groupwhitelist.listener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import voidpointer.spigot.groupwhitelist.config.WhitelistConfig;

import java.util.HexFormat;

import static com.comphenix.protocol.PacketType.Handshake.Client.SET_PROTOCOL;

@Slf4j
@Setter
public final class ProtocolPingListener implements PingListener {
    private static final int LOGIN_ORDINAL = 3;
    private @NotNull WhitelistConfig whitelistConfig;

    public ProtocolPingListener(final @NotNull WhitelistConfig whitelistConfig) {
        this.whitelistConfig = whitelistConfig;
    }

    @Override
    public @NotNull PingListener register(final @NotNull Plugin plugin) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, SET_PROTOCOL) {
            @Override public void onPacketReceiving(PacketEvent event) {
                ProtocolPingListener.this.testHash(event);
            }
        });
        return this;
    }

    @SuppressWarnings("deprecation")
    public void testHash(final @NotNull PacketEvent packetEvent) {
        if (!whitelistConfig.isEnabled())
            return;
        PacketContainer packet = packetEvent.getPacket();
        if (!whitelistConfig.secretSettings().authenticateWithSecret()) {
            var nextState = packet.getModifier().readSafely(3);
            if (nextState instanceof Enum<?> nextStateEnum && nextStateEnum.ordinal() == LOGIN_ORDINAL) {
                /* allow login if not authenticating */
                return;
            }
        }
        if (!(whitelistConfig.hideStatus() && whitelistConfig.secretSettings().showStatusWithSecret())) {
            packetEvent.setCancelled(true);
            return;
        }
        String hostnameAsChecksum = packet.getStrings().readSafely(0);
        byte[] secretChecksum = HexFormat.of().parseHex(hostnameAsChecksum);
        if (secretChecksum == null || !whitelistConfig.testSecretHash(secretChecksum)) {
            log.info("{} sent invalid secret hash", packetEvent.getPlayer().getAddress());
            packetEvent.setCancelled(true);
            /* ProtocolLib's temporary player does not support Paper's #kick()
             * methods and it is challenging to implement because of potential
             * Component class loading issues on platforms other than Paper */
            packetEvent.getPlayer().kickPlayer(null);
        } else {
            log.info("{} secret hash accepted", packetEvent.getPlayer().getAddress());
        }
    }
}
