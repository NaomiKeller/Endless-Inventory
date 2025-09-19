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
import com.kwwsyk.endinv.fabric.network.FabricNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import java.util.UUID;
import java.util.function.Supplier;

import static com.kwwsyk.endinv.common.AbstractModInitializer.withModLocation;

public class ModInit extends AbstractModInitializer implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricNetworking.init();
        FabricEvents.init();
        super.init();
        com.kwwsyk.endinv.fabric.nbtAttachment.AttachingCapabilities.register();
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
                var data = player.getPersistentData();
                return data.hasUUID(name) ? data.getUUID(name) : ModInfo.DEFAULT_UUID;
            }

            @Override
            public void setTo(Player player, UUID uuid) {
                player.getPersistentData().putUUID(name, uuid);
            }

            @Override
            public UUID computeIfAbsent(Player player) {
                var data = player.getPersistentData();
                if (!data.hasUUID(name)) {
                    data.putUUID(name, ModInfo.DEFAULT_UUID);
                }
                return data.getUUID(name);
            }
        };
    }

    @Override
    protected NbtAttachment<SyncedConfig> createSyncedConfig(String name) {
        return new NbtAttachment<>() {
            @Override
            public SyncedConfig getWith(Player player) {
                var data = player.getPersistentData();
                if (!data.contains(name, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                    return SyncedConfig.DEFAULT;
                }
                var compound = data.getCompound(name);
                return SyncedConfig.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, compound)
                        .resultOrPartial(ModInit::logCodecError)
                        .orElse(SyncedConfig.DEFAULT);
            }

            @Override
            public void setTo(Player player, SyncedConfig syncedConfig) {
                var data = player.getPersistentData();
                SyncedConfig.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, syncedConfig)
                        .resultOrPartial(ModInit::logCodecError)
                        .ifPresent(tag -> data.put(name, (net.minecraft.nbt.CompoundTag) tag));
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
