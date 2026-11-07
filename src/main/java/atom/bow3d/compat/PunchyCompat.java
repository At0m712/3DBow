package atom.bow3d.compat;

import atom.bow3d.ThreeDBow;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class PunchyCompat {

	public static void init() {
		if (!FabricLoader.getInstance().isModLoaded("punchy")) {
			return;
		}

		try {
			Class<?> apiClass = Class.forName("punchy.api.PunchyApi");

			Predicate<Object> isBowOrCrossbow = obj -> {
				if (obj instanceof ItemStack stack) {
					return stack.is(Items.BOW) || stack.is(Items.CROSSBOW)
						|| stack.getItem() == Items.BOW || stack.getItem() == Items.CROSSBOW;
				}
				return false;
			};

			BooleanSupplier isHoldingBowOrCrossbow = () -> {
				Minecraft mc = Minecraft.getInstance();
				if (mc.player != null) {
					ItemStack main = mc.player.getMainHandItem();
					ItemStack off = mc.player.getOffhandItem();
					return (main != null && (main.is(Items.BOW) || main.is(Items.CROSSBOW) || main.getItem() == Items.BOW || main.getItem() == Items.CROSSBOW))
						|| (off != null && (off.is(Items.BOW) || off.is(Items.CROSSBOW) || off.getItem() == Items.BOW || off.getItem() == Items.CROSSBOW));
				}
				return false;
			};

			// Boost our registration priority if method exists
			try {
				Method priority = apiClass.getMethod("priority", String.class, int.class);
				priority.invoke(null, "bow3d", 10000);
			} catch (Throwable ignored) {}

			// Register dual-handed surrender to vanilla/Minecraft
			boolean registeredAny = false;
			for (Method m : apiClass.getDeclaredMethods()) {
				try {
					if (m.getName().equals("leaveBothHandsToMinecraft") && m.getParameterCount() == 2) {
						m.invoke(null, "bow3d", isBowOrCrossbow);
						registeredAny = true;
					} else if (m.getName().equals("leaveItemsToMinecraft") && m.getParameterCount() == 2) {
						m.invoke(null, "bow3d", isBowOrCrossbow);
						registeredAny = true;
					} else if (m.getName().equals("useMinecraftHands") && m.getParameterCount() == 2) {
						m.invoke(null, "bow3d", isHoldingBowOrCrossbow);
						registeredAny = true;
					}
				} catch (Throwable ignored) {}
			}

			if (registeredAny) {
				ThreeDBow.LOGGER.info("3D Bow: Successfully registered Punchy compatibility (bow & crossbow overwrite Punchy animations)!");
			}
		} catch (Throwable t) {
			ThreeDBow.LOGGER.warn("3D Bow: Failed to initialize Punchy compatibility: " + t.getMessage());
		}
	}
}
