package net.village_taverns.fabric.mixin;

import net.minecraft.potion.Potions;
import net.village_taverns.Platform;
import net.village_taverns.compat.RangedWeaponCompat;
import net.village_taverns.compat.SpellPowerCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Fabric-only. SpellPower and RangedWeaponAPI both register their potions opt-in, and on Fabric the
/// window for that is the `Potions` `<clinit>` TAIL (which is where their own mixins hook too).
///
/// This mixin does **not** exist on Forge: there, vanilla registries are locked outside their
/// `RegisterEvent` window, so `ForgeMod` asks for the same potions from the POTION window instead.
@Mixin(Potions.class)
public class PotionsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_SpellPower(CallbackInfo ci) {
        if (Platform.util().isModLoaded("spell_power")) {
            try {
                SpellPowerCompat.init();
            } catch (Throwable t) { }
        }
        if (Platform.util().isModLoaded("ranged_weapon_api")) {
            try {
                RangedWeaponCompat.init();
            } catch (Throwable t) { }
        }
    }
}
