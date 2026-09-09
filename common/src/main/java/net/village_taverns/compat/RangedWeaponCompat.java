package net.village_taverns.compat;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;

public class RangedWeaponCompat {
    /// Files the opt-in request for RangedWeaponAPI's potions. It does **not** register anything itself:
    /// RangedWeaponAPI only flags the request and fulfils it from its own potion registration window
    /// (Fabric: `Potions.<clinit>` TAIL; Forge: `RegisterEvent(POTIONS)`).
    ///
    /// **Timing:** on Forge this must be called before `RegisterEvent(POTIONS)` is posted, which is why
    /// Taverns calls it from the `@Mod` constructor. Filing it from Taverns' own POTION window instead
    /// would make it a coin-toss on mod dispatch order: Forge posts the one POTION event to every mod's
    /// bus in turn, and `mods.toml` declares `ordering = "NONE"` for this dependency, so RangedWeaponAPI's
    /// block may well have run — and read an unset request flag — before Taverns' block ever executes.
    /// The mod constructor is before every one of those, so it cannot lose that race.
    ///
    /// On Fabric the `Potions` `<clinit>` TAIL mixin is early enough.
    public static void init() {
        RangedWeaponMod.registerPotions();
    }
}
