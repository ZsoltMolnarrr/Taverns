package net.village_taverns.neoforge;

import net.neoforged.fml.loading.LoadingModList;
import net.village_taverns.Platform;

public class PlatformImpl {
    public static class NeoForgeUtil implements Platform.Util {
        @Override
        public boolean isModLoaded(String modid) {
            // Exact same check as SpellEngine's Platform.Util: LoadingModList (not ModList) is populated
            // during mod discovery, before any constructor runs, so early gates in static initializers
            // (the Potions <clinit> mixin) match Fabric's "resolved up front" timing.
            return LoadingModList.get().getModFileById(modid) != null;
        }
    }
    private static final Platform.Util UTIL = new NeoForgeUtil();
    public static Platform.Util util() {
        return UTIL;
    }
}
