package com.alexkrolick.sbadvancedcrafting.upgrade;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeItem;

import java.util.List;

/**
 * Advanced crafting upgrade: same crafting wrapper/container as stock, conflicts with any
 * {@link CraftingUpgradeItem} (including the stock Sophisticated Backpacks crafting upgrade),
 * and is paired with a custom tab that hosts a recipe-book-like browser.
 */
public class AdvancedCraftingUpgradeItem extends CraftingUpgradeItem {
	private static final List<UpgradeConflictDefinition> CONFLICTS = List.of(new UpgradeConflictDefinition(CraftingUpgradeItem.class::isInstance, 0,
			Component.translatable("gui.sb_advanced_crafting.error.crafting_upgrade_exists")));

	public AdvancedCraftingUpgradeItem(IUpgradeCountLimitConfig upgradeTypeLimitConfig) {
		super(upgradeTypeLimitConfig);
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return CONFLICTS;
	}
}
