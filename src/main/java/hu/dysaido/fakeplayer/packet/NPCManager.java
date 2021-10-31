package hu.dysaido.fakeplayer.packet;

import hu.dysaido.fakeplayer.FakePlayer;
import hu.dysaido.fakeplayer.api.npc.NPC;
import hu.dysaido.fakeplayer.npc.NPCImpl;
import hu.dysaido.fakeplayer.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class NPCManager {

    private final Map<UUID, NPC> npcMap = new HashMap<>();
    private final FakePlayer plugin = FakePlayer.getInstance();

    public void initializationNPCs() {
        final ConfigurationSection section = plugin.getFakePlayersSection();
        if (section.getKeys(false).size() == 0) {
            if (!npcMap.values().isEmpty()) {
                npcMap.values().forEach(NPC::onDestroy);
            }
            return;
        }
        if (!npcMap.isEmpty()) {
            try {
                for (UUID key : npcMap.keySet()) {
                    if (!section.getKeys(false).contains(key.toString())) delete(key).onDestroy();
                }
            } catch (Exception ignore) {}
        }
        for (String key : section.getKeys(false)) {
            final ConfigurationSection npcSection = section.getConfigurationSection(key);
            if (npcSection != null) {
                final Location location = LocationUtil.getLocation(npcSection);
                if (!Bukkit.getWorlds().contains(location.getWorld())) continue;
                final UUID uuid = UUID.fromString(key);
                final String name = npcSection.getString("NPCname");
                if (npcMap.containsKey(uuid)) {
                    if (npcMap.get(uuid).getName().equals(name)) continue;
                    else npcMap.remove(uuid).onDestroy();
                }
                try {
                    final NPC npc = new NPCImpl(UUID.fromString(key), name, null);
                    npc.onCreate(location, npcSection);
                    npcMap.put(npc.getUniqueId(), npc);
                    Bukkit.getOnlinePlayers().forEach(npc::onSpawn);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Npc create error");
                    Bukkit.getLogger().warning(e.toString());
                }

            }
        }
    }

    public void createNPC(String name, String skin, Location location, ConfigurationSection npcSection) {
        Objects.requireNonNull(name);
        final UUID randomUUID = UUID.randomUUID();
        if (plugin.getFakePlayersSection().contains(randomUUID.toString())) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                final NPCImpl npc = new NPCImpl(randomUUID, name, skin);
                npc.onCreate(location, npcSection);
                npcMap.put(npc.getUniqueId(), npc);
                final ConfigurationSection section = plugin.getFakePlayersSection().createSection(randomUUID.toString());
                section.set("NPCname", npc.getName());
                section.set("NPCConsoleCommand", false);
                section.set("NPCExecuteConsole", "heal {player}");
                section.set("NPCSenderCommand", false);
                section.set("NPCExecutePlayer", "balance");
                LocationUtil.setLocation(location, section);
                if (Objects.isNull(npc.getSkin())) {
                    section.set("texture", null);
                    section.set("signature", null);
                } else {
                    section.set("texture", npc.getTexture());
                    section.set("signature", npc.getSignature());
                }
                plugin.saveConfig();
                Bukkit.getOnlinePlayers().forEach(npc::onSpawn);
            } catch (Exception e) {
                Bukkit.getLogger().warning("Npc create error");
                Bukkit.getLogger().warning(e.toString());
            }
        });
    }


    public void set(NPC npc) {
        Objects.requireNonNull(npc);
        if (npcMap.containsKey(npc.getUniqueId())) return;
        npcMap.put(npc.getUniqueId(), npc);
    }

    public NPC remove(UUID uuid) {
        Objects.requireNonNull(uuid);
        return npcMap.remove(uuid);
    }

    public NPC delete(UUID uuid) {
        Objects.requireNonNull(uuid);
        final ConfigurationSection section = plugin.getFakePlayersSection();
        section.set(uuid.toString(), null);
        plugin.saveConfig();
        return remove(uuid);
    }

    public NPC get(UUID uuid) {
        Objects.requireNonNull(uuid);
        return npcMap.get(uuid);
    }

    public boolean containsKey(UUID uuid) {
        return npcMap.containsKey(uuid);
    }

    public Collection<NPC> getNPCs() {
        return npcMap.values();
    }

}
