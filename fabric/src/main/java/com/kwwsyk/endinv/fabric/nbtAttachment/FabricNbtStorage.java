package com.kwwsyk.endinv.fabric.nbtAttachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class FabricNbtStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricNbtStorage.class);

    public static UUID getUuid(Player player){
        var tag = player.getPersistentData();
        if(tag==null) return null;
        if(!tag.contains("endinv_uuid")) return null;
        try{
            return UUID.fromString(tag.getString("endinv_uuid"));
        }catch(Exception e){
            LOGGER.error("Failed parse uuid from player tag",e);
            return null;
        }
    }

    public static void setUuid(Player player, UUID uuid){
        var tag = player.getPersistentData();
        if(tag==null) return;
        var c = new CompoundTag();
        c.putString("endinv_uuid",uuid.toString());
        tag.put("endinv_uuid",c);
    }

}
