package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.IPlatform;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.NbtAttachment;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import com.kwwsyk.endinv.common.network.IPacketDistributor;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.options.IServerConfig;
import com.kwwsyk.endinv.fabric.event.FabricEvents;
import com.kwwsyk.endinv.fabric.integrates.clothconfig.ClothConfigIntegration;
import com.kwwsyk.endinv.fabric.nbtAttachment.FabricNbtStorage;
import com.kwwsyk.endinv.fabric.network.FabricNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
        FabricNetworking.init();
        FabricEvents.init();
        super.init();
        com.kwwsyk.endinv.fabric.nbtAttachment.AttachingCapabilities.register();
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
    protected IServerConfig loadServerConfig() {
        return ServerConfig.INSTANCE;
    }

    @Override
    protected RegistryCallback<Item> itemReg() {
        return new RegistryCallback<>() {
            @Override
            @SuppressWarnings("unchecked")
            public <R extends Item> Supplier<R> register(String id, Supplier<R> supplier) {
                ResourceLocation location = withModLocation(id);
                R item = supplier.get();
                Item registered = net.minecraft.core.Registry.register(BuiltInRegistries.ITEM, location, item);
                return () -> (R) registered;
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

    @Override
    protected Supplier<MenuType<EndlessInventoryMenu>> createEndInvMenuType() {
        return () -> new MenuType<>(EndlessInventoryMenu::createClient, FeatureFlags.DEFAULT_FLAGS);
    }

    @Override
    protected NbtAttachment<UUID> createEndInvUUID(String name) {
        return new NbtAttachment<>() {
            @Override
            public UUID getWith(Player player) {
                UUID uuid = FabricNbtStorage.getUuid(player);
                return uuid != null ? uuid : ModInfo.DEFAULT_UUID;
            }

            @Override
            public void setTo(Player player, UUID uuid) {
                FabricNbtStorage.setUuid(player, uuid);
            }

            @Override
            public UUID computeIfAbsent(Player player) {
                UUID uuid = FabricNbtStorage.getUuid(player);
                if (uuid == null) {
                    FabricNbtStorage.setUuid(player, ModInfo.DEFAULT_UUID);
                    return ModInfo.DEFAULT_UUID;
                }
                return uuid;
            }
        };
    }

    @Override
    protected NbtAttachment<SyncedConfig> createSyncedConfig(String name) {
        return new NbtAttachment<>() {
            @Override
            public SyncedConfig getWith(Player player) {
                if (!FabricNbtStorage.hasCompound(player, name)) {
                    return SyncedConfig.DEFAULT;
                }
                CompoundTag compound = FabricNbtStorage.getCompound(player, name).copy();
                return SyncedConfig.CODEC.parse(NbtOps.INSTANCE, compound)
                        .resultOrPartial(ModInit::logCodecError)
                        .orElse(SyncedConfig.DEFAULT);
            }

            @Override
            public void setTo(Player player, SyncedConfig syncedConfig) {
                SyncedConfig.CODEC.encodeStart(NbtOps.INSTANCE, syncedConfig)
                        .resultOrPartial(ModInit::logCodecError)
                        .ifPresent(tag -> {
                            if (tag instanceof CompoundTag compound) {
                                FabricNbtStorage.setCompound(player, name, compound.copy());
                            }
                        });
            }

            @Override
            public SyncedConfig computeIfAbsent(Player player) {
                SyncedConfig config = getWith(player);
                if (config == SyncedConfig.DEFAULT) {
                    setTo(player, SyncedConfig.DEFAULT);
                }
                return getWith(player);
            }
        };
    }

    private static void logCodecError(String message) {
        org.slf4j.LoggerFactory.getLogger(ModInit.class).warn("Failed to process synced config: {}", message);
    }
}
