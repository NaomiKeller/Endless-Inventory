package com.kwwsyk.endinv.fabric.integrates.clothconfig;

import com.kwwsyk.endinv.common.client.ClientModInfo;
import com.kwwsyk.endinv.common.client.option.IClientConfig;
import com.kwwsyk.endinv.common.client.option.TextureMode;
import com.kwwsyk.endinv.common.menu.page.PageTypeRegistry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

final class ClothConfigScreenBuilder {

    private static final boolean DEFAULT_ATTACHING = true;
    private static final int DEFAULT_ROWS = 0;
    private static final int DEFAULT_COLUMNS = 9;
    private static final boolean DEFAULT_AUTO_SUIT = true;
    private static final TextureMode DEFAULT_TEXTURE = TextureMode.FROM_RESOURCE;
    private static final boolean DEFAULT_SCREEN_DEBUG = false;
    private static final int DEFAULT_MAX_PAGE_BARS = 10;

    private ClothConfigScreenBuilder() {
    }

    static Screen create(Screen parent) {
        IClientConfig config = ClientModInfo.getClientConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.endinv.settings"));
        builder.setSavingRunnable(config::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("endinv.setting.category.general"));
        addGeneralEntries(entryBuilder, general, config);
        addPageEntries(entryBuilder, builder, config);

        return builder.build();
    }

    private static void addGeneralEntries(ConfigEntryBuilder entryBuilder, ConfigCategory category, IClientConfig config) {
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.attaching"), config.attaching().get())
                .setDefaultValue(DEFAULT_ATTACHING)
                .setTooltip(Component.translatable("config.endinv.comment.attaching"))
                .setSaveConsumer(value -> config.attaching().set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.rows"), config.attachedRows().get())
                .setDefaultValue(DEFAULT_ROWS)
                .setMin(0)
                .setTooltip(Component.translatable("config.endinv.comment.row1"))
                .setSaveConsumer(value -> config.attachedRows().set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.columns"), config.attachedColumns().get())
                .setDefaultValue(DEFAULT_COLUMNS)
                .setMin(0)
                .setSaveConsumer(value -> config.attachedColumns().set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.menu_rows"), config.menuRows().get())
                .setDefaultValue(DEFAULT_ROWS)
                .setMin(0)
                .setTooltip(Component.translatable("config.endinv.comment.row1"))
                .setSaveConsumer(value -> config.menuRows().set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.menu_columns"), config.menuColumns().get())
                .setDefaultValue(DEFAULT_COLUMNS)
                .setMin(0)
                .setSaveConsumer(value -> config.menuColumns().set(value))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.auto_suit"), config.autoSuitColumn().get())
                .setDefaultValue(DEFAULT_AUTO_SUIT)
                .setTooltip(Component.translatable("config.endinv.comment.auto_suit"))
                .setSaveConsumer(value -> config.autoSuitColumn().set(value))
                .build());

        category.addEntry(entryBuilder.startEnumSelector(Component.translatable("endinv.setting.texture"), TextureMode.class, config.textureMode().get())
                .setDefaultValue(DEFAULT_TEXTURE)
                .setEnumNameProvider(mode -> Component.translatable("endinv.setting.entry." + mode.name()))
                .setTooltip(Component.translatable("config.endinv.comment.texture"))
                .setSaveConsumer(value -> config.textureMode().set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.max_page_bar"), config.maxPageBarCount().get())
                .setDefaultValue(DEFAULT_MAX_PAGE_BARS)
                .setMin(1)
                .setTooltip(Component.translatable("config.endinv.comment.max_page_bar"))
                .setSaveConsumer(value -> config.maxPageBarCount().set(value))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.screen_debug"), config.screenDebugging().get())
                .setDefaultValue(DEFAULT_SCREEN_DEBUG)
                .setTooltip(Component.translatable("config.endinv.comment.screen_debug"))
                .setSaveConsumer(value -> config.screenDebugging().set(value))
                .build());
    }

    private static void addPageEntries(ConfigEntryBuilder entryBuilder, ConfigBuilder builder, IClientConfig config) {
        List<String> pageIds = PageTypeRegistry.getIdList();
        if (pageIds.isEmpty()) {
            return;
        }

        ConfigCategory pages = builder.getOrCreateCategory(Component.translatable("endinv.setting.category.pages"));
        for (String id : pageIds) {
            boolean hidden = config.isPageHidden(id);
            pages.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.hide_page", Component.translatable("page.endinv." + id)), hidden)
                    .setDefaultValue(id.equals("mod_items"))
                    .setSaveConsumer(value -> config.setPageHiding(id, value))
                    .build());
        }
    }
}
