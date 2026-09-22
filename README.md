# SB Advanced Crafting

NeoForge 1.21.1 addon for [Sophisticated Backpacks](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks) that adds an **Advanced Crafting Upgrade** with an in-GUI **recipe book browser**.

## Features

- New upgrade item: **Advanced Crafting Upgrade** (separate from stock `crafting_upgrade`)
- Opens a crafting tab with a searchable, paginated recipe browser
- Click a recipe to place ingredients into the 3×3 grid
- **Shift-click** (or right-click) places as many as possible (max transfer)
- Ingredient pull uses Sophisticated Core’s dual-source transfer: **backpack storage + player inventory**
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

Jar: `build/libs/sb_advanced_crafting-1.0.0.jar`

## Recipe book implementation

Vanilla `RecipeBookComponent` expects a `RecipeBookMenu` and fights upgrade-tab coordinates on `StorageScreenBase`. This mod implements a **book-like recipe browser** inside the advanced crafting tab:

1. Lists all `RecipeType.CRAFTING` recipes (search + pagination)
2. On click, sends custom payload `sb_advanced_crafting:place_crafting_recipe`
3. Server expands the recipe to a 9-slot template and calls `CraftingContainerRecipeTransferHandlerServer.setItemsWithStacks` (same path JEI/EMI use)

Limitations:

- Not the vanilla animated recipe book widget (appearance is a compact grid browser)
- “Can craft” highlighting of missing ingredients is not yet shown (placement still only moves available items)
- Multi-result picker from stock crafting tab is not duplicated here (first matching result / `setRecipeUsed` still applies after transfer)

## License

MIT — Alex Krolick

Sophisticated Core / Backpacks remain All Rights Reserved; this addon depends on them and extends public APIs without vendoring their sources.
