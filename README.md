# SB Advanced Crafting

NeoForge 1.21.1 addon for [Sophisticated Backpacks](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks) that adds an **Advanced Crafting Upgrade** with the **vanilla green recipe book** and dual-source ingredient pull (backpack storage + player inventory).

## Features

- New upgrade item: **Advanced Crafting Upgrade** (separate from stock `crafting_upgrade`)
- Opens a crafting tab with the **vanilla green `RecipeBookComponent`** (tabs, search, craftable filter, ghost recipe)
- Recipe placement pulls ingredients from **backpack storage and player inventory** (same dual-source path JEI/EMI use)
- **Shift-click** a recipe for max transfer
- Green recipe-book toggle button (vanilla sprites) shows/hides the book panel
- Conflicts with the stock Crafting Upgrade (only one crafting upgrade per backpack)
- Tagged with `sophisticatedbackpacks:upgrade` so it can be inserted into backpack upgrade slots

## Crafting recipe

```
D T D
G C G
D E D
```

| Key | Item |
|-----|------|
| C | `sophisticatedbackpacks:crafting_upgrade` |
| D | Diamond |
| G | Gold Ingot |
| E | Eye of Ender |
| T | Crafting Table |

More expensive than the stock crafting upgrade (which is iron + crafting table + chest + upgrade base).

## Dependencies

| Mod | Version (tested) |
|-----|------------------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.251 |
| Sophisticated Core | 1.21.1-1.5.1.2341 |
| Sophisticated Backpacks | 1.21.1-3.26.3.2158 |

## Build

```bash
./gradlew build
```

Jar: `build/libs/sb_advanced_crafting-1.0.2.jar`

## How the green recipe book works

1. Open the Advanced Crafting upgrade tab on a backpack.
2. The vanilla green recipe book panel sits to the left of the 3×3 grid (category tabs, search, craftable filter).
3. Click the green book button to show/hide the panel (tab width adjusts).
4. Click a recipe to place it; **Shift-click** places as many crafts as possible (`maxTransfer`).
5. If ingredients are missing, the usual ghost outline appears on the grid.

### Dual-source craftability & placement

- **Craftability** (“can craft” highlighting / craftable filter) counts items in:
  - player inventory, and
  - backpack storage slots (excluding the craft grid and result slot).
- **Placement** sends `sb_advanced_crafting:place_crafting_recipe` to the server, which expands the recipe into a 3×3 template (shaped recipes are **centered** like vanilla) and calls Sophisticated Core’s `CraftingContainerRecipeTransferHandlerServer.setItemsWithStacks` with inventory slot indexes from backpack + player (craft grid + result excluded).
- **JEI / EMI** transfer into this container still uses the same dual-source `ICraftingContainer` path.

## Limitations

- The recipe book is embedded in the upgrade tab (not a full `RecipeBookMenu` screen). Keyboard focus for search is forwarded via screen events while the tab is open.
- Multi-result picker from the stock crafting tab is not duplicated here (`setRecipeUsed` still applies after transfer).
- Ghost recipes for uncraftable clicks are client-side; successful placement clears them when items move.

## License

MIT — Alex Krolick

Sophisticated Core / Backpacks remain All Rights Reserved; this addon depends on them and extends public APIs without vendoring their sources.
