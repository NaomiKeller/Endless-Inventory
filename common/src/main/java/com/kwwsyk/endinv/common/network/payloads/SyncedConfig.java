package com.kwwsyk.endinv.common.network.payloads;

import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.util.SortType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

/**Synced endless inventory config data across server player (attached) data and
 *  client ClientConfig of player.
 *  Used before open menu.
 *
 */
public record SyncedConfig(PageData pageData,boolean attaching,boolean autoPicking) implements ModPacketPayload {

    public static final SyncedConfig DEFAULT = new SyncedConfig(PageData.DEFAULT,true,true);
    public static final Codec<SyncedConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    PageData.CODEC.optionalFieldOf("page_data",PageData.DEFAULT).forGetter(SyncedConfig::pageData),
                    Codec.BOOL.optionalFieldOf("attaching",true).forGetter(SyncedConfig::attaching),
                    Codec.BOOL.optionalFieldOf("auto_pickup",true).forGetter(SyncedConfig::autoPicking)
            ).apply(instance, SyncedConfig::new)
    );

    public static void encode(SyncedConfig config, FriendlyByteBuf o){
        PageData.encode(o, config.pageData);
        o.writeBoolean(config.attaching);
        o.writeBoolean(config.autoPicking);
    }

    public static SyncedConfig decode(FriendlyByteBuf o){
        return new SyncedConfig(PageData.decode(o),
                o.readBoolean(),
                o.readBoolean());
    }

    /**
     * Used when player is not viewing EndInv.
     * e.g. player joined world or player opened menu screen with EndInv attaching allowed.
     */

    /**
     * Used when player changed page param in client page.
     * @param config new config
     */

    /**
     * Read client config to update player's config.
     * @param ofMenu if is in {@link com.kwwsyk.endinv.common.menu.EndlessInventoryMenu}
     * @return Updated local player's config.
     */


    @Override
    public String id() {
        return "endinv_settings";
    }

    public void handle(ModPacketContext context){
        if(context.player()!=null) {
            ModRegistries.NbtAttachments.getSyncedConfig().setTo(context.player(), this);
        }
    }

    public SyncedConfig searchingChanged(String searching) {
        return new SyncedConfig(pageData.searchingChanged(searching),attaching,autoPicking);
    }

    public SyncedConfig sortTypeChanged(SortType type) {
        return new SyncedConfig(pageData.sortTypeChanged(type),attaching,autoPicking);
    }

    public SyncedConfig ofReverseSort() {
        return new SyncedConfig(pageData.ofReverseSort(),attaching,autoPicking);
    }

    public SyncedConfig ofRowChanged(int rows) {
        return new SyncedConfig(pageData.ofRowChanged(rows),attaching,autoPicking);
    }

    public SyncedConfig pageKeyChanged(String regKey) {
        return new SyncedConfig(pageData.ofPageKeyChanged(regKey),attaching,autoPicking);
    }

    /**
     * Though ClientConfig approves 0 valued row/col count for auto adjustment,
     *  SyncedConfig represents real row/col count involved in both client and server logics.
     * @return valid SyncedConfig state with positive row/col count.
     */
    public boolean checkState(){
        return pageData.rows()>0 && pageData.columns()>0;
    }

    public boolean checkForAttaching(){
        return checkState() && attaching;
    }
}