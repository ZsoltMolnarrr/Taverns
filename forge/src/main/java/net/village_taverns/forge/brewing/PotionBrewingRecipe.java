package net.village_taverns.forge.brewing;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraftforge.common.brewing.IBrewingRecipe;

/// Forge's stand-in for a vanilla `BrewingRecipeRegistry` potion recipe.
///
/// Forge 47 patches the brewing stand (block entity **and** screen handler) to route every check
/// through `net.minecraftforge.common.brewing.BrewingRecipeRegistry`, and vanilla's own
/// `registerPotionRecipe` is private, so the loader-neutral list is not reachable. Forge's built-in
/// `BrewingRecipe(Ingredient, Ingredient, ItemStack)` is not usable either: an `Ingredient` matches
/// by item only, so it would accept *any* potion as the base. This matches on the base potion itself.
///
/// The output keeps the input's own potion item, so a splash / lingering base brews into a splash /
/// lingering result exactly like vanilla.
public record PotionBrewingRecipe(Potion base, Item ingredient, Potion result) implements IBrewingRecipe {
    @Override
    public boolean isInput(ItemStack stack) {
        var item = stack.getItem();
        if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) {
            return false;
        }
        return PotionUtil.getPotion(stack) == base;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.isOf(ingredient);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredientStack) {
        if (!isInput(input) || !isIngredient(ingredientStack)) {
            return ItemStack.EMPTY;
        }
        return PotionUtil.setPotion(new ItemStack(input.getItem()), result);
    }
}
