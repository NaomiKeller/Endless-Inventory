package com.kwwsyk.endinv.fabric;

import com.kwwsyk.endinv.common.AbstractModInitializer;
import com.kwwsyk.endinv.common.IPlatform;
import com.kwwsyk.endinv.common.NbtAttachment;
import com.kwwsyk.endinv.common.menu.EndlessInventoryMenu;
import com.kwwsyk.endinv.common.network.IPacketDistributor;
import com.kwwsyk.endinv.common.network.payloads.SyncedConfig;
import com.kwwsyk.endinv.common.options.ServerConfigs;
import com.kwwsyk.endinv.common.options.config.json.JsonConfigurationHandler;
import com.kwwsyk.endinv.fabric.event.FabricEvents;
import com.kwwsyk.endinv.fabric.network.FabricServerNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;
@SuppressWarnings("UnstableApiUsage")
public class ModInit extends AbstractModInitializer implements ModInitializer {

    public static final AttachmentType<UUID> ENDINV_UUID = AttachmentRegistry.create(
            withModLocation("endinv_uuid"),
            builder -> builder
                    .initializer(UUID::randomUUID)
                    .persistent(UUIDUtil.CODEC)
                    .copyOnDeath()
                    .syncWith(
                            UUIDUtil.STREAM_CODEC,
                            AttachmentSyncPredicate.targetOnly()
                    )
    );
    public static final AttachmentType<SyncedConfig> SYNCED_CONFIG = AttachmentRegistry.create(
            withModLocation("synced_config"),
            builder -> builder
                    .initializer(()-> SyncedConfig.DEFAULT)
                    .persistent(SyncedConfig.CODEC)
                    .copyOnDeath()
                    .syncWith(
                            SyncedConfig.STREAM_CODEC,
                            AttachmentSyncPredicate.targetOnly()
                    )
    );

    @Override
    public void onInitialize() {
        // Register payload types first, then receivers
        com.kwwsyk.endinv.fabric.network.FabricNetworking.init();
        FabricServerNetworking.init();
        FabricEvents.init();
        super.init();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                && FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            try {
                Class<?> clazz = Class.forName("com.kwwsyk.endinv.fabric.integrates.clothconfig.ClothConfigIntegration");
                clazz.getMethod("register").invoke(null);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    protected IPlatform loadOtherPlatformSpecific() {
        return new IPlatform() {
            @Override
            public boolean onItemStackedOn(ItemStack clickedItem, ItemStack carriedItem, Slot slot, ClickAction action, Player player, SlotAccess access) {
                var features = player.level().enabledFeatures();
                return (carriedItem.isItemEnabled(features) && carriedItem.overrideStackedOnOther(slot, action, player))
                        || (clickedItem.isItemEnabled(features) && clickedItem.overrideOtherStackedOnMe(carriedItem, slot, action, player, access));
            }

            @Override
            public boolean isModLoaded(String modid) {
                return FabricLoader.getInstance().isModLoaded(modid);
            }
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
                Identifier location = withModLocation(id);
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
            public <R extends MenuType<?>> Supplier<R> register(String id, Supplier<R> supplier) {
                Identifier location = withModLocation(id);
                R type = supplier.get();
                R registered = net.minecraft.core.Registry.register(BuiltInRegistries.MENU, location, type);
                return () -> registered;
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

    @Override
    protected NbtAttachment<UUID> createEndInvUUID(String name) {
        return new NbtAttachment<>() {
            @Override@Nullable
            public UUID getWith(Player player) {
                return player.getAttached(ENDINV_UUID);
            }

            @Override
            public void setTo(Player player, UUID uuid) {
                player.setAttached(ENDINV_UUID, uuid);
            }

            @Override
            public UUID computeIfAbsent(Player player) {
                return player.getAttachedOrCreate(ENDINV_UUID);
            }
        };
    }

    @Override@SuppressWarnings("UnstableApiUsage")
    protected NbtAttachment<SyncedConfig> createSyncedConfig(String name) {
        return new NbtAttachment<>() {
            @Override@Nullable
            public SyncedConfig getWith(Player player) {
                return player.getAttached(SYNCED_CONFIG);
            }

            @Override
            public void setTo(Player player, SyncedConfig syncedConfig) {
                player.setAttached(SYNCED_CONFIG, syncedConfig);
            }

            @Override
            public SyncedConfig computeIfAbsent(Player player) {
                return player.getAttachedOrCreate(SYNCED_CONFIG);
            }
        };
    }
}
