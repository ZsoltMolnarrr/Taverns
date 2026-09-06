package net.village_taverns.forge;

import net.minecraftforge.fml.loading.LoadingModList;
import net.village_taverns.Platform;

public class PlatformImpl {
    public static class ForgeUtil implements Platform.Util {
        @Override
        public boolean isModLoaded(String modid) {
            // LoadingModList (not ModList) is populated during mod discovery, before any constructor
            // runs, so early gates match Fabric's "resolved up front" timing.
            return LoadingModList.get().getModFileById(modid) != null;
        }
    }
    private static final Platform.Util UTIL = new ForgeUtil();
    public static Platform.Util util() {
        return UTIL;
    }
}
