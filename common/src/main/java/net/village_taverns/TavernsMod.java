package net.village_taverns;

import net.fabric_extras.structure_pool.api.StructurePoolAPI;
import net.fabric_extras.structure_pool.api.StructurePoolConfig;
import net.tiny_config.ConfigManager;
import net.village_taverns.block.TavernBlocks;
import net.village_taverns.config.BrewingConfig;
import net.village_taverns.config.Defaults;

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

    public static ConfigManager<StructurePoolConfig> villageConfig = new ConfigManager<>
            ("villages", Defaults.villages)
            .builder()
            .setDirectory(ID)
            .sanitize(true)
            .build();

    public static void init() {
        // Refreshed here, not at brewing-registration time, so a mid-session edit cannot apply
        // half-way through.
        brewingConfig.refresh();

        villageConfig.refresh();
        if (!Platform.util().isModLoaded("lithostitched")) {
            // Only inject the tavern if Lithostitched is not present - otherwise the data-driven
            // worldgen modifiers in `data/village_taverns/lithostitched/` already do it.
            //
            // `injectAll` only *queues* the entries; StructurePoolAPI's own entrypoint applies them
            // when the server starts (Fabric SERVER_STARTING / Forge ServerAboutToStartEvent, both
            // before the spawn region generates). The queue is deliberately never cleared, so this
            // must be called exactly once, here at mod init - never per world load.
            StructurePoolAPI.injectAll(villageConfig.value);
        }
        villageConfig.save();
    }

    public static void registerBlocks() {
        TavernBlocks.register();
    }

    public static void registerBlockItems() {
        TavernBlocks.registerItems();
    }
}
