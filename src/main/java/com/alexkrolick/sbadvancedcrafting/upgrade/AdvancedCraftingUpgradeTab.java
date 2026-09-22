package com.alexkrolick.sbadvancedcrafting.upgrade;

import com.alexkrolick.sbadvancedcrafting.network.PlaceCraftingRecipePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.ICraftingUIPart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.GUI_CONTROLS;

/**
 * Crafting upgrade tab with an embedded recipe-book-like browser. Clicking a recipe sends
 * {@link PlaceCraftingRecipePayload}, which uses Sophisticated Core dual-source transfer
 * (backpack storage + player inventory).
 */
public class AdvancedCraftingUpgradeTab extends UpgradeSettingsTab<CraftingUpgradeContainer> {
	private static final int BOOK_WIDTH = 112;
	private static final int BOOK_COLS = 5;
	private static final int BOOK_ROWS = 5;
	private static final int RECIPES_PER_PAGE = BOOK_COLS * BOOK_ROWS;
	private static final TextureBlitData ARROW = new TextureBlitData(GUI_CONTROLS, new UV(97, 216), new Dimension(15, 8));

	private final ICraftingUIPart craftingUIAddition;
	private final TextBox searchBox;
	private final Button prevPageButton;
	private final Button nextPageButton;
	private List<RecipeHolder<CraftingRecipe>> allRecipes = List.of();
	private List<RecipeHolder<CraftingRecipe>> filteredRecipes = List.of();
	private int page;
	private String lastSearch = "";

	public AdvancedCraftingUpgradeTab(CraftingUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen,
			ButtonDefinition.Toggle<Boolean> shiftClickTargetButton, ButtonDefinition.Toggle<Boolean> refillCraftingGridButton) {
		super(upgradeContainer, position, screen, Component.translatable("gui.sb_advanced_crafting.tab.advanced_crafting"),
				Component.translatable("gui.sb_advanced_crafting.tab.advanced_crafting.tooltip"));

		craftingUIAddition = screen.getCraftingUIAddition();
		int gridLeft = BOOK_WIDTH + craftingUIAddition.getWidth();
		openTabDimension = new Dimension(63 + gridLeft, 148);

		addHideableChild(new ToggleButton<>(new Position(x + 3 + BOOK_WIDTH, y + 24), shiftClickTargetButton,
				button -> getContainer().setShiftClickIntoStorage(!getContainer().shouldShiftClickIntoStorage()), getContainer()::shouldShiftClickIntoStorage));
		addHideableChild(new ToggleButton<>(new Position(x + 21 + BOOK_WIDTH, y + 24), refillCraftingGridButton,
				button -> getContainer().setRefillCraftingGrid(!getContainer().shouldRefillCraftingGrid()), getContainer()::shouldRefillCraftingGrid));

		searchBox = new TextBox(new Position(x + 4, y + 24), new Dimension(BOOK_WIDTH - 8, 12));
		searchBox.setBordered(true);
		searchBox.setMaxLength(40);
		searchBox.setUnfocusedEmptyHint("Search...");
		searchBox.setResponder(value -> {
			page = 0;
			applyFilter();
		});
		addHideableChild(searchBox);

		prevPageButton = new Button(new Position(x + 4, y + 130),
				new ButtonDefinition(new Dimension(8, 12),
						new TextureBlitData(GUI_CONTROLS, new UV(53, 18), new Dimension(8, 12)),
						new TextureBlitData(GUI_CONTROLS, new UV(61, 18), new Dimension(8, 12)),
						new TextureBlitData(GuiHelper.ICONS, new Position(0, 0), Dimension.SQUARE_256, new UV(48, 144), new Dimension(8, 12)),
						Component.translatable("gui.sb_advanced_crafting.button.prev_page")),
				button -> {
					if (button == 0 && page > 0) {
						page--;
					}
				});
		addHideableChild(prevPageButton);

		nextPageButton = new Button(new Position(x + BOOK_WIDTH - 20, y + 130),
				new ButtonDefinition(new Dimension(8, 12),
						new TextureBlitData(GUI_CONTROLS, new UV(53, 18), new Dimension(8, 12)),
						new TextureBlitData(GUI_CONTROLS, new UV(61, 18), new Dimension(8, 12)),
						new TextureBlitData(GuiHelper.ICONS, new Position(0, 0), Dimension.SQUARE_256, new UV(56, 144), new Dimension(8, 12)),
						Component.translatable("gui.sb_advanced_crafting.button.next_page")),
				button -> {
					if (button == 0 && (page + 1) * RECIPES_PER_PAGE < filteredRecipes.size()) {
						page++;
					}
				});
		addHideableChild(nextPageButton);

	}

	@Override
	protected void onTabOpen() {
		super.onTabOpen();
		reloadRecipes();
		applyFilter();
	}

	@Override
	protected void onTabClose() {
		super.onTabClose();
		craftingUIAddition.onCraftingSlotsHidden();
	}

	private void reloadRecipes() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) {
			allRecipes = List.of();
			return;
		}
		List<RecipeHolder<CraftingRecipe>> list = new ArrayList<>(mc.level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING));
		list.sort(Comparator.comparing(r -> BuiltInRegistries.ITEM.getKey(r.value().getResultItem(mc.level.registryAccess()).getItem()).toString()));
		allRecipes = list;
	}

	private void applyFilter() {
		String query = searchBox.getValue() == null ? "" : searchBox.getValue().trim().toLowerCase(Locale.ROOT);
		lastSearch = query;
		Minecraft mc = Minecraft.getInstance();
		if (query.isEmpty()) {
			filteredRecipes = allRecipes;
			return;
		}
		List<RecipeHolder<CraftingRecipe>> list = new ArrayList<>();
		for (RecipeHolder<CraftingRecipe> holder : allRecipes) {
			ItemStack result = holder.value().getResultItem(mc.level.registryAccess());
			String name = result.getHoverName().getString().toLowerCase(Locale.ROOT);
			String id = holder.id().toString().toLowerCase(Locale.ROOT);
			if (name.contains(query) || id.contains(query)) {
				list.add(holder);
			}
		}
		filteredRecipes = list;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		super.renderBg(guiGraphics, minecraft, mouseX, mouseY);
		if (!getContainer().isOpen()) {
			return;
		}

		int gridLeft = x + 3 + BOOK_WIDTH + craftingUIAddition.getWidth();
		GuiHelper.renderSlotsBackground(guiGraphics, gridLeft, y + 44, 3, 3);
		GuiHelper.blit(guiGraphics, gridLeft + 19, y + 101, ARROW);
		GuiHelper.blit(guiGraphics, gridLeft + 14, y + 111, GuiHelper.CRAFTING_RESULT_SLOT);

		// recipe book panel background
		guiGraphics.fill(x + 3, y + 40, x + 3 + BOOK_WIDTH - 2, y + 128, 0xAA1A1A1A);
		int bx = x + 3, by = y + 40, bw = BOOK_WIDTH - 2, bh = 88;
		guiGraphics.fill(bx, by, bx + bw, by + 1, 0xFF55FFFF);
		guiGraphics.fill(bx, by + bh - 1, bx + bw, by + bh, 0xFF55FFFF);
		guiGraphics.fill(bx, by, bx + 1, by + bh, 0xFF55FFFF);
		guiGraphics.fill(bx + bw - 1, by, bx + bw, by + bh, 0xFF55FFFF);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (getContainer().isOpen() && !searchBox.getValue().equals(lastSearch)) {
			applyFilter();
		}
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (!getContainer().isOpen()) {
			return;
		}

		prevPageButton.setVisible(page > 0);
		nextPageButton.setVisible((page + 1) * RECIPES_PER_PAGE < filteredRecipes.size());

		int start = page * RECIPES_PER_PAGE;
		int end = Math.min(start + RECIPES_PER_PAGE, filteredRecipes.size());
		Minecraft mc = Minecraft.getInstance();
		for (int i = start; i < end; i++) {
			int local = i - start;
			int col = local % BOOK_COLS;
			int row = local / BOOK_COLS;
			int slotX = x + 6 + col * 20;
			int slotY = y + 44 + row * 16;
			ItemStack result = filteredRecipes.get(i).value().getResultItem(mc.level.registryAccess());
			guiGraphics.renderItem(result, slotX, slotY);
			guiGraphics.renderItemDecorations(font, result, slotX, slotY, null);
		}

		getHoveredRecipeIndex(mouseX, mouseY).ifPresent(idx -> {
			ItemStack result = filteredRecipes.get(idx).value().getResultItem(mc.level.registryAccess());
			AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 6 + ((idx - page * RECIPES_PER_PAGE) % BOOK_COLS) * 20,
					y + 44 + ((idx - page * RECIPES_PER_PAGE) / BOOK_COLS) * 16, 0, -2130706433);
		});

		String pageLabel = (filteredRecipes.isEmpty() ? 0 : page + 1) + "/" + Math.max(1, (filteredRecipes.size() + RECIPES_PER_PAGE - 1) / RECIPES_PER_PAGE);
		guiGraphics.drawCenteredString(font, pageLabel, x + BOOK_WIDTH / 2, y + 132, 0xFFFFFF);
	}

	@Override
	public void renderTooltip(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderTooltip(screen, guiGraphics, mouseX, mouseY);
		getHoveredRecipeIndex(mouseX, mouseY).ifPresent(idx -> {
			ItemStack result = filteredRecipes.get(idx).value().getResultItem(Minecraft.getInstance().level.registryAccess());
			guiGraphics.renderTooltip(font, result, mouseX, mouseY);
		});
	}

	private Optional<Integer> getHoveredRecipeIndex(int mouseX, int mouseY) {
		if (!getContainer().isOpen() || filteredRecipes.isEmpty()) {
			return Optional.empty();
		}
		int start = page * RECIPES_PER_PAGE;
		int end = Math.min(start + RECIPES_PER_PAGE, filteredRecipes.size());
		for (int i = start; i < end; i++) {
			int local = i - start;
			int col = local % BOOK_COLS;
			int row = local / BOOK_COLS;
			int slotX = x + 6 + col * 20;
			int slotY = y + 44 + row * 16;
			if (mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return getHoveredRecipeIndex((int) mouseX, (int) mouseY).map(idx -> {
			RecipeHolder<CraftingRecipe> holder = filteredRecipes.get(idx);
			boolean maxTransfer = Screen.hasShiftDown() || button == 1;
			PacketDistributor.sendToServer(new PlaceCraftingRecipePayload(holder.id(), maxTransfer));
			return true;
		}).orElse(false);
	}

	@Override
	protected void moveSlotsToTab() {
		int gridLeftOffset = BOOK_WIDTH + craftingUIAddition.getWidth();
		int slotNumber = 0;
		for (Slot slot : getContainer().getSlots()) {
			slot.x = x + 3 + gridLeftOffset - screen.getGuiLeft() + 1 + (slotNumber % 3) * 18;
			slot.y = y + 44 - screen.getGuiTop() + 1 + (slotNumber / 3) * 18;
			slotNumber++;
			if (slotNumber >= 9) {
				break;
			}
		}

		Slot craftingSlot = getContainer().getSlots().get(9);
		craftingSlot.x = x + 3 + gridLeftOffset - screen.getGuiLeft() + 19;
		craftingSlot.y = y + 44 - screen.getGuiTop() + 72;

		craftingUIAddition.onCraftingSlotsDisplayed(getContainer().getSlots());
	}
}
