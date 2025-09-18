# Change Log
__branch 1.20.1__

## 2025, before Sept: *A Brief*

First release was published in May 2025, supports Neoforge 1.21.1. 
The version support forge 1.20.1 was released in Aug 15.  

## Sept 2025

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