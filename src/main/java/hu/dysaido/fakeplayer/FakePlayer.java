package hu.dysaido.fakeplayer;

import hu.dysaido.fakeplayer.command.Commandfakeplayer;
import hu.dysaido.fakeplayer.listener.EntityInteractOfWorld;
import hu.dysaido.fakeplayer.packet.NPCHandler;
import hu.dysaido.fakeplayer.packet.NPCManager;
import hu.dysaido.fakeplayer.packet.PacketReader;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class FakePlayer extends JavaPlugin {

    private static FakePlayer plugin;
    private NPCManager npcManager;
    private NPCHandler npcHandler;

    public static synchronized FakePlayer getInstance() {
        return plugin;
    }

    private static void setInstance(FakePlayer fakePlayer) {
        FakePlayer.plugin = fakePlayer;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        setInstance(this);
        getConfig().options().copyDefaults(true);
        saveConfig();
        npcManager = new NPCManager();
        npcHandler = new NPCHandler();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
           while (Bukkit.getOnlinePlayers().size() == 0) {}
           initialization();
        });
        getServer().getPluginCommand("fakeplayer").setExecutor(new Commandfakeplayer());
    }

    private void initialization() {
        npcManager.initializationNPCs();
        npcHandler.schedulerNPCLook();
        registerListeners();
    }

    private void registerListeners() {
        new EntityInteractOfWorld();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ConfigurationSection getFakePlayersSection() {
        return getConfig().getConfigurationSection("fake-players");
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public NPCHandler getNpcHandler() {
        return npcHandler;
    }
}
