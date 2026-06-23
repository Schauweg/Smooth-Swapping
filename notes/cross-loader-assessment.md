# Cross-loader container compatibility assessment

Date: 2026-06-23
Branch: modded-container-compat

## Smooth Swapping platform structure

Smooth Swapping 26.1 currently includes `common`, `fabric`, and `neoforge` modules. The root
`gradle.properties` has `enabled_platforms=fabric,neoforge`, and there is no `forge` subproject in
this checkout. `gradlew tasks --all` exposes `:fabric:*` and `:neoforge:*` build tasks, but no
`:forge:*` tasks.

The modded container compatibility layer lives in `common/src/main/java/dev/shwg/smoothswapping/compat`.
The changed mixins also live in `common/src/main/java/dev/shwg/smoothswapping/mixin`.

Both loaders use the same common mixin config:

- Fabric: `fabric/src/main/resources/fabric.mod.json` references `smoothswapping-common.mixins.json`.
- NeoForge: `neoforge/src/main/resources/META-INF/neoforge.mods.toml` references
  `smoothswapping-common.mixins.json`.

The common compatibility code does not reference Fabric Loader, Fabric API, NeoForge API, or target
mod classes directly. Target mod detection is string-based, so missing optional mods should not cause
class loading failures.

## Target mod references

### Macaw's Furniture

- Modrinth project: `dtWC90iB`
- Mod id: `mcwfurnitures`
- Fabric reference used earlier: `mcw-furniture-3.4.2-mc26.1fabric.jar`
- NeoForge official file: `mcw-furniture-3.4.2-mc26.1neoforge.jar`
  - Game versions: `26.1`, `26.1.1`, `26.1.2`
  - SHA1 verified: `284997aa5653c4179e5fbe08812a958dfcb5d652`
- Forge official file: `mcw-furniture-3.4.2-mc26.1forge.jar`
  - Game versions: `26.1`, `26.1.1`, `26.1.2`
  - SHA1 verified: `76dca0e8df94f1b0fd0e67decd3f96e012dbd66d`

Class names:

- Fabric screen: `net.kikoz.mcwfurnitures.storage.FurnitureScreen`
- Fabric menu: `net.kikoz.mcwfurnitures.storage.FurnitureScreenHandler`
- NeoForge/Forge screen: `com.mcwfurnitures.kikoz.storage.FurnitureStorageScreeen`
- NeoForge/Forge menu: `com.mcwfurnitures.kikoz.storage.FurnitureStorageContainer`

Assessment:

- The NeoForge/Forge class names differ from Fabric.
- `MacawsFurnitureAdapter` now includes both the Fabric class pair and the NeoForge/Forge class pair.
- The adapter still uses vanilla-like slot behavior and has no hard dependency on Macaw's Furniture.

### MrCrayfish's Refurbished Furniture

- Official source: `https://github.com/MrCrayfish/MrCrayfishFurnitureMod-Refurbished.git`
- Local reference: `refs/MrCrayfishFurnitureMod-Refurbished`
- Branch: `26.1.2`
- Commit: `192af84c84b5cac1b06613d69609a4ddfc1bf15f`
- Mod id: `refurbished_furniture`
- Minecraft version: `26.1.2`
- Local source includes Fabric and NeoForge modules.

Relevant classes:

- Common screens include:
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.ComputerScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.DoorMatScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.ElectricityContainerScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.ElectricityGeneratorScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.PostBoxScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.RecyclingBinScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.WorkbenchScreen`
- NeoForge-only cooking screens:
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.FreezerScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.MicrowaveScreen`
  - `com.mrcrayfish.furniture.refurbished.client.gui.screen.StoveScreen`
- Fabric-only cooking screens:
  - `com.mrcrayfish.furniture.refurbished.client.screen.FabricFreezerScreen`
  - `com.mrcrayfish.furniture.refurbished.client.screen.FabricMicrowaveScreen`
  - `com.mrcrayfish.furniture.refurbished.client.screen.FabricStoveScreen`
- Menus include:
  - `com.mrcrayfish.furniture.refurbished.inventory.SimpleContainerMenu`
  - `com.mrcrayfish.furniture.refurbished.inventory.SimpleRecipeContainerMenu`
  - `com.mrcrayfish.furniture.refurbished.inventory.FreezerMenu`
  - `com.mrcrayfish.furniture.refurbished.inventory.MicrowaveMenu`
  - `com.mrcrayfish.furniture.refurbished.inventory.StoveMenu`
  - `com.mrcrayfish.furniture.refurbished.inventory.WorkbenchMenu`

Assessment:

- These screens and menus follow vanilla-like `AbstractContainerScreen` / `AbstractContainerMenu`
  patterns.
- Slots are ordinary `Slot` subclasses such as `FuelSlot`, `ResultSlot`, and `PostBoxSlot`.
- No dedicated adapter is currently required; the default `VanillaLikeScreenAdapter` is expected to
  cover these screens on Fabric and NeoForge.
- User testing already confirmed the Fabric path.

### Traveler's Backpack

- Official source: `https://github.com/Tiviacz1337/Travelers-Backpack.git`
- Fabric local reference: `refs/Travelers-Backpack`
  - Branch: `26.1-fabric`
  - Commit: `8dfacd7e2b2869d4a0053701bc80466f92323420`
- NeoForge local reference: `refs/Travelers-Backpack-NeoForge`
  - Branch: `26.1-neoforge`
  - Commit: `bd578dff2fed5c6b0f6da6664ab1f5335de8b328`
- Modrinth project: `rlloIFEV`
- Mod id: `travelersbackpack`
- NeoForge official file: `travelersbackpack-neoforge-26.1.2-11.2.4.jar`
  - Game version: `26.1.2`
  - SHA1: `d58bbc15bea310ea45cf7ff58edd23a7075b2136`
- No Modrinth Forge file was found for game version `26.1.2`.

NeoForge class names checked:

- Screen: `com.tiviacz.travelersbackpack.client.screens.BackpackScreen`
- Menus:
  - `com.tiviacz.travelersbackpack.inventory.menu.BackpackItemMenu`
  - `com.tiviacz.travelersbackpack.inventory.menu.BackpackBlockEntityMenu`
- Main storage slot:
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.BackpackSlotItemHandler`
- Special slots:
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.ToolSlotItemHandler`
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeSlotItemHandler`
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.UpgradeLockableSlotItemHandler`
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.FluidSlotItemHandler`
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.FilterSlotItemHandler`
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.DisabledSlot`
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.CraftingSlot`
  - `com.tiviacz.travelersbackpack.inventory.menu.slot.ResultSlotExt`

Assessment:

- The NeoForge branch keeps the same screen/menu/main storage slot class names used by the Fabric
  adapter.
- `TravelersBackpackAdapter` intentionally animates only visible main backpack storage slots and
  vanilla player inventory slots.
- Tool slots, upgrade slots, lockable upgrade slots, fluid slots, filter/fake slots, disabled slots,
  and upgrade crafting/result slots are not treated as real item slots by the adapter.
- The adapter also limits animations to a recent normal container click and at most 12 changed slots,
  so bulk actions are conservative.

## Forge conclusion

The current Smooth Swapping 26.1 checkout cannot produce a Forge jar because there is no `forge`
module and `enabled_platforms` does not include Forge. Macaw's Furniture has an official Forge
26.1.2 jar, but Traveler's Backpack does not have a Modrinth Forge file for 26.1.2, and Smooth
Swapping itself has no Forge build task here. Adding a new Forge platform module would be a separate
porting task and was not done in this compatibility pass.

## NeoForge conclusion

NeoForge should theoretically reuse the Fabric-tested compatibility logic because:

- the implementation is in `common`;
- NeoForge loads `smoothswapping-common.mixins.json`;
- the mixin targets are Minecraft client/container classes shared by the two enabled platforms;
- no Fabric-only APIs are referenced from common code;
- Macaw's NeoForge class names are now covered;
- Traveler's Backpack NeoForge class names match the existing adapter;
- Refurbished Furniture uses vanilla-like screens and menus covered by the default adapter.

Manual NeoForge testing is still needed because slot mutation timing and target mod loader code can
differ at runtime even when class names and inheritance are aligned.

## Recommended NeoForge manual test order

1. Start NeoForge with only Smooth Swapping and required loader dependencies.
2. Test vanilla inventory, chest, barrel, shulker box, hopper, furnace, and crafting table.
3. Install Macaw's Furniture and test storage furniture normal click, shift-click, and player
   inventory transfer.
4. Install Refurbished Furniture and test storage/crafting furniture containers with normal click,
   shift-click, and player inventory transfer.
5. Install Traveler's Backpack and test opening the main backpack, main storage normal click,
   shift-click, and transfer between player inventory and backpack storage.
6. For Traveler's Backpack, verify that tool slots, upgrade slots, fluid slots, filter slots, locked
   slots, memory slot UI, and disabled backpack item slots do not produce incorrect animations.
7. Traveler's Backpack sort, quick stack, transfer, and other bulk buttons remain conservative:
   animation is not guaranteed, but they should not crash or create obviously wrong animations.
