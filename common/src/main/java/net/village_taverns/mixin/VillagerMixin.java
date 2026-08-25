package net.village_taverns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerData;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.village_taverns.TavernVillagers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/// Swaps the bartender's brain schedule for the always-work one.
///
/// 1.21.11: `Brain#setSchedule` takes an `EnvironmentAttribute<Activity>` instead of a `Schedule`
/// (the `minecraft:schedule` registry is gone), so the `@At` descriptor changed with it. Loom 1.17
/// no longer runs the Mixin AP, so a stale descriptor would compile and silently never apply.
@Mixin(VillagerEntity.class)
public abstract class VillagerMixin {
    @Shadow public abstract VillagerData getVillagerData();

    @WrapOperation(
            method = "initBrain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/Brain;setSchedule(Lnet/minecraft/world/attribute/EnvironmentAttribute;)V")
    )
    private void wrapInitBrain(Brain instance, EnvironmentAttribute<Activity> schedule, Operation<Void> original) {
        // `VillagerData#profession()` is a `RegistryEntry<VillagerProfession>` now — comparing it to a
        // bare `VillagerProfession` would compile and never match, so match on the registry key.
        var profession = getVillagerData().profession();
        if (profession.matchesKey(TavernVillagers.BARTENDER_PROFESSION_KEY)) {
            original.call(instance, TavernVillagers.ALWAYS_WORK_ACTIVITY);
        } else {
            original.call(instance, schedule);
        }
    }
}
