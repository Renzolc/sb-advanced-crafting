package com.alexkrolick.sbadvancedcrafting.upgrade;

import com.alexkrolick.sbadvancedcrafting.client.recipebook.DualSourceRecipeBookComponent;
import com.alexkrolick.sbadvancedcrafting.client.recipebook.DualSourceRecipeBookMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.ICraftingUIPart;

import static net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper.GUI_CONTROLS;

/**
 * Advanced crafting upgrade tab embedding the vanilla green {@link RecipeBookComponent}.
 * Placement and craftability consider backpack storage and player inventory.
 */
public class AdvancedCraftingUpgradeTab extends UpgradeSettingsTab<CraftingUpgradeContainer> {
	private static final int BOOK_PANEL_WIDTH = 147;
	private static final int BOOK_TAB_OVERHANG = 30;
	private static final int BOOK_SECTION = BOOK_TAB_OVERHANG + BOOK_PANEL_WIDTH;
	private static final TextureBlitData ARROW = new TextureBlitData(GUI_CONTROLS, new UV(97, 216), new Dimension(15, 8));

	private final ICraftingUIPart craftingUIAddition;
	private final DualSourceRecipeBookComponent recipeBook = new DualSourceRecipeBookComponent();
	private DualSourceRecipeBookMenu bridgeMenu;
	private ImageButton recipeToggleButton;
	private boolean bookVisible = true;
	private final int craftSectionWidth;

	public AdvancedCraftingUpgradeTab(CraftingUpgradeContainer upgradeContainer, Position position, StorageScreenBase<?> screen,
			ButtonDefinition.Toggle<Boolean> shiftClickTargetButton, ButtonDefinition.Toggle<Boolean> refillCraftingGridButton) {
		super(upgradeContainer, position, screen, Component.translatable("gui.sb_advanced_crafting.tab.advanced_crafting"),
				Component.translatable("gui.sb_advanced_crafting.tab.advanced_crafting.tooltip"));

		craftingUIAddition = screen.getCraftingUIAddition();
		craftSectionWidth = 63 + craftingUIAddition.getWidth();
		updateOpenDimensions();

		addHideableChild(new ToggleButton<>(new Position(x + BOOK_SECTION + 3, y + 24), shiftClickTargetButton,
				button -> getContainer().setShiftClickIntoStorage(!getContainer().shouldShiftClickIntoStorage()),
				getContainer()::shouldShiftClickIntoStorage));
		addHideableChild(new ToggleButton<>(new Position(x + BOOK_SECTION + 21, y + 24), refillCraftingGridButton,
				button -> getContainer().setRefillCraftingGrid(!getContainer().shouldRefillCraftingGrid()),
				getContainer()::shouldRefillCraftingGrid));
	}

	private void updateOpenDimensions() {
		if (bookVisible) {
			openTabDimension = new Dimension(BOOK_SECTION + craftSectionWidth, Math.max(186, 148));
		} else {
			openTabDimension = new Dimension(craftSectionWidth, 148);
		}
	}

	private int bookContentLeft() {
		return bookVisible ? BOOK_SECTION : 0;
	}

	@Override
	protected void onTabOpen() {
		super.onTabOpen();
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && screen.getMenu() instanceof StorageContainerMenuBase<?> storageMenu) {
			bridgeMenu = new DualSourceRecipeBookMenu(storageMenu, getContainer());
			if (bookVisible) {
				initRecipeBook();
			}
		}
		ensureRecipeToggleButton();
		repositionRecipeToggle();
	}

	@Override
	protected void onTabClose() {
		super.onTabClose();
		craftingUIAddition.onCraftingSlotsHidden();
		recipeBook.setBookVisible(false);
		bridgeMenu = null;
	}

	private void ensureRecipeToggleButton() {
		if (recipeToggleButton == null) {
			recipeToggleButton = new ImageButton(0, 0, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, btn -> toggleBook());
		}
	}

	private void toggleBook() {
		bookVisible = !bookVisible;
		updateOpenDimensions();
		if (isOpen) {
			setWidth(Math.max(openTabDimension.width(), 21));
			setHeight(openTabDimension.height());
		}
		if (bookVisible) {
			initRecipeBook();
		} else {
			recipeBook.setBookVisible(false);
		}
		moveSlotsToTab();
		repositionRecipeToggle();
	}

	private void initRecipeBook() {
		Minecraft mc = Minecraft.getInstance();
		if (bridgeMenu == null || mc.player == null) {
			return;
		}
		recipeBook.initAnchored(mc, bridgeMenu, x + BOOK_TAB_OVERHANG, y + 20);
	}

	private void repositionRecipeToggle() {
		if (recipeToggleButton == null) {
			return;
		}
		int gridLeft = x + bookContentLeft() + craftingUIAddition.getWidth();
		recipeToggleButton.setPosition(gridLeft + 3, y + 42);
	}

	@Override
	public void tick() {
		if (isOpen && bookVisible && recipeBook.isVisible()) {
			recipeBook.reanchor(x + BOOK_TAB_OVERHANG, y + 20);
			recipeBook.tick();
			if (bridgeMenu != null) {
				bridgeMenu.syncSlotPositions();
			}
		}
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, Minecraft minecraft, int mouseX, int mouseY) {
		super.renderBg(guiGraphics, minecraft, mouseX, mouseY);
		if (!getContainer().isOpen()) {
			return;
		}
		int gridLeft = x + bookContentLeft() + craftingUIAddition.getWidth();
		GuiHelper.renderSlotsBackground(guiGraphics, gridLeft + 3, y + 44, 3, 3);
		GuiHelper.blit(guiGraphics, gridLeft + 3 + 19, y + 101, ARROW);
		GuiHelper.blit(guiGraphics, gridLeft + 3 + 14, y + 111, GuiHelper.CRAFTING_RESULT_SLOT);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		if (!getContainer().isOpen()) {
			return;
		}
		repositionRecipeToggle();
		if (recipeToggleButton != null) {
			recipeToggleButton.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		if (bookVisible && recipeBook.isVisible()) {
			recipeBook.render(guiGraphics, mouseX, mouseY, partialTicks);
			recipeBook.renderGhostRecipe(guiGraphics, screen.getGuiLeft(), screen.getGuiTop(), true, partialTicks);
		}
	}

	@Override
	public void renderTooltip(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderTooltip(screen, guiGraphics, mouseX, mouseY);
		if (isOpen && bookVisible && recipeBook.isVisible()) {
			recipeBook.renderTooltip(guiGraphics, this.screen.getGuiLeft(), this.screen.getGuiTop(), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isOpen && recipeToggleButton != null && recipeToggleButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (isOpen && bookVisible && recipeBook.isVisible() && recipeBook.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {
		return isOpen && bookVisible && recipeBook.isVisible() && recipeBook.keyPressed(keyCode, scanCode, modifiers);
	}

	public boolean handleCharTyped(char codePoint, int modifiers) {
		return isOpen && bookVisible && recipeBook.isVisible() && recipeBook.charTyped(codePoint, modifiers);
	}

	@Override
	protected void moveSlotsToTab() {
		int gridLeftOffset = bookContentLeft() + craftingUIAddition.getWidth();
		int slotNumber = 0;
		for (Slot slot : getContainer().getSlots()) {
			slot.x = x + 3 + gridLeftOffset - screen.getGuiLeft() + 1 + (slotNumber % 3) * 18;
			slot.y = y + 44 - screen.getGuiTop() + 1 + (slotNumber / 3) * 18;
			slotNumber++;
			if (slotNumber >= 9) {
				break;
			}
		}

		Slot craftingResult = getContainer().getSlots().get(9);
		craftingResult.x = x + 3 + gridLeftOffset - screen.getGuiLeft() + 19;
		craftingResult.y = y + 44 - screen.getGuiTop() + 72;

		craftingUIAddition.onCraftingSlotsDisplayed(getContainer().getSlots());
		if (bridgeMenu != null) {
			bridgeMenu.syncSlotPositions();
		}
	}
}
