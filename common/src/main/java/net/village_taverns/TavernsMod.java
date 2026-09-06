package net.village_taverns;

import net.tiny_config.ConfigManager;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.config.BrewingConfig;
import net.village_taverns.config.Defaults;
import net.village_taverns.village.VillageStructures;

public class TavernsMod {

    public static final String ID = "village_taverns";

    /// Brewing recipes for the SpellPower / RangedWeaponAPI potions. Config-driven because brewing
    /// has no datapack path in 1.20.1 — see [BrewingConfig]. Versioned, so bumping
    /// [BrewingConfig#SCHEMA_VERSION] regenerates stale files instead of leaving players on an old
    /// recipe list.
    public static ConfigManager<BrewingConfig> brewingConfig = new ConfigManager<>
            ("brewing", Defaults.brewing)
            .builder()
            .setDirectory(ID)
            .sanitize(true)
            .schemaVersion(BrewingConfig.SCHEMA_VERSION)
            .build();

    public static void init() {
        // Refreshed here, not at brewing-registration time, so a mid-session edit cannot apply
        // half-way through.
        brewingConfig.refresh();
        // Fabric-only on 1.20.1 (StructurePoolAPI has no Forge artifact); a no-op on Forge, where the
        // Lithostitched worldgen modifiers are the only injection path.
        VillageStructures.injectIfAvailable();
    }

    public static void registerBlocks() {
        TavernBlocks.register();
    }

    public static void registerBlockItems() {
        TavernBlocks.registerItems();
    }
}
