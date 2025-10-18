package com.kwwsyk.endinv.fabric.event;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.autopick.AutoPickHelper;
import com.kwwsyk.endinv.common.autopick.events.IBlockBreakEvent;
import com.kwwsyk.endinv.common.network.payloads.toClient.ItemPickedUpPayload;
import com.kwwsyk.endinv.fabric.ServerConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;
import java.util.Optional;

public final class LootEvent {

    private LootEvent() {
    }

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            boolean[] cancelled = {false};
            AutoPickHelper.onBlockBreak(
                    new IBlockBreakEvent() {
                        @Override
                        public LevelAccessor getLevel() {
                            return world;
                        }

                        @Override
                        public BlockPos getPos() {
                            return pos;
                        }

                        @Override
                        public BlockState getState() {
                            return state;
                        }

                        @Override
                        public Player getPlayer() {
                            return player;
                        }

                        @Override
                        public int getExpToDrop() {
                            return 0;
                        }

                        @Override
                        public void setExpToDrop(int exp) {

                        }

                        @Override
                        public void setCanceled(boolean canceled) {
                            cancelled[0] = canceled;
                        }
                    }
            );
            return cancelled[0];
        });
    }

    private static void handleDrops(LootParams params, List<ItemStack> drops) {
        if (drops.isEmpty()) {
            return;
        }

        ServerPlayer player = findPlayer(params)
                .or(() -> findNearbyPlayer(params))
                .orElse(null);
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

    private static Optional<ServerPlayer> findNearbyPlayer(LootParams params) {
        try {
            var origin = params.getParamOrNull(LootContextParams.ORIGIN);
            var level = params.getLevel();
            if (origin != null) {
                // 7-block radius heuristic for block breaks
                ServerPlayer nearest = (ServerPlayer) level.getNearestPlayer(origin.x, origin.y, origin.z, 7.0, false);
                return Optional.ofNullable(nearest);
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
    }

    private static boolean isAutoPickEnabled(ServerPlayer player) {
        return ModRegistries.NbtAttachments.getSyncedConfig().getWith(player).autoPicking();
    }
}
