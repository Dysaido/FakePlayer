package hu.dysaido.fakeplayer.api.npc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface NPC {

    int getId();

    UUID getUniqueId();

    String getName();

    Location getLocation();

    String getTexture();

    String getSignature();

    Object getEntityPlayer();

    Object getBukkitEntity();

    void onCreate(Location location, ConfigurationSection section);

    void onSpawn(Player player);

    void lookAtPlayer(Player player);

    void onDespawn(Player player);

    void onDestroy();

    double getLocX();

    double getLocY();

    double getLocZ();

    World getWorld();

}
