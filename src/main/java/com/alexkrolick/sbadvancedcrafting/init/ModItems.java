package com.alexkrolick.sbadvancedcrafting.init;

import com.alexkrolick.sbadvancedcrafting.SbAdvancedCraftingMod;
import com.alexkrolick.sbadvancedcrafting.upgrade.AdvancedCraftingUpgradeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.Config;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeWrapper;

public final class ModItems {
	private ModItems() {
	}

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SbAdvancedCraftingMod.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
			SbAdvancedCraftingMod.MOD_ID);

	public static final DeferredHolder<Item, AdvancedCraftingUpgradeItem> ADVANCED_CRAFTING_UPGRADE = ITEMS.register("advanced_crafting_upgrade",
			() -> new AdvancedCraftingUpgradeItem(Config.SERVER.maxUpgradesPerStorage));

	public static final UpgradeContainerType<CraftingUpgradeWrapper, CraftingUpgradeContainer> ADVANCED_CRAFTING_TYPE = new UpgradeContainerType<>(
			CraftingUpgradeContainer::new);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = CREATIVE_TABS.register("main",
			() -> CreativeModeTab.builder().title(Component.translatable("itemGroup.sb_advanced_crafting"))
					.icon(() -> new ItemStack(ADVANCED_CRAFTING_UPGRADE.get())).displayItems((params, output) -> output.accept(ADVANCED_CRAFTING_UPGRADE.get()))
					.build());

	public static void register(IEventBus modBus) {
		ITEMS.register(modBus);
		CREATIVE_TABS.register(modBus);
		modBus.addListener(ModItems::registerContainers);
	}

	private static void registerContainers(RegisterEvent event) {
		if (event.getRegistryKey().equals(Registries.MENU)) {
			UpgradeContainerRegistry.register(ADVANCED_CRAFTING_UPGRADE.getId(), ADVANCED_CRAFTING_TYPE);
		}
	}
}
