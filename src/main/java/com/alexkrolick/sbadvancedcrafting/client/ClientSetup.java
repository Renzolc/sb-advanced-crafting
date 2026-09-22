package com.alexkrolick.sbadvancedcrafting.client;

import com.alexkrolick.sbadvancedcrafting.init.ModItems;
import com.alexkrolick.sbadvancedcrafting.upgrade.AdvancedCraftingUpgradeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
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
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> UpgradeGuiManager.registerTab(ModItems.ADVANCED_CRAFTING_TYPE,
				(CraftingUpgradeContainer uc, Position p, StorageScreenBase<?> s) -> new AdvancedCraftingUpgradeTab(uc, p, s,
						SBPButtonDefinitions.SHIFT_CLICK_TARGET, SBPButtonDefinitions.REFILL_CRAFTING_GRID)));
	}
}
