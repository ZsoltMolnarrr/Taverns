package net.village_taverns.compat;

import net.minecraft.potion.Potion;
import net.minecraft.util.Identifier;
import net.spell_power.SpellPowerMod;

import java.util.Map;

public class SpellPowerCompat {
    /// Fabric: registers Spell Power's potions straight away, from the `Potions` `<clinit>` TAIL window.
    public static void init() {
        SpellPowerMod.registerPotions();
    }

    /// Every potion Spell Power would add, keyed by the id it registers under. Creation only — nothing is
    /// registered here, so a loader that registers potions itself iterates this instead of calling
    /// #init(). Spell Power's own `register_potions` config is deliberately not consulted, exactly as
    /// #init() does not consult it: asking for these potions *is* Taverns' opt-in, and the brewing tree
    /// and the bartender's trade table are both built on top of them.
    public static Map<Identifier, Potion> potionsToRegister() {
        return SpellPowerMod.potionsToRegister();
    }
}
