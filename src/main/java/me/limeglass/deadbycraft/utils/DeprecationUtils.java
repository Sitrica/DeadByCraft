package me.limeglass.deadbycraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.TropicalFish.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

@SuppressWarnings("deprecation")
public class DeprecationUtils {

	public static void setupOldSkull(BlockState state) {
		MaterialData data = state.getData();
		data.setData((byte) 1);
		state.setData(data);
		state.update(true);
	}

	public static void sendBlockChange(Player player, Location location, Material material) {
		try {
			player.sendBlockChange(location, material, (byte) 0);
		} catch (Exception e) {
			player.sendBlockChange(location, material.createBlockData());
		}
	}

	public static ItemStack getItemInMainHand(Player player) {
		try {
			return player.getItemInHand();
		} catch (Exception e) {
			return player.getInventory().getItemInMainHand();
		}
	}

	public static short getDurability(ItemStack item) {
		try {
			return item.getDurability();
		} catch (Exception e) {
			return 0;
		}
	}

	public static double getMaxHealth(LivingEntity entity) {
		try {
			return entity.getMaxHealth();
		} catch (Exception e) {
			return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		}
	}

	public static void setItemInMainHand(LivingEntity entity, ItemStack item) {
		try {
			entity.getEquipment().setItemInHand(item);
		} catch (Exception e) {
			entity.getEquipment().setItemInMainHand(item);
		}
	}

	public static void setMaxHealth(LivingEntity entity, double health) {
		try {
			entity.setMaxHealth(health);
		} catch (Exception e) {
			entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
		}
	}

	public static void setItemInHandDropChance(LivingEntity entity, float chance) {
		try {
			entity.getEquipment().setItemInHandDropChance(chance);
		} catch (Exception e) {
			entity.getEquipment().setItemInMainHandDropChance(chance);
		}
	}

	public static OfflinePlayer getSkullOwner(String input) {
		if (Utils.isUUID(input))
			return Bukkit.getOfflinePlayer(Utils.getUniqueId(input));
		else
			return Bukkit.getOfflinePlayer(input);
	}

	public static Enchantment getEnchantment(String name) {
		try {
			return Enchantment.getByName(name);
		} catch (Exception e) {
			try {
				return Enchantment.getByKey(NamespacedKey.minecraft(name));
			} catch (Exception e2) {
				return null;
			}
		}
	}

	public static void setKnockbackResistance(LivingEntity entity, double amount) {
		try {
			// 1.8 doesn't have Attributes.
			entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(amount);
		} catch (Exception e) {
			entity.setVelocity(entity.getLocation().getDirection().multiply(-amount));
		}
	}

	public static void setKnockbackAttribute(Monster champion, double amount) {
		champion.setVelocity(champion.getLocation().getDirection().multiply(-amount));
	}

	public static ItemMeta setupItemMeta(ItemMeta itemMeta, String meta) {
		if (meta.equals(""))
			return itemMeta;
		if (itemMeta instanceof PotionMeta) {
			PotionMeta potionMeta = (PotionMeta) itemMeta;
			PotionType type;
			try {
				type = PotionType.valueOf(meta);
			} catch (Exception e) {
				type = PotionType.SPEED;
			}
			potionMeta.setBasePotionData(new PotionData(type));
		}
		if (itemMeta instanceof SkullMeta) {
			SkullMeta skullMeta = (SkullMeta) itemMeta;
			skullMeta.setOwningPlayer(getSkullOwner(meta));
		}
		if (itemMeta instanceof TropicalFishBucketMeta) {
			TropicalFishBucketMeta fishMeta = (TropicalFishBucketMeta) itemMeta;
			String[] metas = meta.split(":");
			if (metas.length < 2)
				return itemMeta;
			Pattern pattern;
			try {
				pattern = Pattern.valueOf(metas[1]);
			} catch (Exception e) {
				pattern = Pattern.BETTY;
			}
			fishMeta.setPattern(pattern);
			DyeColor color;
			try {
				color = DyeColor.valueOf(metas[0]);
			} catch (Exception e) {
				color = DyeColor.GREEN;
			}
			fishMeta.setBodyColor(color);
		}
		if (itemMeta instanceof SpawnEggMeta) {
			SpawnEggMeta eggMeta = (SpawnEggMeta) itemMeta;
			EntityType entity;
			try {
				entity = EntityType.valueOf(meta);
			} catch (Exception e) {
				entity = EntityType.ZOMBIE;
			}
			eggMeta.setSpawnedType(entity);
		}
		if (itemMeta instanceof LeatherArmorMeta) {
			LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
			Color color;
			String[] colors = meta.split(":");
			if (colors.length < 3)
				return itemMeta;
			int r = Integer.parseInt(colors[0]);
			int g = Integer.parseInt(colors[1]);
			int b = Integer.parseInt(colors[2]);
			try {
				color = Color.fromBGR(r, g, b);
			} catch (Exception e) {
				color = Color.RED;
			}
			leatherMeta.setColor(color);
		}
		if (itemMeta instanceof BannerMeta) {
			BannerMeta bannerMeta = (BannerMeta) itemMeta;
			DyeColor color;
			try {
				color = DyeColor.valueOf(meta);
			} catch (Exception e) {
				color = DyeColor.RED;
			}
			bannerMeta.setBaseColor(color);
		}
		return itemMeta;
	}

}
