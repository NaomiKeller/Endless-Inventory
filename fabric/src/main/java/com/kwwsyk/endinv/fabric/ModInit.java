package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.IPlatform;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.NbtAttachment;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import com.kwwsyk.endinv.common.network.IPacketDistributor;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.options.ServerConfigs;
import com.kwwsyk.endinv.common.options.config.json.JsonConfigurationHandler;
import com.kwwsyk.endinv.fabric.event.FabricEvents;
import com.kwwsyk.endinv.fabric.integrates.clothconfig.ClothConfigIntegration;
import com.kwwsyk.endinv.fabric.nbtAttachment.FabricAttachment;
import com.kwwsyk.endinv.fabric.network.FabricServerNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import java.util.UUID;
import java.util.function.Supplier;

public class ModInit extends AbstractModInitializer implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register payload types first, then receivers
        com.kwwsyk.endinv.fabric.network.FabricNetworking.init();
        FabricServerNetworking.init();
        FabricEvents.init();
        super.init();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            ClothConfigIntegration.register();
        }
    }

    @Override
    protected IPlatform loadOtherPlatformSpecific() {
        return (clickedItem, carriedItem, slot, action, player, access) -> {
            var features = player.level().enabledFeatures();
            return (carriedItem.isItemEnabled(features) && carriedItem.overrideStackedOnOther(slot, action, player))
                    || (clickedItem.isItemEnabled(features) && clickedItem.overrideOtherStackedOnMe(carriedItem, slot, action, player, access));
        };
    }

    @Override
    protected IPacketDistributor loadPacketDistributor() {
        return new FabricPacketDistributor();
    }

    @Override
    protected void loadServerConfig() {
        new JsonConfigurationHandler(FabricLoader.getInstance().getConfigDir().resolve("endless_inventory-server.json"), ServerConfigs.getConfigs())
                .load();
    }

    @Override
    protected RegistryCallback<Item> itemReg() {
        return new RegistryCallback<>() {
            @Override
            public <R extends Item> Supplier<R> register(String id, Supplier<R> supplier) {
                ResourceLocation location = withModLocation(id);
                R item = supplier.get();
                R registered = net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, location, item);
                return () -> registered;
            }
        };
    }

    @Override
    protected RegistryCallback<MenuType<?>> menuReg() {
        return new RegistryCallback<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <R extends MenuType<?>> Supplier<R> register(String id, Supplier<R> supplier) {
                ResourceLocation location = withModLocation(id);
                R type = supplier.get();
                MenuType<?> registered = net.minecraft.core.Registry.register(BuiltInRegistries.MENU, location, type);
                return () -> (R) registered;
            }
        };
    }

    @Override @SuppressWarnings("unchecked")
    protected Supplier<MenuType<EndlessInventoryMenu>> createEndInvMenuType() {
        return () -> {
            @SuppressWarnings({"rawtypes", "unchecked"})
            net.minecraft.world.inventory.MenuType raw = new net.minecraft.world.inventory.MenuType(
                    (id, inventory) -> {
                        try {
                            Class<?> cls = Class.forName("com.kwwsyk.endinv.common.menu.EndlessInventoryMenu");
                            java.lang.reflect.Method m = cls.getMethod("createClient", int.class, net.minecraft.world.entity.player.Inventory.class);
                            Object menu = m.invoke(null, id, inventory);
                            return (net.minecraft.world.inventory.AbstractContainerMenu) menu;
                        } catch (Throwable t) {
                            throw new RuntimeException(t);
                        }
                    },
                    FeatureFlags.DEFAULT_FLAGS
            );
            return (MenuType<EndlessInventoryMenu>) raw;
        };
    }

    @Override@SuppressWarnings("UnstableApiUsage")
    protected NbtAttachment<UUID> createEndInvUUID(String name) {
        return new NbtAttachment<>() {
            @Override
            public UUID getWith(Player player) {
                return player.getAttachedOrElse(FabricAttachment.ENDINV_UUID, ModInfo.DEFAULT_UUID);
            }

            @Override
            public void setTo(Player player, UUID uuid) {
                player.setAttached(FabricAttachment.ENDINV_UUID, uuid);
            }

            @Override
            public UUID computeIfAbsent(Player player) {
                return player.getAttachedOrCreate(FabricAttachment.ENDINV_UUID);
            }
        };
    }

    @Override@SuppressWarnings("UnstableApiUsage")
    protected NbtAttachment<SyncedConfig> createSyncedConfig(String name) {
        return new NbtAttachment<>() {
            @Override
            public SyncedConfig getWith(Player player) {
                return player.getAttachedOrElse(FabricAttachment.SYNCED_CONFIG, SyncedConfig.DEFAULT);
            }

            @Override
            public void setTo(Player player, SyncedConfig syncedConfig) {
                player.setAttached(FabricAttachment.SYNCED_CONFIG, syncedConfig);
            }

            @Override
            public SyncedConfig computeIfAbsent(Player player) {
                return player.getAttachedOrCreate(FabricAttachment.SYNCED_CONFIG);
            }
        };
    }
}
