package net.village_taverns;

import dev.architectury.injectables.annotations.ExpectPlatform;

/// Loader-neutral platform seam, mirroring SpellEngine's `net.spell_engine.Platform`. Taverns has no
/// SpellEngine dependency, so it carries its own equivalent seam. The `@ExpectPlatform` method is
/// rewired by the Architectury transformer to the matching `PlatformImpl` in each loader's module,
/// so `common` needs no loader API and the check is resolved statically (no mutable global to install).
public class Platform {
    public interface Util {
        /// Whether another mod is present. Fabric: `FabricLoader.isModLoaded`; Forge:
        /// `LoadingModList.get().getModFileById(modid) != null` — populated during mod discovery
        /// (before any constructor runs), so early gates read a correct answer.
        boolean isModLoaded(String modid);
    }

    @ExpectPlatform
    public static Util util() {
        throw new AssertionError();
    }
}
