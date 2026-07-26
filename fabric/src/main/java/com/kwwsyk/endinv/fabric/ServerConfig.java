package com.kwwsyk.endinv.fabric;

import com.google.gson.*;
import com.kwwsyk.endinv.common.options.*;
import com.kwwsyk.endinv.common.util.Accessibility;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServerConfig implements IServerConfig {

    public static final ServerConfig INSTANCE = new ServerConfig();

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);
    private static final String MAX_STACK = "maxStackSize";
    private static final String ENABLE_INFINITE = "enableInfinite";
    private static final String ENABLE_AUTO_PICK = "enableAutoPick";
    private static final String ENABLE_ATTACHING = "enableAttaching";
    private static final String TRANSFER_MODE = "transferMode";
    private static final String DEFAULT_ACCESS = "defaultAccessibility";
    private static final String CREATION_MODE = "creationMode";
    private static final String CONVERT_EMPTY_TAG = "convertEmptyTag";
    private static final String SPECIFIED_MENU_ATTACH_LIST = "specifiedMenuAttachability"; // ["<menu_id>:<true|false>"]
    private static final String INVENTORY_ATTACHABLE = "inventoryAttachable";

    private final Path path;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private int maxStackSize = Integer.MAX_VALUE;
    private boolean infinite = false;
    private boolean autoPick = false;
    private boolean attaching = true;
    private ContentTransferMode transferModeValue = ContentTransferMode.ALL;
    private Accessibility defaultAccess = Accessibility.PUBLIC;
    private MissingEndInvPolicy creationPolicy = MissingEndInvPolicy.CREATE_PER_PLAYER;
    private boolean convertEmpty = true;
    private com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig specifiedAttach = new com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig();

    private ServerConfig() {
        this.path = FabricLoader.getInstance().getConfigDir().resolve("endless_inventory-server.json");
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
                LOGGER.warn("Server config is not a json object, using defaults");
                return;
            }
            JsonObject json = element.getAsJsonObject();
            if (json.has(MAX_STACK) && json.get(MAX_STACK).isJsonPrimitive()) {
                maxStackSize = json.get(MAX_STACK).getAsInt();
            }
            if (json.has(ENABLE_INFINITE) && json.get(ENABLE_INFINITE).isJsonPrimitive()) {
                infinite = json.get(ENABLE_INFINITE).getAsBoolean();
            }
            if (json.has(ENABLE_AUTO_PICK) && json.get(ENABLE_AUTO_PICK).isJsonPrimitive()) {
                autoPick = json.get(ENABLE_AUTO_PICK).getAsBoolean();
            }
            if (json.has(ENABLE_ATTACHING) && json.get(ENABLE_ATTACHING).isJsonPrimitive()) {
                attaching = json.get(ENABLE_ATTACHING).getAsBoolean();
            }
            if (json.has(TRANSFER_MODE) && json.get(TRANSFER_MODE).isJsonPrimitive()) {
                try {
                    transferModeValue = ContentTransferMode.valueOf(json.get(TRANSFER_MODE).getAsString());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Unknown transfer mode in config: {}", json.get(TRANSFER_MODE).getAsString());
                }
            }
            if (json.has(DEFAULT_ACCESS) && json.get(DEFAULT_ACCESS).isJsonPrimitive()) {
                try {
                    defaultAccess = Accessibility.valueOf(json.get(DEFAULT_ACCESS).getAsString());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Unknown accessibility in config: {}", json.get(DEFAULT_ACCESS).getAsString());
                }
            }
            if (json.has(CREATION_MODE) && json.get(CREATION_MODE).isJsonPrimitive()) {
                try {
                    creationPolicy = MissingEndInvPolicy.valueOf(json.get(CREATION_MODE).getAsString());
                } catch (IllegalArgumentException ex) {
                    LOGGER.warn("Unknown creation mode in config: {}", json.get(CREATION_MODE).getAsString());
                }
            }
            if (json.has(CONVERT_EMPTY_TAG) && json.get(CONVERT_EMPTY_TAG).isJsonPrimitive()) {
                convertEmpty = json.get(CONVERT_EMPTY_TAG).getAsBoolean();
            }

            // specified menu attachability
            com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig readCfg = new com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig();
            if (json.has(SPECIFIED_MENU_ATTACH_LIST) && json.get(SPECIFIED_MENU_ATTACH_LIST).isJsonArray()) {
                var arr = json.getAsJsonArray(SPECIFIED_MENU_ATTACH_LIST);
                java.util.List<String> entries = new java.util.ArrayList<>(arr.size());
                for (var el : arr) if (el.isJsonPrimitive()) entries.add(el.getAsString());
                readCfg = com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig.Parser.readList(entries);
            }
            boolean invAttach = true;
            if (json.has(INVENTORY_ATTACHABLE) && json.get(INVENTORY_ATTACHABLE).isJsonPrimitive()) {
                invAttach = json.get(INVENTORY_ATTACHABLE).getAsBoolean();
            }
            readCfg.setInventoryAttachable(invAttach);
            specifiedAttach = readCfg;
        } catch (IOException ex) {
            LOGGER.warn("Failed to read server config, using defaults", ex);
        }
    }

    private void save() {
        try {
            Files.createDirectories(path.getParent());
            JsonObject json = new JsonObject();
            json.addProperty(MAX_STACK, maxStackSize);
            json.addProperty(ENABLE_INFINITE, infinite);
            json.addProperty(ENABLE_AUTO_PICK, autoPick);
            json.addProperty(ENABLE_ATTACHING, attaching);
            json.addProperty(TRANSFER_MODE, transferModeValue.name());
            json.addProperty(DEFAULT_ACCESS, defaultAccess.name());
            json.addProperty(CREATION_MODE, creationPolicy.name());
            json.addProperty(CONVERT_EMPTY_TAG, convertEmpty);
            // write specified attachability
            var arr = new com.google.gson.JsonArray();
            for (String s : com.kwwsyk.endinv.common.options.SpecifiedMenuAttachingConfig.Parser.fromMap(specifiedAttach.getConfigs())) {
                arr.add(s);
            }
            json.add(SPECIFIED_MENU_ATTACH_LIST, arr);
            json.addProperty(INVENTORY_ATTACHABLE, specifiedAttach.isInventoryAttachable());
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(json, writer);
            }
        } catch (IOException ex) {
            LOGGER.warn("Failed to save server config", ex);
        }
    }

    private <T> LegacyIConfigValue<T> tracked(java.util.function.Supplier<T> getter, java.util.function.Consumer<T> setter) {
        return LegacyIConfigValue.of(getter, value -> {
            setter.accept(value);
            save();
        });
    }

    @Override
    public LegacyIConfigValue<Integer> getMaxAllowedStackSize() {
        return tracked(() -> maxStackSize, value -> maxStackSize = value);
    }

    @Override
    public LegacyIConfigValue<Boolean> allowInfinityMode() {
        return tracked(() -> infinite, value -> infinite = value);
    }

    @Override
    public LegacyIConfigValue<Boolean> enableAutoPick() {
        return LegacyIConfigValue.of(() -> autoPick, value -> {
            autoPick = value;
            save();
            onAttachingOrAutopickConfigChanged();
        });
    }

    @Override
    public LegacyIConfigValue<Boolean> enableAttaching() {
        return LegacyIConfigValue.of(() -> attaching, value -> {
            attaching = value;
            save();
            onAttachingOrAutopickConfigChanged();
            onSpecifiedMenuAttachabilityChanged();
        });
    }

    @Override
    public LegacyIConfigValue<ContentTransferMode> transferMode() {
        return tracked(() -> transferModeValue, value -> transferModeValue = value);
    }

    @Override
    public LegacyIConfigValue<Accessibility> defaultAccessibility() {
        return tracked(() -> defaultAccess, value -> defaultAccess = value);
    }

    @Override
    public LegacyIConfigValue<MissingEndInvPolicy> policyHandlingMissing() {
        return tracked(() -> creationPolicy, value -> creationPolicy = value);
    }

    @Override
    public LegacyIConfigValue<Boolean> doConvertEmptyTag() {
        return tracked(() -> convertEmpty, value -> convertEmpty = value);
    }

    @Override
    public LegacyIConfigValue<SpecifiedMenuAttachingConfig> specifiedMenuAttachability() {
        return LegacyIConfigValue.of(() -> specifiedAttach, value -> {
            specifiedAttach.clearConfigs();
            specifiedAttach.getConfigs().putAll(value.getConfigs());
            specifiedAttach.setInventoryAttachable(value.isInventoryAttachable());
            save();
            onSpecifiedMenuAttachabilityChanged();
        });
    }
}
