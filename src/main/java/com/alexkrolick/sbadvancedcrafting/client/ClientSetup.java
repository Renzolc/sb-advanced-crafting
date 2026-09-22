package com.alexkrolick.sbadvancedcrafting.client;

import com.alexkrolick.sbadvancedcrafting.init.ModItems;
import com.alexkrolick.sbadvancedcrafting.upgrade.AdvancedCraftingUpgradeTab;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.SBPButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;

public final class ClientSetup {
	private ClientSetup() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener(ClientSetup::onClientSetup);
		NeoForge.EVENT_BUS.addListener(ClientSetup::onKeyPressed);
		NeoForge.EVENT_BUS.addListener(ClientSetup::onCharTyped);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> UpgradeGuiManager.registerTab(ModItems.ADVANCED_CRAFTING_TYPE,
				(CraftingUpgradeContainer uc, Position p, StorageScreenBase<?> s) -> new AdvancedCraftingUpgradeTab(uc, p, s,
						SBPButtonDefinitions.SHIFT_CLICK_TARGET, SBPButtonDefinitions.REFILL_CRAFTING_GRID)));
	}

	private static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
		findOpenAdvancedTab(event.getScreen()).ifPresent(tab -> {
			if (tab.handleKeyPressed(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
				event.setCanceled(true);
			}
		});
	}

	private static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
		findOpenAdvancedTab(event.getScreen()).ifPresent(tab -> {
			if (tab.handleCharTyped(event.getCodePoint(), event.getModifiers())) {
				event.setCanceled(true);
			}
		});
	}

	private static java.util.Optional<AdvancedCraftingUpgradeTab> findOpenAdvancedTab(Screen screen) {
		if (!(screen instanceof StorageScreenBase<?> storageScreen)) {
			return java.util.Optional.empty();
		}
		return storageScreen.getUpgradeSettingsControl().getOpenTab()
				.filter(AdvancedCraftingUpgradeTab.class::isInstance)
				.map(AdvancedCraftingUpgradeTab.class::cast);
	}
}
