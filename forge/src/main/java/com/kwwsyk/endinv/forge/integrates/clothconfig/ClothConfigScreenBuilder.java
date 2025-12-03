package com.kwwsyk.endinv.forge.integrates.clothconfig;

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

    private ClothConfigScreenBuilder() {
    }

    static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("title.endinv.settings"));
        // saver is bound per entry

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("endinv.setting.category.general"));
        addGeneralEntries(entryBuilder, general);

        addPageEntries(entryBuilder, builder);

        return builder.build();
    }

    private static void addGeneralEntries(ConfigEntryBuilder entryBuilder, ConfigCategory category) {
        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.attaching"), ClientConfigs.DO_ATTACH.get())
                .setDefaultValue(true)
                .setSaveConsumer(value -> ClientConfigs.DO_ATTACH.set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.rows"), ClientConfigs.EIM_CONFIG.Rows.get())
                .setDefaultValue(0)
                .setMin(0)
                .setTooltip(Component.translatable("config.endinv.comment.row1"))
                .setSaveConsumer(value -> ClientConfigs.EIM_CONFIG.Rows.set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.columns"), ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.Columns.get())
                .setDefaultValue(9)
                .setMin(0)
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.Columns.set(value))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.auto_suit"), ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.autoColumns.get())
                .setDefaultValue(true)
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.PageBasicLayout.autoColumns.set(value))
                .build());

        category.addEntry(entryBuilder.startEnumSelector(Component.translatable("endinv.setting.texture"), TextureMode.class, ClientConfigs.ATTACHED_MENU_CONFIG.TextureMode.get())
                .setDefaultValue(TextureMode.FROM_RESOURCE)
                .setEnumNameProvider(mode -> Component.translatable("endinv.setting.entry." + mode.name()))
                .setSaveConsumer(value -> ClientConfigs.ATTACHED_MENU_CONFIG.TextureMode.set(value))
                .build());

        category.addEntry(entryBuilder.startIntField(Component.translatable("endinv.setting.max_page_bar"), ClientConfigs.EIM_CONFIG.PageSwitchBar.MaxBars.get())
                .setDefaultValue(10)
                .setMin(1)
                .setSaveConsumer(value -> ClientConfigs.EIM_CONFIG.PageSwitchBar.MaxBars.set(value))
                .build());

        category.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.screen_debug"), ClientConfigs.SCREEN_DEBUG.get())
                .setDefaultValue(false)
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
            boolean hidden = ClientConfigs.EIM_CONFIG.DontDisplayPages.get().contains(id);
            boolean defaultHidden = false;

            pages.addEntry(entryBuilder.startBooleanToggle(Component.translatable("endinv.setting.hide_page", id), hidden)
                    .setDefaultValue(defaultHidden)
                    .setSaveConsumer(value -> {
                        var list = ClientConfigs.EIM_CONFIG.DontDisplayPages.get();
                        if (value) {
                            if (!list.contains(id)) list.add(id);
                        } else {
                            list.remove(id);
                        }
                        ClientConfigs.EIM_CONFIG.DontDisplayPages.set(list);
                    })
                    .build());
        }
    }
}
