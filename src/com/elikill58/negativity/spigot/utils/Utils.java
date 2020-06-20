package com.elikill58.negativity.spigot.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.elikill58.negativity.spigot.ClickableText;
import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.universal.Version;
import com.elikill58.negativity.universal.permissions.Perm;
import com.elikill58.negativity.universal.utils.UniversalUtils;
import com.google.common.collect.Sets;

@SuppressWarnings("deprecation")
public class Utils {

	public static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
			.split(",")[3];

	public static String coloredMessage(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static List<String> coloredMessage(String... messages) {
		List<String> ret = new ArrayList<>();
		for (String message : messages) {
			ret.add(coloredMessage(message));
		}
		return ret;
	}

	public static List<Player> getOnlinePlayers() {
		List<Player> list = new ArrayList<>();
		try {
			Class<?> mcServer = Class.forName("net.minecraft.server." + VERSION + ".MinecraftServer");
			Object server = mcServer.getMethod("getServer").invoke(mcServer);
			Object craftServer = server.getClass().getField("server").get(server);
			Object getted = craftServer.getClass().getMethod("getOnlinePlayers").invoke(craftServer);
			if (getted instanceof Player[])
				for (Player obj : (Player[]) getted)
					list.add(obj);
			else if (getted instanceof List)
				for (Object obj : (List<?>) getted)
					list.add((Player) obj);
			else
				System.out.println("Unknow getOnlinePlayers");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Nullable
	public static Player getFirstOnlinePlayer() {
		List<Player> onlinePlayers = getOnlinePlayers();
		return onlinePlayers.isEmpty() ? null : onlinePlayers.iterator().next();
	}

	public static ItemStack createSkull(String name, int amount, String owner, String... lore) {
		ItemStack skull = new ItemStack(ItemUtils.PLAYER_HEAD, 1, (byte) 3);
		SkullMeta skullmeta = (SkullMeta) skull.getItemMeta();
		skullmeta.setDisplayName(ChatColor.RESET + name);
		skullmeta.setOwner(owner);
		List<String> lorel = new ArrayList<>();
		for (String s : lore)
			lorel.add(s);
		skullmeta.setLore(lorel);
		skull.setItemMeta(skullmeta);
		return skull;
	}
	
	/**
	 * Get the current player ping
	 * 
	 * @param p the player
	 * @return the player ping
	 */
	public static int getPing(Player p) {
		try {
			Object entityPlayer = PacketUtils.getEntityPlayer(p);
			return entityPlayer.getClass().getField("ping").getInt(entityPlayer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static Effect getEffect(String effect) {
		Effect m = null;
		try {
			m = (Effect) Effect.class.getField(effect).get(Effect.class);
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e1) {
			m = null;
		}
		return m;
	}

	public static void sendUpdateMessageIfNeed(Player p) {
		if(!Perm.hasPerm(SpigotNegativityPlayer.getNegativityPlayer(p), Perm.SHOW_ALERT))
			return;
		if(UniversalUtils.isLatestVersion(SpigotNegativity.getInstance().getDescription().getVersion()))
			return;
		String newerVersion = UniversalUtils.getLatestVersion().orElse("unknow");
		new ClickableText().addOpenURLHoverEvent(
				ChatColor.YELLOW + "New version of Negativity available (" + newerVersion +  "). " + ChatColor.BOLD + "Download it here.",
				"Click here", "https://www.spigotmc.org/resources/48399/")
				.sendToPlayer(p);
	}

	public static double getLastTPS() {
		double[] tps = getTPS();
		return tps[tps.length - 1];
	}

	public static double[] getTPS() {
		try {
			Class<?> mcServer = PacketUtils.getNmsClass("MinecraftServer");
			Object server = mcServer.getMethod("getServer").invoke(mcServer);
			return (double[]) server.getClass().getField("recentTps").get(server);
		} catch (Exception e) {
			SpigotNegativity.getInstance().getLogger().warning("Cannot get TPS (Work on Spigot but NOT CraftBukkit).");
			return new double[] {20, 20, 20};
		}
	}
	
	public static void teleportPlayerOnGround(Player p) {
		int i = 20;
		Location loc = p.getLocation();
		while (loc.getBlock().getType().equals(Material.AIR) && i > 0) {
			loc.subtract(0, 1, 0);
			i--;
		}
		p.teleport(loc.add(0, 1, 0));
	}
	
	public static Block getTargetBlock(Player p, int distance) {
		Material[] transparentItem = new Material[] {};
		try {
			if(Version.getVersion().isNewerOrEquals(Version.V1_14)) {
				return (Block) p.getClass().getMethod("getTargetBlockExact", int.class).invoke(p, distance);
			} else {
				try {
					return (Block) p.getClass().getMethod("getTargetBlock", Set.class, int.class).invoke(p, (Set<Material>) Sets.newHashSet(transparentItem), distance);
				} catch (NoSuchMethodException e) {}
				try {
					HashSet<Byte> hashSet = new HashSet<>();
					for(Material m : transparentItem)
						hashSet.add((byte) m.getId());
					return (Block) p.getClass().getMethod("getTargetBlock", HashSet.class, int.class).invoke(p, hashSet, distance);
				} catch (NoSuchMethodException e) {}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}
	
	public static boolean isInBoat(Player p) {
		return p.isInsideVehicle() && p.getVehicle().getType().equals(EntityType.BOAT);
	}

	public static boolean hasThorns(Player p) {
		ItemStack[] armor = p.getInventory().getArmorContents();
		if(armor == null)
			return false;
		for(ItemStack item : armor)
			if(item != null && item.containsEnchantment(Enchantment.THORNS))
				return true;
		return false;
	}
	
	public static ItemStack getItemInHand(Player p) {
		return p.getItemInHand();
	}
}
