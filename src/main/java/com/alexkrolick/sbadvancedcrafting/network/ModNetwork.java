package com.alexkrolick.sbadvancedcrafting.network;

import com.alexkrolick.sbadvancedcrafting.SbAdvancedCraftingMod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
	private ModNetwork() {
	}

	public static void register(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar(SbAdvancedCraftingMod.MOD_ID).versioned("1");
		registrar.playToServer(PlaceCraftingRecipePayload.TYPE, PlaceCraftingRecipePayload.STREAM_CODEC, PlaceCraftingRecipePayload::handle);
	}
}
