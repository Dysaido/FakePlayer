package hu.dysaido.fakeplayer.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import hu.dysaido.fakeplayer.FakePlayer;
import hu.dysaido.fakeplayer.api.npc.NPC;
import hu.dysaido.fakeplayer.util.Format;
import hu.dysaido.fakeplayer.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.util.Vector;

import java.io.InputStreamReader;
import java.lang.reflect.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class NPCImpl implements NPC {

    protected final Object ADD_PLAYER = ReflectUtil.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getField("ADD_PLAYER").get(null);
    protected final Object REMOVE_PLAYER = ReflectUtil.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getField("REMOVE_PLAYER").get(null);
    protected final Constructor<?> packetPlayOutPlayerInfoConstructor = ReflectUtil.getNMSClass("PacketPlayOutPlayerInfo").getConstructor(ReflectUtil.getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction"),
            ReflectUtil.getNMSArrayClass("EntityPlayer"));
    protected final Constructor<?> packetPlayOutNamedEntitySpawnConstructor = ReflectUtil.getNMSClass("PacketPlayOutNamedEntitySpawn").getConstructor(ReflectUtil.getNMSClass("EntityHuman"));
    protected final Constructor<?> packetPlayOutEntityHeadRotationConstructor = ReflectUtil.getNMSClass("PacketPlayOutEntityHeadRotation").getConstructor(ReflectUtil.getNMSClass("Entity"), byte.class);
    protected final Constructor<?> entityPlayerConstructor = ReflectUtil.getNMSClass("EntityPlayer").getDeclaredConstructors()[0];
    protected final Constructor<?> playerInteractManagerConstructor = ReflectUtil.getNMSClass("PlayerInteractManager").getDeclaredConstructors()[0];
    protected final Constructor<?> packetPlayOutEntityDestroyConstructor = ReflectUtil.getNMSClass("PacketPlayOutEntityDestroy").getDeclaredConstructor(int[].class);
    protected final Constructor<?> packetPlayOutEntityLookConstructor = ReflectUtil.getNMSClass("PacketPlayOutEntity$PacketPlayOutEntityLook").getConstructor(int.class, byte.class, byte.class, boolean.class);
    protected final Class<?> MathHelper = ReflectUtil.getNMSClass("MathHelper");
    protected final Set<Player> players = new HashSet<>();
    private final String name;
    private final String skin;
    private final UUID randomUUID;
    protected Object entity;
    protected Object entityPlayer;
    protected Object server;
    protected Object world;
    protected GameProfile gameProfile;
    protected Location location;
    protected int id;

    public NPCImpl(UUID randomUUID, String name, String skin) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        this.name = name;
        this.skin = skin;
        this.randomUUID = randomUUID;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public UUID getUniqueId() {
        return randomUUID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        this.location = location;
        Method setLocation = entityPlayer.getClass().getMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
        setLocation.invoke(entityPlayer, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public String getTexture() {
        return getSkin()[0];
    }

    @Override
    public String getSignature() {
        return getSkin()[1];
    }

    @Override
    public Object getEntityPlayer() {
        return entityPlayer;
    }

    @Override
    public void onCreate(Location location, ConfigurationSection section) {
        try {
            server = ReflectUtil.getOBCClass("CraftServer").getDeclaredMethod("getServer").invoke(Bukkit.getServer());
            world = ReflectUtil.getOBCClass("CraftWorld").getDeclaredMethod("getHandle").invoke(location.getWorld());
            gameProfile = new GameProfile(randomUUID, Format.color(name));
            if (section != null) {
                String texture = section.getString("texture");
                String signature = section.getString("signature");
                if (Objects.nonNull(texture) && Objects.nonNull(signature))
                    gameProfile.getProperties().put("textures", new Property("textures", texture, signature));
            } else {
                String[] name = getSkin();
                if (Objects.nonNull(name))
                    gameProfile.getProperties().put("textures", new Property("textures", getTexture(), getSignature()));
            }
            entityPlayer = entityPlayerConstructor.newInstance(server, world, gameProfile, playerInteractManagerConstructor.newInstance(world));
            id = (int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer);
            entity = entityPlayer.getClass().getMethod("getBukkitEntity").invoke(entityPlayer);
            setLocation(location);
            watch();
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public void onSpawn(Player player) {
        try {
            Object array = Array.newInstance(ReflectUtil.getNMSClass("EntityPlayer"), 1);
            Array.set(array, 0, entityPlayer);
            Object addInfo = packetPlayOutPlayerInfoConstructor.newInstance(ADD_PLAYER, array);
            Object spawn = packetPlayOutNamedEntitySpawnConstructor.newInstance(entityPlayer);
            float yaw = (float) entityPlayer.getClass().getField("yaw").get(entityPlayer);
            Object headRotation = packetPlayOutEntityHeadRotationConstructor.newInstance(entityPlayer, (byte) (yaw * 256 / 360));

            sendPacket(player, addInfo);
            sendPacket(player, spawn);
            sendPacket(player, headRotation);
            Bukkit.getScheduler().scheduleSyncDelayedTask(FakePlayer.getInstance(), () -> {
                try {
                    Object remInfo = packetPlayOutPlayerInfoConstructor.newInstance(REMOVE_PLAYER, array);
                    sendPacket(player, remInfo);
                } catch (Exception e) {
                    Bukkit.getLogger().warning(e.getMessage());
                }
            }, 10L);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public void lookAtPlayer(Player player) {
        double distance = calculateDistance(player);
        if (distance < 10 && distance > 8) {
            if (players.contains(player)) return;
            onDespawn(player);
            onSpawn(player);
            players.add(player);
        } else {
            players.remove(player);
        }
        if (distance < 5) {
            final Vector difference = player.getLocation().subtract(location).toVector().normalize();
            final float degrees = (float) Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2);
            try {
                final int angle = (int) MathHelper.getMethod("d", float.class).invoke(null, (degrees * 256.0F) / 360.0F);
                final long pitch = (long) MathHelper.getMethod("d", double.class).invoke(null, (Math.toDegrees(Math.atan(difference.getY() * -1)) * 256.0F) / 360.0F);
                final Object headRotation = packetPlayOutEntityHeadRotationConstructor.newInstance(entityPlayer, (byte) angle);
                final Object entityLook = packetPlayOutEntityLookConstructor.newInstance(getId(), (byte) angle, (byte) pitch, true);

                sendPacket(player, headRotation);
                sendPacket(player, entityLook);
            } catch (Exception e) {
                Bukkit.getLogger().warning(e.getMessage());
            }
        }
    }

    @Override
    public Object getBukkitEntity() {
        return entity;
    }

    @Override
    public void onDespawn(Player player) {
        try {
            Object array = Array.newInstance(ReflectUtil.getNMSClass("EntityPlayer"), 1);
            Array.set(array, 0, entityPlayer);
            Object idArray = Array.newInstance(int.class, 1);
            Array.set(idArray, 0, id);
            Object destroy = packetPlayOutEntityDestroyConstructor.newInstance(idArray);
            Object addInfo = packetPlayOutPlayerInfoConstructor.newInstance(ADD_PLAYER, array);
            Object remInfo = packetPlayOutPlayerInfoConstructor.newInstance(REMOVE_PLAYER, array);
            sendPacket(player, addInfo);
            sendPacket(player, destroy);
            sendPacket(player, remInfo);

//            PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
//
//            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(id);
//            PacketPlayOutPlayerInfo addInfo = new PacketPlayOutPlayerInfo(
//                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER,
//                    (EntityPlayer) entityPlayer);
//            PacketPlayOutPlayerInfo remInfo = new PacketPlayOutPlayerInfo(
//                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
//                    (EntityPlayer) entityPlayer);
//
//            connection.sendPacket(addInfo);
//            connection.sendPacket(destroy);
//            connection.sendPacket(remInfo);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Bukkit.getOnlinePlayers().forEach(this::onDespawn);
    }

    @Override
    public double getLocX() {
        double locX = 0.0;
        try {
            Field field = entityPlayer.getClass().getField("locX");
            locX = (double) field.get(entityPlayer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return locX;
    }

    @Override
    public double getLocY() {
        double locY = 0.0;
        try {
            Field field = entityPlayer.getClass().getField("locY");
            locY = (double) field.get(entityPlayer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return locY;
    }

    @Override
    public double getLocZ() {
        double locZ = 0.0;
        try {
            Field field = entityPlayer.getClass().getField("locZ");
            locZ = (double) field.get(entityPlayer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return locZ;
    }

    @Override
    public World getWorld() {
        return location.getWorld();
    }

    public String[] getSkin() {
        String[] string = null;
        try {
            if (Objects.isNull(skin)) return null;
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + skin);
            InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
            String uuid = new JsonParser().parse(inputStreamReader).getAsJsonObject().get("id").getAsString();

            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader inputStreamReader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(inputStreamReader2).getAsJsonObject()
                    .get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = property.get("value").getAsString();
            String signature = property.get("signature").getAsString();
            string = new String[]{texture, signature};
        } catch (Exception e) {
            Bukkit.getOnlinePlayers().stream().filter(ServerOperator::isOp).forEach(player -> player.sendMessage("NPC's skin not found."));
        }
        return string;
    }

    public void watch() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object dataWatcher = entityPlayer.getClass().getMethod("getDataWatcher").invoke(entityPlayer);
        Method watch = dataWatcher.getClass().getMethod("watch", int.class, Object.class);
        watch.invoke(dataWatcher, 10, (byte) 127);
    }

    public void setHealth(float f) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method setHealth = entityPlayer.getClass().getMethod("setHealth", float.class);
        setHealth.invoke(entityPlayer, f);
    }

    public double calculateDistance(Player player) {
        double diffX = getLocX() - player.getLocation().getX(), diffZ = getLocZ() - player.getLocation().getZ();
        double x = diffX < 0 ? (diffX * -1) : diffX, z = diffZ < 0 ? (diffZ * -1) : diffZ;
        return Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    public void sendPacket(Player player, Object packet) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            Method sendPacket = playerConnection.getClass().getMethod("sendPacket", ReflectUtil.getNMSClass("Packet"));
            sendPacket.invoke(playerConnection, packet);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "NPC{Name='" + name + '\'' + ", Skin='" + skin + '\'' + ", UUID=" + randomUUID + '}';
    }
}
