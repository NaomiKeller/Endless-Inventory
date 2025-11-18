package com.kwwsyk.endinv.neoforge.util;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Ingredient of ItemStack with or without components.
 */
public class StackIngredient {

    public static StackIngredient EMPTY = new StackIngredient((ItemStack[]) null);

    @Nullable
    private  ItemStack[] stacks;
    private Predicate<PatchedDataComponentMap> componentPredicate = t -> true;


    public StackIngredient(Ingredient ingredient) {
        if(ingredient.isCustom()){
            var ci = ingredient.getCustomIngredient();

        }else{

        }
    }

    public StackIngredient(@Nullable ItemStack[] stacks) {
        this.stacks = stacks;
    }

    @Nullable
    public ItemStack[] getStacks() {
        return stacks;
    }
}
