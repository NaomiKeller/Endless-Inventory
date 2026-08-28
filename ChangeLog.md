# Change Log
__branch 1.20.1__

## 2025, before Sept: *A Brief*

First release was published in May 2025, supports Neoforge 1.21.1. 
The version support forge 1.20.1 was released in Aug 15.  

# Sept 2025

## 9.9
Switch Forge dev tool from 'legacyforge'(by neoforge group) to 'forgeGradle'(the original forge) as an issue existed in legacyforge.  

**SLICE FROM README**
#### forge 1.20.1 runServer problem:
'error loading client class `LanServerPinger`'  
It seems like forge (or legacyforge) internal problem that it *marks* class LanServerPinger
with `@OnlyIn(Dist.CLIENT)` but when running dedicated server, the class will be loaded to ping the server
to lan and a `RuntimeException` will be thrown.  
Normally, the class will not be marked with that annotation: neoforge notates it
"//neo: mark this class as server loadable...(the brief meaning)", and
forge(forgegradle instead of legacyforge) just does not mark this annotation.
###### Solution
Config forge's server config `forge-server.toml`, possibly located in
`run/world/serverconfig`.  
Set `advertiseDedicatedServerToLan` to `false`. 

## 9.17
Fixed a bug where certain items (`tacz:workbench_b`) could not be taken out.   
This bug was caused by the specific block item `tacz:workbench_b` in TACZ, whose NBT could under certain conditions be assigned an empty value (`CompoundTag tag = null -> tag = {}`).  
The issue was also related to JEI’s refresh behavior, which made it hard to detect.

### Technical:
Used remote debugging (by adding JVM parameters `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005` in the launcher and connecting with IntelliJ IDEA’s remote debug).  
Also used a Mixin to monitor item NBT operations.  

### Fix
Add a configuration: `Convert Empty Tag` and apply it in `SourceInventory #takeItem & #addItem`. Let item with `tag={}` server as `tag=null` 


## 9.19
- Refactored SyncedConfig to only carry attaching/auto-pick flags and moved page layout storage into local client config, with servers updating via ItemPageContext.
- Updated EndlessInventory menu and client UI to bootstrap from cached PageData so screens open without extra server round-trips.
- Expanded Fabric client config/network hooks to persist sort/search preferences and keep cached flags in sync when packets arrive.
## 9.18
- Texture: add `DEDICATED_LOCATION` mode and optimize related loading/performance
- Creative Mode: fix item taking logic to handle empty NBT tags and improve reliability

### DEDICATED_LOCATION
DEDICATED_LOCATION allows using custom texture in resource packs, to use refer such locations:  
In `assets/endless_inventory/textures/gui/*`:
- `item_grid.png` for common pages, derived from chest menu's generic54.png
- `tabs.png` for page switch tabs, derived from achievements gui sprites
- `item_entry.png` for item entry display (enchantment book classify page), derived from generic54 by removing vertical rules to form row strips  
- - the process is to fill each row of the grid with a long strip, in FromResource mode, `renderBg` method of `ItemEntryDisplay.class` does it so X).

## 9.25
- Fabric: ported screen attachment lifecycle, client input handlers, and debug overlays to Fabric API events, and refreshed `fabric.mod.json` metadata for the loader entry points.

## 9.26
- Fabric: finished port by wiring client events, networking handlers, and mixins to the updated Fabric API so the module now compiles cleanly.
- Fabric: added accessor/mixin shims for screen widgets and player persistent data so config toggles persist without Forge APIs.
- Fabric: implemented reflection fallback for loot event hooks and centralized NBT helpers shared with the common module.

## 9.21 1.1.0-SNAPSHOT-2
- Added a crafter menu into Endless Inventory Menu which is for Jei's recipe handler.

### after release
- Rearranged the JAR filename so the loader name (Forge/Fabric) is prefixed for easier version identification.

## 9.23 1.1.0-Pre1
- \[Forge] Implemented EndlessInventory's JEI recipe transfer.
- \[MAYBE] Updated readme

## 9.26 1.1.0-Pre2
Changed core logic of DisplayPage,ItemPage and their subclasses and some payloads to let pages item modify and some other logic clearer and well-organized.   
* Coincidentally, the bug of SegClassifyItemDisplay's items un-seg-geg bug is incidentally fixed.

## 9.27 1.1.0-Pre2
\[Forge] Added curios mod integration: Curio classify page.

## 10.8 1.1.0-Pre3
Fixed:
- Update is not instant when SortType and search is switched.
- Fabric: launch and attack

todo: check every feature and utilities to reduce bugs; jei's mixined recipe transfer
fabric: search; equipment page's arrangement
forge: quick move item in survival mode will not sync to server, fabric has no such problem

# 10.12 1.1.0-Pre3
Dev aspect
- SyncedConfig#attaching only presents player's client attaching config.
- AttachingManager -> AttachingMonitor and I marked it `reserved`.

Bug: quick-move creative item picker's items seems cannot sync to server.
    Seems not? But once after jei transfer, creative added items vanished...

# Develop note 10.12 1.1.0-Pre3
There are no attachability check for EndInv operations in packet handle. So if some bug I left exists or who cheats, 
player can interact with EndInv without depending on EIM even server's attaching config let the player's menu not attachable.

If I do not make mistakes, `OpenEndInvPayload (server)` and `ScreenAttachment (forge/fabric | client)`
are the only classes who check attachability.

# 10.13
Forge: Added icon to curios page

Forge: Added jei mouse click handler, so press R,U... can apply on Page's items.
todo: fabric

Ensure Endless Inventory updates always advance the last modified timestamp so quick successive edits keep their ordering.

# 10.17
Fabric: fix search

# 1.1.0 Pre-3 Changelog
Forge:
- Fixed several bugs
- Added "curios page" for Curios
- Added config screen, cloth config api is a selectable dependency now
- Experimental: Use mixin on Jei's recipe transfer handler to let Jei import ingredients from Attaching Screen in any menu.
- - To open it: config startup config in `endless_inventory-common.toml`
- - This feature depends on Jei's (`15.20.0.112`) non-api codes, this means it's not stable.

Fabric:
- Fixed several bugs
- Added config screen, cloth config api is a selectable dependency now
\
# 10.17 Fabric autopick & metadata
- Fabric: removed reflection-based loot registration; use mixins and Fabric API where applicable to restore autopick.
- Fabric: block break drops are absorbed via Block.popResource/popExperience mixins; no entities when fully absorbed; XP repairs Mending gear then grants remainder.
- Fabric: mob death drops are absorbed by intercepting LivingEntity.dropFromLootTable and Entity.spawnAtLocation; XP handled via ExperienceOrb.award.
- Fabric: added server/client networking init split to avoid unregistered payloads; ensured clientbound/serverbound encoders registered on respective sides.
- Metadata: mods metadata updated to suggest optional Cloth Config on Fabric and optional cloth_config on Forge; relaxed Fabric loader and MC version ranges.

# 10.18 Fabric autopick fix
- Fabric: fix BlockDropMixin null breaker by capturing and clearing the breaker around Block.playerDestroy; ThreadLocal now set precisely during drops/XP awarding to restore reliable auto-pick.
- Forge: Optimistic autopick mechanic: fixed bug in picking skulker_box.

# 10.18 1.1.0 Release

Endless Inventory 1.1.0 is a feature-packed update for 1.20.1 (Forge/Fabric), with stability fixes and quality-of-life improvements. Highlights below are curated for CurseForge/Modrinth.

What’s new
- Attached Item Page overhaul: faster open from cached data, improved sorting, searching, and category browsing; bookmark support and better quick-move behavior.
- Crafter inside Endless Inventory Menu: toggle an embedded crafting grid, supports JEI recipe transfer into the crafter.
- Curios integration (Forge): a dedicated Curios page for quick access and management.
- Config screens: optional Cloth Config UI on both Forge and Fabric to tweak layout, textures, and behavior.
- Texture “DedicatedLocation” mode: ship or resource-pack custom skins for the attached UI (item grid, tabs, entry rows) without editing code.

Fabric parity and autopick
- Fabric port completed for 1.20.1 with feature parity to Forge where applicable.
- Auto-pick utility restored and hardened on Fabric: block and mob drops go straight to EndInv; XP first repairs Mending, then awards the remainder.
- Fixed a critical null-breaker bug in Fabric autopick by scoping the breaker during Block.playerDestroy, ensuring reliable capture through popResource/popExperience.

Fixes and polish
- Consistent search box input on Fabric; character typing bugs resolved.
- Corrected quick-move behaviors and slot validation; fewer desyncs and edge-case fails.
- Empty NBT tag handling: added “Convert Empty Tag” option to treat {} as null, fixing items that couldn’t be taken from pages in specific mods.
- Stable “last modified” ordering: enforced monotonic timestamps so recent changes sort correctly.
- Numerous UI and attachment lifecycle refinements for smoother screen overlays and inputs.

Compatibility and metadata
- Loaders: Forge 1.20.1 and Fabric 1.20.1.
- Optional integrations: Cloth Config (both loaders), JEI (Forge). Integration paths are guarded when mods are absent.
- Pack authors: refreshed mod metadata and texture hooks for resource-pack friendly theming.

Notes
- Auto-pick is configurable on the server and is still experimental in heavily modded stacks. Disable if another loot-capture mod conflicts.
- JEI recipe transfer mixin on Forge uses non-API internals; treat as experimental and report incompatibilities.

Thanks for testing the pre-releases and reporting issues — your feedback directly shaped this update. Enjoy the inventory freedom!

## 2026.8.27 Session recap

Two kinds of work this session: **Fixes** repair things that were broken regardless of taste — worth eventually upstreaming to the original mod. **Features & UI** are personal layout/organization changes — a matter of preference, not correctness.

### Fixes

- **Favorited items you're out of now show up** — Favoriting an item and then using up your entire stock used to make the icon vanish from Bookmarks instead of staying visible with a red "0".
  <details><summary>Technical note</summary>The network message carrying favorited items piggybacked on a Minecraft function that silently drops item data when the count is zero. Now sent as two separate pieces of data so identity survives at zero count.</details>
- **Un-favoriting and hovering fixed for those same items** — Once an out-of-stock item disappeared from Bookmarks, un-favoriting or hovering it for a tooltip silently did nothing. Fixed alongside the display issue above.
- **Scrolling to the bottom of a long list no longer goes blank** — Scrolling past the last item used to overshoot into an empty page; it now stops exactly at the final row.
- **Reverse sort no longer leaves blank slots when scrolling** — With reverse sort on, scrolling a long list (especially sorted by count) could leave the tail end blank. Sort order and scroll position are now combined correctly.
- **Favoriting a large stack (over 64) now actually saves** — Favoriting a stack bigger than a normal Minecraft stack (e.g. 300 iron ingots) could silently fail with nothing added to Bookmarks.
  <details><summary>Technical note</summary>Same "count gets corrupted over the network" bug as the favorites issue above, but in the star-item message. Minecraft compressed the count into a single byte, so anything over 127 wrapped around and got rejected.</details>
- **Fixed a scrolling crash on smaller pages** — Scrolling on a page with fewer items than the whole inventory (Gear, Stone, Consumables, etc.) could crash the game outright. Confirmed fixed by attempting to reproduce it and failing to.
- **Fixed a real performance bug** — Every item icon drawn on screen was making a full copy of the entire inventory's data just to look up one item, dozens of times a frame. Now a cheap direct lookup.
- **Hiding/showing pages in Settings works immediately** — Toggling a page on/off used to require a full game restart to take effect; now applies as soon as the settings screen closes and the inventory reopens.

### Features & UI

- **Weapons, Tools, and Armor merged into one "Gear" tab** — Visually grouped into sections (weapons, tools, each armor piece) with dividers, instead of three separate tabs.
- **Four new pages** — **Stone**, **Wood**, and **Ores & Minerals** group building/mining materials by type; **Mod Items** collects everything added by other mods separately from vanilla content.
- **"Block Items" now means actual solid blocks** — No longer includes glass panes, leaves, slabs, carpets, etc.; limited to solid, full-size cubes (stone, wood, wool, concrete, ...).
- **Separate row/column sizing for each view** — The main inventory screen and the version attached to chests/other menus can now each have their own row/column count instead of sharing one setting.
- **Reworked layout for both views** — Search bar and settings button moved to a consistent spot at the bottom of both views. The Crafter toggle moved from floating above the panel to inside it. Page tabs repositioned and spaced out so they no longer overlap the settings button.
- **Simpler "selected tab" look** — The open tab now uses a brightness highlight instead of popping outward, so it looks right regardless of which side the tabs are on.

*Fabric build 1.20.1-1.1.0.1 — built and tested against the Mizuno modpack instance.*

## 2026.8.28 Session recap

### Fixes

- **Button icons no longer turn into garbled text after some rebuilds** — `common/build.gradle` didn't force UTF-8 like the other subprojects, so whenever `common` recompiled under a different default system encoding, the unicode glyphs used for the settings/sort/scroll-arrow buttons (▲ ▼ ⚙ ⇅) got mangled into mojibake baked right into the compiled class.
- **Attached-view tab-scroll arrows now match the tab width** — they were 1px wider than the tabs they scroll.
- **Reverse-sort button keeps its background in the attached view** — its vanilla bevel was getting painted over by the grid background, which renders after the attached view's widgets instead of before (the standalone menu doesn't have this ordering quirk).
- **Search field now updates results while typing, not just on backspace** — the attached view's typed characters go through the host screen's own text handling rather than this mod's own key handler, so nothing was triggering a refresh until some other key (like backspace) happened to go through a different, more reliable path. Now polled once per frame regardless of which input path changed the text.
- **Fixed a doubled/thickened border line on the attached view's grid** — both the bottom cap and the right-side cap (for any column count other than 9) were resampling a texture pixel the grid body had already drawn on its own, doubling a highlight line into a visibly thicker seam.
- **Fixed a 1px tab-column misalignment specific to exactly 9 columns** — the dedicated 9-column vanilla texture is 1px narrower than the generic width formula assumed, nudging the tab column and scroll arrows 1px right of the box's real edge.
- **Page tab name tooltip no longer renders behind the item grid** — it was drawn before the grid's icons/counts instead of after.
- **Settings screen no longer shows raw page ids** — the page-hiding list showed things like `bookmark` and `block_items` instead of their translated names.

### Features & UI

- **Page renames for consistency**: Block Items → Blocks, Food&Potion → Consumables, Bookmark → Favorites, All items → All Items.
- **Mod Items page is hidden by default** on a fresh install, since an empty page (no other mods yet) reads as broken rather than just unpopulated.
- **Search field placeholder text** ("Search...") added to both views.
- **Sort dropdown overhaul**: entries relabeled (Default, Amount, Name, ID, Recents) instead of raw enum names; "Amount" now defaults to largest stack first and "Recents" defaults to most-recently-touched first (both still flip via Reverse Sort).
- **General settings tab reworded throughout** for clarity — e.g. "Auto suit in columns" → "Shrink Columns to Fit Window", "Attaching menu screen" → "Show Attached View", "Texture mode" → "Texture Style" — and added tooltips that were missing entirely.
- **Minor spacing/alignment polish**: search field width, and positioning of the sort dropdown, reverse-sort button, and crafter toggle on both views.

*Fabric/Forge build 1.20.1-1.3.0-naomi — built and tested against the Mizuno modpack instance.*

## 2026.8.28 Performance pass

Follow-up to the session above after a player reported the "All Items" page (and any page that keeps most of the grid filled) costing noticeably more FPS than a small page like Favorites, with the drop scaling with how much of the visible grid was filled - traced down to four separate hot-path issues, none of them related to other mods.

### Fixes

- **Sorting the inventory no longer copies the whole item map** — `getSortedKeyReference()` deep-copied the entire inventory into a new map on every call (every scroll notch, keystroke, and click), regardless of sort type. Now reads the live map directly, same fix already applied elsewhere.
- **The sorted item list is now cached** — sorting doesn't depend on which page is open or what's typed in search, so redoing the full O(n log n) sort from scratch on every scroll/keystroke was wasted work. Cached until the inventory's content or the active sort type actually changes.
- **The server no longer resends your entire inventory on every scroll/click/keystroke** — with the default "send everything" transfer mode, every one of those interactions triggered a fresh full-inventory copy, network packet, and resync, even when nothing had changed since the last one. Now skipped unless the content actually changed; still forces a correct resync on reconnect or reopening the screen, so nothing goes stale.
- **Item icons no longer get rebuilt from scratch every single frame** — this was the largest remaining cost. Displaying an item slot rebuilt a brand new `ItemStack` on every rendered frame, regardless of whether anything changed, and this ran once per *visible* slot every frame whether or not you were interacting with the page at all. Pages that keep most slots filled (All Items) paid this cost constantly; pages with mostly empty slots (Favorites) barely noticed since empty slots have nothing to rebuild.

### Note

What's left after these fixes is Minecraft's own inherent cost of drawing many distinct item icons per frame, which scales with how many *visible* slots are filled - not a bug, just physics. A smaller row/column count directly reduces it if you want the extra margin. Reported drops that felt much larger than that (e.g. sudden 40-50fps cliffs) turned out to be a VSync frame-pacing artifact unrelated to the mod, not the rendering cost itself.

*Fabric/Forge build 1.20.1-1.3.1-naomi — built and tested against the Mizuno modpack instance.*
