package atom.bow3d.client;

import net.minecraft.util.Util;

public class BowShotTracker {
	public static final long RELOAD_DURATION_MS = 450L;
	private static long lastShotTimeMs = 0L;

	public static void onShotFired() {
		lastShotTimeMs = Util.getMillis();
	}

	public static void cancelReload() {
		lastShotTimeMs = 0L;
	}

	public static boolean isReloading() {
		return getReloadProgress() < 1.0F;
	}

	public static float getReloadProgress() {
		if (lastShotTimeMs == 0L) {
			return 1.0F;
		}
		long elapsed = Util.getMillis() - lastShotTimeMs;
		if (elapsed >= RELOAD_DURATION_MS) {
			return 1.0F;
		}
		return (float) elapsed / (float) RELOAD_DURATION_MS;
	}
}
