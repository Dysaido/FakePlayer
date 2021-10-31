package hu.dysaido.fakeplayer.packet;

import hu.dysaido.fakeplayer.FakePlayer;
import hu.dysaido.fakeplayer.api.event.PlayerLeftClickAtNPCEvent;
import hu.dysaido.fakeplayer.api.event.PlayerRightClickAtNPCEvent;
import hu.dysaido.fakeplayer.api.npc.NPC;
import hu.dysaido.fakeplayer.util.ReflectUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketReader {

    public static final Map<UUID, Channel> CHANNEL_MAP = new HashMap<>();
    private static final FakePlayer plugin = FakePlayer.getInstance();

    public static void injectAll() {
        Bukkit.getOnlinePlayers().forEach(PacketReader::inject);
    }

    public static void uninjectAll() {
        Bukkit.getOnlinePlayers().forEach(PacketReader::uninject);
    }

    public static void inject(Player player) {
        if (CHANNEL_MAP.get(player.getUniqueId()) != null) return;
        try {
            final Object handle = player.getClass().getMethod("getHandle").invoke(player);
            final Object playerConnection = ReflectUtil.getField(handle, "playerConnection");
            final Object networkManager = ReflectUtil.getField(playerConnection, "networkManager");
            final Channel channel = (Channel) ReflectUtil.getField(networkManager, "channel");
            channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Object>() {
                @Override
                protected void decode(ChannelHandlerContext channelHandlerContext, Object packet, List<Object> objects) {
                    objects.add(packet);
                    readPacket(player, packet);
                }
            });
            CHANNEL_MAP.put(player.getUniqueId(), channel);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void uninject(Player player) {
        final Channel channel = CHANNEL_MAP.get(player.getUniqueId());
        if (Objects.nonNull(channel)) {
            if (channel.pipeline().get("PacketInjector") != null) {
                channel.pipeline().remove("PacketInjector");
            }
        }
        CHANNEL_MAP.remove(player.getUniqueId());
    }

    private static void readPacket(Player player, Object packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {
            int id = (Integer) ReflectUtil.getDeclaredField(packet, "a");
            for (NPC npc : plugin.getNpcManager().getNPCs()) {
                if (npc.getId() == id) {
                    if (ReflectUtil.getDeclaredField(packet, "action").toString().equalsIgnoreCase("ATTACK")) {
                        Bukkit.getServer().getPluginManager().callEvent(new PlayerLeftClickAtNPCEvent(player, npc));
                    }
                    if (ReflectUtil.getDeclaredField(packet, "action").toString().equalsIgnoreCase("INTERACT")) {
                        Bukkit.getServer().getPluginManager().callEvent(new PlayerRightClickAtNPCEvent(player, npc));
                    }
                }
            }

        }
    }

}