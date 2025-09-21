package com.kwwsyk.endinv.fabric.client;

import com.google.gson.*;
import com.kwwsyk.endinv.common.client.option.IClientConfig;
import com.kwwsyk.endinv.common.client.option.TextureMode;
import com.kwwsyk.endinv.common.menu.page.PageType;
import com.kwwsyk.endinv.common.options.IConfigValue;
import com.kwwsyk.endinv.common.util.SortType;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class FabricClientConfig implements IClientConfig {

    public static final FabricClientConfig INSTANCE = new FabricClientConfig();

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricClientConfig.class);
    private static final String ATTACHING = "attaching";
    private static final String ROWS = "rows";
    private static final String COLUMNS = "columns";
    private static final String AUTO_SUIT = "autoSuitColumn";
    private static final String TEXTURE = "texture";
    private static final String SCREEN_DEBUG = "screenDebug";
    private static final String MAX_PAGE_BARS = "maxPageBars";
    private static final String HIDDEN_PAGES = "hiddenPages";
    private static final String PAGE_ID = "pageId";
    private static final String SEARCH = "search";
    private static final String SORT_TYPE = "sortType";
    private static final String REVERSE_SORT = "reverseSort";

    private final Path path;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private boolean attachingMenu = true;
    private int rowCount = 0;
    private int columnCount = 9;
    private boolean autoSuitColumn = true;
    private TextureMode textureMode = TextureMode.FROM_RESOURCE;
    private boolean screenDebug = false;
    private int maxPageBars = 10;
    private final Set<String> hiddenPages = new HashSet<>();
    private SortType sortType = SortType.DEFAULT;
    private boolean reverseSort = false;
    private String activePageId = PageType.DEFAULT_KEY;
    private String searchQuery = "";

    private FabricClientConfig() {
        this.path = FabricLoader.getInstance().getConfigDir().resolve("endless_inventory-client.json");
        load();
    }

    private void load() {
        if (Files.notExists(path)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement element = JsonParser.parseReader(reader);
            if (!element.isJsonObject()) {
                LOGGER.warn("Client config is not a json object, using defaults");
                return;
            }
            JsonObject json = element.getAsJsonObject();
            if (json.has(ATTACHING)) {
                attachingMenu = json.get(ATTACHING).getAsBoolean();
            }
            if (json.has(ROWS)) {
                rowCount = json.get(ROWS).getAsInt();
            }
            if (json.has(COLUMNS)) {
                columnCount = json.get(COLUMNS).getAsInt();
            }
            if (json.has(AUTO_SUIT)) {
                autoSuitColumn = json.get(AUTO_SUIT).getAsBoolean();
            }
            if (json.has(TEXTURE)) {
                try {
                    textureMode = TextureMode.valueOf(json.get(TEXTURE).getAsString());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Unknown texture mode: {}", json.get(TEXTURE).getAsString());
                }
            }
            if (json.has(SCREEN_DEBUG)) {
                screenDebug = json.get(SCREEN_DEBUG).getAsBoolean();
            }
            if (json.has(MAX_PAGE_BARS)) {
                maxPageBars = json.get(MAX_PAGE_BARS).getAsInt();
            }
            if (json.has(SORT_TYPE)) {
                try {
                    sortType = SortType.valueOf(json.get(SORT_TYPE).getAsString());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Unknown sort type: {}", json.get(SORT_TYPE).getAsString());
                    sortType = SortType.DEFAULT;
                }
            }
            if (json.has(REVERSE_SORT)) {
                reverseSort = json.get(REVERSE_SORT).getAsBoolean();
            }
            if (json.has(PAGE_ID)) {
                activePageId = json.get(PAGE_ID).getAsString();
            }
            if (json.has(SEARCH)) {
                searchQuery = json.get(SEARCH).getAsString();
            }
            hiddenPages.clear();
            if (json.has(HIDDEN_PAGES) && json.get(HIDDEN_PAGES).isJsonArray()) {
                JsonArray array = json.getAsJsonArray(HIDDEN_PAGES);
                for (JsonElement entry : array) {
                    if (entry.isJsonPrimitive()) {
                        hiddenPages.add(entry.getAsString());
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed to read client config, using defaults", ex);
        }
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            JsonObject json = new JsonObject();
            json.addProperty(ATTACHING, attachingMenu);
            json.addProperty(ROWS, rowCount);
            json.addProperty(COLUMNS, columnCount);
            json.addProperty(AUTO_SUIT, autoSuitColumn);
            json.addProperty(TEXTURE, textureMode.name());
            json.addProperty(SCREEN_DEBUG, screenDebug);
            json.addProperty(MAX_PAGE_BARS, maxPageBars);
            JsonArray array = new JsonArray();
            hiddenPages.forEach(array::add);
            json.add(HIDDEN_PAGES, array);
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(json, writer);
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed to save client config", ex);
        }
    }

    private <T> IConfigValue<T> tracked(Supplier<T> getter, Consumer<T> setter) {
        return IConfigValue.of(getter, value -> {
            setter.accept(value);
            save();
        });
    }

    @Override
    public IConfigValue<Boolean> attaching() {
        return tracked(() -> attachingMenu, value -> attachingMenu = value);
    }

    @Override
    public IConfigValue<Integer> rows() {
        return tracked(() -> rowCount, value -> rowCount = value);
    }

    @Override
    public IConfigValue<Integer> columns() {
        return tracked(() -> columnCount, value -> columnCount = value);
    }

    @Override
    public IConfigValue<Boolean> autoSuitColumn() {
        return tracked(() -> autoSuitColumn, value -> autoSuitColumn = value);
    }

    @Override
    public IConfigValue<TextureMode> textureMode() {
        return tracked(() -> textureMode, value -> textureMode = value);
    }

    @Override
    public IConfigValue<Boolean> screenDebugging() {
        return tracked(() -> screenDebug, value -> screenDebug = value);
    }

    @Override
    public IConfigValue<Integer> maxPageBarCount() {
        return tracked(() -> maxPageBars, value -> maxPageBars = value);
    }

    @Override
    public Set<String> hidingPageIds() {
        return Collections.unmodifiableSet(hiddenPages);
    }

    @Override
    public void setPageHiding(String id, boolean hiding) {
        if (hiding) {
            hiddenPages.add(id);
        } else {
            hiddenPages.remove(id);
        }
        save();
    }
}

