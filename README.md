
**Endless Inventory**
=========
![ModIcon](/gallery/icon256.png)  
Endless Inventory is a Minecraft mod providing an RPG like, infinite space inventory.

# Versions

+ In readme/repo
+ + v 1.1.0-SNAPSHOTs-Sep25
+ Forge
+ + 1.20.1 1.1.0-Pre1
+ Fabric
+ + 1.20.1 1.1.0-SNAPSHOT2
+ Neoforge
+ + 1.21.1 1.0.4

*Neoforge-1.21 is the version when I started this project.* 

# Features

## Dedicated Menu
![EIMDemo,compressed](/gallery/EIMDemo.png)
+ Open your endless inventory using a designated item (EndInv Accessor | `endless_inventory:test_endinv`) or press specific key `I`.
### Togglable inner Crafting menu
Click Crafter button to toggle inner crafting menu and
**\[FORGE\]** it supports Jei's recipe transfer.!
#### Click priority
- `shift-click` To crafter>inventory>page
- `ctrl-click` To page>inventory>crafter

## Attached Item Page
![AttachedScreen in FromResource mode](/gallery/AS_FR_DEMO.png)
+ Paginated GUI with category filters and advanced sorting and searching.
+ ctrl-click to quick move items

## Operations
With attached endInv page screen:
+ `I` in gui: open `EndlessInventoryMenu`
+ `ctrl+Left Click` in menu: quick move item from menu to endInv.
+ `A` in page area: add item to bookmark
+ `A` in bookmark page area: remove item from bookmark

## Configurable
client and server config.toml to customize the behavior of EndInv to suit specific needs. See Configuration part.

# Mod Instructions

## Configuration

This mod provides both client and server-side configurations:
+ config/endless_inventory-server.toml: Server behavior and inventory mode.
+ config/endless_inventory-client.toml: UI layout, sorting preferences, and GUI toggles.

### client config

+ Texture mode - Help to change the texture of attached screen
    
    ![Attached Screen in Transparent Mode](/gallery/AS_TP_DEMO.png)
  
    <table>
        <thead>
            <tr>
                <th>Mode</th>
                <th>Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>FromResource</td>
                <td>Vanilla menu background texture clipped</td>
            </tr>
            <tr>
                <td>Transparent</td>
                <td>Render transparent background</td>
            </tr>
            <tr>
                <td>DedicatedLocation</td>
                <td>Allow to define dedicated texture. (See Texture config part)</td>
            </tr>
        </tbody>
    </table>

+ attachingMenuScreen, determine whether to attach endInv with menu screens.
+ rows, of Endless Inventory Screen and Attached Screen
+ columns, of attached screen
+ auto_suit_column, adjust column count if the gui size is too big to contain menu and AS.
+ enable_debug, open screen debug that shows gui info. (see screen debug)
+ max_page_bars, the max count showing side of page.
+ *\[Experimental\]* enable pages

### server config

+ *\[Experimental\]* autoPickUtility, auto send mob and mine loot or picked up dropped item to EndInv. This may cause bugs, typically with other mods.
+ TransferMode, internal item transfer mode between server and client
+ + ALL, transfer all items of an EndInv to client
+ + PART, firstly compute items' filter and sort and send to client, low network pressure but may cause bugs (lack of test).
+ creationMode, how to handle it when there is no EndInv
+ + CREATE_PER_PLAYER, create EndInv for every player who has not an EndInv (no valid UUID).
+ + USE_GLOBAL_SHARED, only create a new EndInv if no valid EndInv exists, players use a shared EndInv.
+ + NONE, do not create a new EndInv, this can be used when you don't like players get EndInv without any conditions.
+ convert_empty_tag: in some situations some item cannot be taken out of Page as the tags may be {} and same item in SourceInv which tag is null.
+ itemCapacity *\[Experimental and lack of test\]*
+ + maxStackSize: max count one item can stack to. Note that all item is same even it is a tool.

Commands
--------
`/endinv`
- `backup` : backup `endless_inventories.dat` file
- `new` : create a new Endless Inventory
- - `public`/`restricted`/`private` : Accessibility of new EndInv default to `public`
- `ofIndex` :
- - `<no arg>` : get player's current endInv index in `levelEndInvs`
- - `<index>`  : get endinv by index in `levelEndInvs`
- - - `open` : open endinv, anyone can run this, but only the accessible players or ops can open.
- - - `setDefault` : set player's default endInv in opening menu.
- - - `setOwner`: set endinv's owner to executor player.
- - - `addWhiteList` : add executor player to endinv's allowlist.
- - - `removeWhiteList` : remove executor player from endinv's allowlist.
- - - `setAccessibility <public/restricted/private>`: set endinv's accessibility.
- - - `remove`: remove current endinv from level endinv data, before this a backup file will be created.
- - - - `<forceRemove>: boolean` : this will force remove endinv though backup failed.

Installation
-------
+ Make sure NeoForge is installed and compatible with your Minecraft version.
+ Download the latest release of the Endless Inventory mod from the Releases page.
+ Place the `.jar` file in your `mods` folder.
+ Launch the game and enjoy your new inventory system!

## DEDICATED_LOCATION
DEDICATED_LOCATION allows using custom texture in resource packs, to use refer such locations:  
In `assets/endless_inventory/textures/gui/*`:
- `item_grid.png` for common pages, derived from chest menu's generic54.png
- `tabs.png` for page switch tabs, derived from achievements gui sprites
- `item_entry.png` for item entry display (enchantment book classify page), derived from generic54 by removing vertical rules to form row strips
- - the process is to fill each row of the grid with a long strip, in FromResource mode, `renderBg` method of `ItemEntryDisplay.class` does it so X).




Developer Guide
======
Prerequisites
----
Java 21 or later

Development Environment
--------
You can import the project into IntelliJ IDEA or Eclipse using the Gradle build script.
//todo need more Env and Toolchain configuration guide

# Miscellaneous Dev Diary

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

Features coming soon
----
- Fix item extract problem (from me and cf comment) and render problem associated with some mod's items.
- Better texture mode and better survival obtain time.