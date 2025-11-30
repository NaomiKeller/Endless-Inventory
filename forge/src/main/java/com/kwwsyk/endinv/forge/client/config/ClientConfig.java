package com.kwwsyk.endinv.forge.client.config;

import com.kwwsyk.endinv.common.client.option.IClientConfig;
import com.kwwsyk.endinv.common.client.option.TextureMode;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import com.kwwsyk.endinv.common.options.config.IConfigValue;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.kwwsyk.endinv.common.client.gui.bg.FromResource.*;

public class ClientConfig {

    public static final ClientConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;
    public final ForgeConfigSpec.IntValue ROWS;
    public final ForgeConfigSpec.IntValue COLUMNS;
    public final ForgeConfigSpec.BooleanValue AUTO_SUIT_COLUMN;
    public final ForgeConfigSpec.EnumValue<TextureMode> TEXTURE;
    public final Map<String,ForgeConfigSpec.BooleanValue> PAGE2HIDING = new LinkedHashMap<>();
    public final ForgeConfigSpec.BooleanValue ATTACHING;
    public final ForgeConfigSpec.BooleanValue ENABLE_DEBUG;
    public final ForgeConfigSpec.IntValue MAX_PAGE_BARS;

    private ClientConfig(ForgeConfigSpec.Builder builder){
        ATTACHING = builder.comment("show endless inventory view when opening a menu.")
                .define("attachingMenuScreen",true);

        ROWS = builder.comment("Default rows of EndInv view, 0 for auto.")
                .translation("config.endinv.comment.row1")
                .defineInRange("rows",0,0,Integer.MAX_VALUE);
        COLUMNS = builder.comment("Default columns of EndInv view, 0 for auto.")
                .defineInRange("columns",9,0,Integer.MAX_VALUE);

        AUTO_SUIT_COLUMN = builder.comment("auto suit in columns if GUI Size is too big.")
                .define("auto_suit_column",true);

        TEXTURE = builder
                .comment("Texture mode of EndInv view, transparent or vanilla menu style")
                .comment("FROM_RESOURCE uses vanilla textures, grid is "+CONTAINER_TEXTURE_RESOURCE+", tab is "+TABS_RESOURCE+".")
                .comment("DEDICATED_LOCATION allows using custom texture in resource packs, to use refer such locations: ")
                .comment("grid: "+DEDICATED_CONTAINER_TEXTURE+", tab: "+DEDICATED_TABS+", item_entry: "+ITEM_ENTRY_DISPLAY_RESOURCE)
                .defineEnum("texture_mode",TextureMode.FROM_RESOURCE);

        ENABLE_DEBUG = builder.comment("Press F3 in screen can show some information of menu screen")
                .define("enable_debug",false);

        MAX_PAGE_BARS = builder
                .defineInRange("max_page_bars",10,1,255);

        for (String id : PageTypeRegistry.getIdList()) {
            ForgeConfigSpec.BooleanValue pageEntry = builder
                    .comment("Hide page: " + id)
                    .define("hide_pages." + id, false);
            PAGE2HIDING.put(id,pageEntry);
        }
    }

    static {
        Pair<ClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public final IClientConfig INSTANCE = new IClientConfig() {

        private static IConfigValue<Boolean> convert(ForgeConfigSpec.BooleanValue value){
            return IConfigValue.of(value,value::set);
        }

        private static IConfigValue<Integer> convert(ForgeConfigSpec.IntValue value){
            return IConfigValue.of(value,value::set);
        }

        @Override
        public IConfigValue<Boolean> attaching() {
            return convert(ATTACHING);
        }

        @Override
        public IConfigValue<Integer> rows() {
            return convert(ROWS);
        }

        @Override
        public IConfigValue<Integer> columns() {
            return convert(COLUMNS);
        }

        @Override
        public IConfigValue<Boolean> autoSuitColumn() {
            return convert(AUTO_SUIT_COLUMN);
        }

        @Override
        public IConfigValue<TextureMode> textureMode() {
            return IConfigValue.of(TEXTURE,TEXTURE::set);
        }

        @Override
        public IConfigValue<Boolean> screenDebugging() {
            return convert(ENABLE_DEBUG);
        }

        @Override
        public IConfigValue<Integer> maxPageBarCount(){
            return convert(MAX_PAGE_BARS);
        }

        @Override
        public Set<String> hidingPageIds() {
            return PAGE2HIDING.entrySet().stream()
                    .filter(entry->entry.getValue().get())
                    .map(Map.Entry::getKey).collect(Collectors.toSet());
        }

        @Override
        public void setPageHiding(String id, boolean hiding) {
            Optional.ofNullable(PAGE2HIDING.get(id)).ifPresent(v->v.set(hiding));
            CONFIG_SPEC.save();
        }

        @Override
        public void save() {
            CONFIG_SPEC.save();
        }
    };
}
