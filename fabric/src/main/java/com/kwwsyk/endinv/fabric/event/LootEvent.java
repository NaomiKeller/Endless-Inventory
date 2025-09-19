package com.kwwsyk.endinv.fabric.event;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.toClient.ItemPickedUpPayload;
import com.kwwsyk.endinv.fabric.ServerConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Optional;

public final class LootEvent {

    private LootEvent() {
    }

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> handleDrops(context, drops));
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
