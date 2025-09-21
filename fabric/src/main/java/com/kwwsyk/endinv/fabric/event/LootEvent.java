package com.kwwsyk.endinv.fabric.event;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.toClient.ItemPickedUpPayload;
import com.kwwsyk.endinv.fabric.ServerConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

public final class LootEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LootEvent.class);

    private LootEvent() {
    }

    public static void register() {
        if (!registerModifyDropsCompat()) {
            LOGGER.debug("Fabric loot modify drops hook unavailable; auto-pickup will fall back to world drops.");
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean registerModifyDropsCompat() {
        try {
            Class<?> eventsClass = Class.forName("net.fabricmc.fabric.api.loot.v3.LootTableEvents");
            Object event = eventsClass.getField("MODIFY_DROPS").get(null);
            Class<?> callbackClass = Class.forName("net.fabricmc.fabric.api.loot.v3.LootTableEvents$ModifyDrops");
            Object listener = Proxy.newProxyInstance(callbackClass.getClassLoader(), new Class<?>[]{callbackClass}, (proxy, method, args) -> {
                if (args == null || args.length == 0) {
                    return null;
                }

                LootParams lootParams = null;
                List<ItemStack> drops = null;
                for (Object arg : args) {
                    if (arg instanceof LootParams params) {
                        lootParams = params;
                    } else if (arg instanceof List<?> list) {
                        if (list.isEmpty() || list.get(0) instanceof ItemStack) {
                            drops = (List<ItemStack>) list;
                        }
                    }
                }

                if (lootParams != null && drops != null) {
                    handleDrops(lootParams, drops);
                }

                return null;
            });
            event.getClass().getMethod("register", callbackClass).invoke(event, listener);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to register Fabric modify drops bridge", e);
            return false;
        }
    }

    private static void handleDrops(LootParams params, List<ItemStack> drops) {
        if (drops.isEmpty()) {
            return;
        }

        ServerPlayer player = findPlayer(params).orElse(null);
        if (player == null) {
            return;
        }

        if (!ServerConfig.INSTANCE.enableAutoPick().get() || !isAutoPickEnabled(player)) {
            return;
        }

        ServerLevelEndInv.getEndInvForPlayer(player).ifPresent(endInv -> {
            boolean consumedAll = true;

            for (int i = 0; i < drops.size(); ++i) {
                ItemStack stack = drops.get(i);
                if (stack.isEmpty()) {
                    continue;
                }

                ItemStack remain = endInv.addItem(stack.copy());
                int inserted = stack.getCount() - remain.getCount();

                if (inserted > 0) {
                    ItemStack picked = stack.copy();
                    picked.setCount(inserted);
                    ModInfo.getPacketDistributor().sendToPlayer(player, new ItemPickedUpPayload(picked));
                }

                if (remain.isEmpty()) {
                    drops.set(i, ItemStack.EMPTY);
                } else {
                    drops.set(i, remain);
                    consumedAll = false;
                }
            }

            drops.removeIf(ItemStack::isEmpty);

            if (consumedAll) {
                drops.clear();
            }
        });
    }

    private static Optional<ServerPlayer> findPlayer(LootParams params) {
        var direct = params.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (direct instanceof ServerPlayer serverPlayer) {
            return Optional.of(serverPlayer);
        }
        var attacker = params.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        if (attacker instanceof ServerPlayer serverPlayer) {
            return Optional.of(serverPlayer);
        }
        return Optional.empty();
    }

    private static boolean isAutoPickEnabled(ServerPlayer player) {
        return ModRegistries.NbtAttachments.getSyncedConfig().computeIfAbsent(player).autoPicking();
    }
}
