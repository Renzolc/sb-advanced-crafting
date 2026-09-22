package com.alexkrolick.sbadvancedcrafting.upgrade;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Builds a 9-slot ingredient template for dual-source transfer (first matching stack per ingredient).
 */
public final class RecipePlacementHelper {
	private RecipePlacementHelper() {
	}

	public static List<ItemStack> expandCraftingRecipeToGrid(Level level, ResourceLocation recipeId) {
		Optional<RecipeHolder<?>> holderOpt = level.getRecipeManager().byKey(recipeId);
		if (holderOpt.isEmpty() || !(holderOpt.get().value() instanceof CraftingRecipe craftingRecipe)
				|| craftingRecipe.getType() != RecipeType.CRAFTING) {
			return List.of();
		}

		List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
		NonNullList<Ingredient> ingredients = craftingRecipe.getIngredients();

		if (craftingRecipe instanceof ShapedRecipe shaped) {
			int width = shaped.getWidth();
			int height = shaped.getHeight();
			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++) {
					int ingredientIndex = row * width + col;
					if (ingredientIndex >= ingredients.size()) {
						continue;
					}
					ItemStack template = firstMatch(ingredients.get(ingredientIndex));
					if (!template.isEmpty()) {
						grid.set(row * 3 + col, template);
					}
				}
			}
		} else {
			int slot = 0;
			for (Ingredient ingredient : ingredients) {
				if (slot >= 9) {
					break;
				}
				ItemStack template = firstMatch(ingredient);
				if (!template.isEmpty()) {
					grid.set(slot, template);
				}
				slot++;
			}
		}
		return grid;
	}

	private static ItemStack firstMatch(Ingredient ingredient) {
		if (ingredient.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack[] items = ingredient.getItems();
		if (items.length == 0) {
			return ItemStack.EMPTY;
		}
		ItemStack copy = items[0].copy();
		copy.setCount(1);
		return copy;
	}
}
