package com.kwwsyk.endinv.fabric.integrates.clothconfig;

import com.kwwsyk.endinv.common.client.option.ClientConfigs;
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
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.endinv.settings"));
        builder.setSavingRunnable(() -> {});

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("endinv.setting.category.general"));
        addGeneralEntries(entryBuilder, general);
        addPageEntries(entryBuilder, builder);

        return builder.build();
    }

    private static void addGeneralEntries(ConfigEntryBuilder entryBuilder, ConfigCategory category) {
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.attaching"), ClientConfigs.DO_ATTACH.get())
                .setDefaultValue(DEFAULT_ATTACHING)
                .setSaveConsumer(value -> ClientConfigs.DO_ATTACH.set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.rows"), ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.Rows.get())
                .setDefaultValue(DEFAULT_ROWS)
                .setMin(0)
                .setTooltip(Component.translatable("config.endinv.comment.row1"))
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.Rows.set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.columns"), ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.Columns.get())
                .setDefaultValue(DEFAULT_COLUMNS)
                .setMin(0)
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.Columns.set(value))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.auto_suit"), ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.autoColumns.get())
                .setDefaultValue(DEFAULT_AUTO_SUIT)
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.autoColumns.set(value))
                .build());

        category.addEntry(entryBuilder.startEnumSelector(Component.translatable("endinv.setting.texture"), TextureMode.class, ClientConfigs.ATTACHED_MENU_CONFIG.TextureMode.get())
                .setDefaultValue(DEFAULT_TEXTURE)
                .setEnumNameProvider(mode -> Component.translatable("endinv.setting.entry." + mode.name()))
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.TextureMode.set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.max_page_bar"), ClientConfigs.ATTACHED_MENU_CONFIG.PageSwitchBar.MaxBars.get())
                .setDefaultValue(DEFAULT_MAX_PAGE_BARS)
                .setMin(1)
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.PageSwitchBar.MaxBars.set(value))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.screen_debug"), ClientConfigs.SCREEN_DEBUG.get())
                .setDefaultValue(DEFAULT_SCREEN_DEBUG)
                .setSaveConsumer(value -> ClientConfigs.SCREEN_DEBUG.set(value))
                .build());
    }

    private static void addPageEntries(ConfigEntryBuilder entryBuilder, ConfigBuilder builder) {
        List<String> pageIds = PageTypeRegistry.getIdList();
        if (pageIds.isEmpty()) {
            return;
        }

        ConfigCategory pages = builder.getOrCreateCategory(Component.translatable("endinv.setting.category.pages"));
        for (String id : pageIds) {
            boolean hidden = ClientConfigs.ATTACHED_MENU_CONFIG.DontDisplayPages.get().contains(id);
            pages.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.hide_page", id), hidden)
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> {
                        List<String> hiddenPages = new java.util.ArrayList<>(ClientConfigs.ATTACHED_MENU_CONFIG.DontDisplayPages.get());
                        if(value && !hiddenPages.contains(id)) hiddenPages.add(id);
                        if(!value) hiddenPages.remove(id);
                        ClientConfigs.ATTACHED_MENU_CONFIG.DontDisplayPages.set(hiddenPages);
                    })
                    .build());
        }
    }
}
