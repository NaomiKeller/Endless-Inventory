package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.IPlatform;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.NbtAttachment;
import com.kwwsyk.endinv.common.network.IPacketDistributor;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.options.IServerConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
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
        super.init();
        if (EnvType.CLIENT == FabricLoader.getInstance().getEnvironmentType()) {
            new ClientModInit();
        }

        ModInfo.setPacketDistributor(new FabricPacketDistributor());
        ModInfo.setServerConfig(new ServerConfig());
        com.kwwsyk.endinv.fabric.nbtAttachment.AttachingCapabilities.register();
    }

    @Override
    protected IPlatform loadOtherPlatformSpecific() {
        return (clickedItem, carriedItem, slot, action, player, access) -> {
            var featureflagset = player.level().enabledFeatures();
            return (carriedItem.isItemEnabled(featureflagset) && carriedItem.overrideStackedOnOther(slot, action, player))
                    || (clickedItem.isItemEnabled(featureflagset) && clickedItem.overrideOtherStackedOnMe(carriedItem, slot, action, player, access));
        };
    }

    @Override
    protected IPacketDistributor loadPacketDistributor() {
        return new FabricPacketDistributor();
    }

    @Override
    protected IServerConfig loadServerConfig() {
        return new ServerConfig();
    }

    @Override
    protected RegistryCallback<Item> itemReg() {
        return new RegistryCallback<>() {
            @Override
            public <R extends Item> Supplier<R> register(String id, Supplier<R> supplier) {
                ResourceLocation loc = withModLocation(id);
                R inst = supplier.get();
                net.minecraft.core.Registry.register(Registries.ITEM, loc, inst);
                return () -> inst;
            }
        };
    }

    @Override
    protected RegistryCallback<MenuType<?>> menuReg() {
        return new RegistryCallback<>() {
            @Override
            public <R extends MenuType<?>> Supplier<R> register(String id, Supplier<R> supplier) {
                ResourceLocation loc = withModLocation(id);
                R inst = supplier.get();
                net.minecraft.core.Registry.register(Registries.MENU, loc, inst);
                return () -> inst;
            }
        };
    }

    @Override
    protected Supplier<MenuType<com.kwwsyk.endinv.common.menu.EndlessInventoryMenu>> createEndInvMenuType() {
        return () -> new MenuType<>(com.kwwsyk.endinv.common.menu.EndlessInventoryMenu::createClient, FeatureFlags.DEFAULT_FLAGS);
    }

    @Override
    protected NbtAttachment<UUID> createEndInvUUID(String name) {
        return new NbtAttachment<>() {
            @Override
            public UUID getWith(Player player) {
                UUID u = com.kwwsyk.endinv.fabric.nbtAttachment.FabricNbtStorage.getUuid(player);
                return u == null ? ModInfo.DEFAULT_UUID : u;
            }

            @Override
            public void setTo(Player player, UUID uuid) {
                com.kwwsyk.endinv.fabric.nbtAttachment.FabricNbtStorage.setUuid(player, uuid);
            }

            @Override
            public UUID computeIfAbsent(Player player) {
                UUID u = com.kwwsyk.endinv.fabric.nbtAttachment.FabricNbtStorage.getUuid(player);
                if (u == null) {
                    com.kwwsyk.endinv.fabric.nbtAttachment.FabricNbtStorage.setUuid(player, ModInfo.DEFAULT_UUID);
                    return ModInfo.DEFAULT_UUID;
                }
                return u;
            }
        };
    }

    @Override
    protected NbtAttachment<SyncedConfig> createSyncedConfig(String name) {
        return new NbtAttachment<>() {
            private final String key = "endinv_settings";

            @Override
            public SyncedConfig getWith(Player player) {
                try {
                    var tag = player.getPersistentData();
                    if (tag == null || !tag.contains(key)) return SyncedConfig.DEFAULT;
                    var compound = tag.getCompound(key);
                    return SyncedConfig.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, compound)
                            .resultOrPartial(err -> {})
                            .orElse(SyncedConfig.DEFAULT);
                } catch (Exception e) {
                    return SyncedConfig.DEFAULT;
                }
            }

            @Override
            public void setTo(Player player, SyncedConfig syncedConfig) {
                try {
                    var compound = (net.minecraft.nbt.CompoundTag) SyncedConfig.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, syncedConfig).getOrThrow(false, err -> {});
                    player.getPersistentData().put(key, compound);
                } catch (Exception ignored) {
                }
            }

            @Override
            public SyncedConfig computeIfAbsent(Player player) {
                var v = getWith(player);
                if (v == SyncedConfig.DEFAULT) {
                    setTo(player, SyncedConfig.DEFAULT);
                    return SyncedConfig.DEFAULT;
                }
                return v;
            }
        };
    }

}
