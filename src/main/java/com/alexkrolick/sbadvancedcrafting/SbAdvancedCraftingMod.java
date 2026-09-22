package com.alexkrolick.sbadvancedcrafting;

import com.alexkrolick.sbadvancedcrafting.client.ClientSetup;
import com.alexkrolick.sbadvancedcrafting.init.ModItems;
import com.alexkrolick.sbadvancedcrafting.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(SbAdvancedCraftingMod.MOD_ID)
public class SbAdvancedCraftingMod {
	public static final String MOD_ID = "sb_advanced_crafting";
	public static final Logger LOGGER = LogUtils.getLogger();

	public SbAdvancedCraftingMod(IEventBus modBus) {
		ModItems.register(modBus);
		modBus.addListener(ModNetwork::register);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ClientSetup.init(modBus);
		}
		LOGGER.info("SB Advanced Crafting loaded");
	}
}
