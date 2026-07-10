package com.kwwsyk.endinv.neoforge.events;

import com.kwwsyk.endinv.common.EndlessInventory;
import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.ModRegistries;
import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.network.payloads.toClient.ItemPickedUpPayload;
import com.kwwsyk.endinv.common.options.ServerConfigs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.*;

import static com.kwwsyk.endinv.common.ModInfo.getPacketDistributor;

/**Handle auto-pickup features, triggered when
 * 1. Entity drop item and exp: automatically transfer items into EndInv, and exp picked up, meaning jumping item and exp entity spawn.
 * 2. Block drop item and exp: Similar to item behavior. But beds or chests may still drop item entities as different drop logic.
 * 3. Picked-up items (by player touch item entities) will be automatically transferred into EndInv. But items satisfied with some conditions will stay in playerInv.
 */
@EventBusSubscriber(modid = ModInfo.MOD_ID)
public class LootEvent {

    private static final Set<ItemEntity> CapturedDrops = new HashSet<>();

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event){
        List<ItemEntity> drops = event.getDrops();
        if(ServerConfigs.PICKUP_HELPER.ITEM_DROPS.PROTECT_DROPS.get()){
            drops.forEach(
                    item -> {
                        item.invulnerableTime = 6000;
                    }
            );
        }
        if(!(event.getBreaker() instanceof Player player)) return;
        if(ServerConfigs.PICKUP_HELPER.ITEM_DROPS.DIRECTLY_SEND_TO_ENDINV.get()){
            drops.forEach(
                    itemEntity -> {
                        ItemStack remain = sendItemToEndinv(player, itemEntity.getItem());
                        if(!remain.isEmpty()){
                            itemEntity.setItem(remain);
                        }
                    }
            );
        }
        if(ServerConfigs.PICKUP_HELPER.ITEM_DROPS.SEND_TO_INVENTORY.get()){
            drops.forEach(
                    itemEntity -> {
                        CapturedDrops.add(itemEntity);
                        itemEntity.setNoPickUpDelay();
                        itemEntity.playerTouch(player);
                    }
            );
        }
        int v;
        if((v = ServerConfigs.PICKUP_HELPER.ITEM_DROPS.DIRECTED_DISTRIBUTE.get()) != 0){
            drops.forEach(
                    itemEntity -> {
                        if(itemEntity.isRemoved()) return;
                        if(v < 0) tpEntityToPlayer(itemEntity, player);
                        else directEntityToPlayer(itemEntity, player, 0.05 * v);
                    }
            );
        }

        if(ServerConfigs.PICKUP_HELPER.EXP_DROPS.GIVE_DIRECTLY.get()){
            int xp = event.getDroppedExperience();
            if(xp > 0){
                player.giveExperiencePoints(xp);
                event.setDroppedExperience(0);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        Collection<ItemEntity> drops = event.getDrops();
        if(ServerConfigs.PICKUP_HELPER.ITEM_DROPS.PROTECT_DROPS.get()){
            drops.forEach(item -> item.invulnerableTime = 6000);
        }
        if(!(event.getSource().getEntity() instanceof Player player)) return;
        if(ServerConfigs.PICKUP_HELPER.ITEM_DROPS.DIRECTLY_SEND_TO_ENDINV.get()){
            drops.forEach(
                    itemEntity -> {
                        ItemStack remain = sendItemToEndinv(player, itemEntity.getItem());
                        if(!remain.isEmpty()){
                            itemEntity.setItem(remain);
                        }
                    }
            );
        }
        if(ServerConfigs.PICKUP_HELPER.ITEM_DROPS.SEND_TO_INVENTORY.get()){
            drops.forEach(itemEntity -> {
                CapturedDrops.add(itemEntity);
                itemEntity.setNoPickUpDelay();
                itemEntity.playerTouch(player);
            });
        }

        int v;
        if((v = ServerConfigs.PICKUP_HELPER.ITEM_DROPS.DIRECTED_DISTRIBUTE.get()) != 0){
            drops.forEach(itemEntity -> {
                if(itemEntity.isRemoved()) return;
                if(v < 0) tpEntityToPlayer(itemEntity, player);
                else directEntityToPlayer(itemEntity, player, 0.05 * v);
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExpDrops(LivingExperienceDropEvent event){
        Player player = event.getAttackingPlayer();
        if(player == null) return;

        // Deliver experience directly via event when configured
        if(ServerConfigs.PICKUP_HELPER.EXP_DROPS.GIVE_DIRECTLY.get()){
            int xp = event.getDroppedExperience();
            xp = repairPlayerItems((ServerPlayer) player, xp);
            if(xp > 0){
                player.giveExperiencePoints(xp);
                event.setDroppedExperience(0);
            }
        }

        // Note: Protection and directed distribution for XP orbs likely require
        // altering ExperienceOrb behavior (e.g., via mixin). Left unimplemented here.
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPickupItem(ItemEntityPickupEvent.Pre event){
        Player player = event.getPlayer();
        if(!(player instanceof ServerPlayer) || !isPlayerEnabledAutoPick(player)){
            return;
        }
        ItemEntity entity = event.getItemEntity();
        if(entity.hasPickUpDelay() || entity.getTarget()!=null && entity.getTarget()!=player.getUUID()) return;
        if(
                ServerConfigs.PICKUP_HELPER.ITEM_DROPS.ENDINV_AFTER_INVENTORY.get() && CapturedDrops.contains(entity)
                || ServerConfigs.PICKUP_HELPER.ITEM_DROPS.PICK_TO_ENDINV.get()
        ) {
            ItemStack stack = entity.getItem();
            ItemStack remain = sendItemToEndinv(player, stack);
            entity.setItem(remain);
        }
    }

    private static ItemStack sendItemToEndinv(Player player, ItemStack stack){
        if (player instanceof ServerPlayer && shouldMoveTo(player, stack)) {
            if(ServerLevelEndInv.getEndInvForPlayer(player).isPresent()){
                EndlessInventory endinv = ServerLevelEndInv.getEndInvForPlayer(player).get();
                ItemStack remain = endinv.addItem(stack.copy());
                if (!stack.isEmpty())
                    getPacketDistributor().sendToPlayer((ServerPlayer) player, new ItemPickedUpPayload(stack.copy().split(remain.getCount())));
                return remain;
            }
        }
        return stack;
    }

    /**Items satisfied with several conditions will stay in the player inventory.
     * 1. Player has mergeable items in the inventory.
     * 2. Player has unstackable some class items in inventory or worn, e.g. {@link SwordItem},{@link ArmorItem}...
     */
    private static boolean shouldMoveTo(Player player, ItemStack stack){
        if(stack.isEmpty()) return false;
        Item item = stack.getItem();
        switch (item){
            case SwordItem swordItem -> {
                return hasSuch(player,swordItem);
            }
            case AxeItem axeItem -> {
                return hasSuch(player,axeItem);
            }
            case PickaxeItem such -> {
                return hasSuch(player,such);
            }
            case ShovelItem such -> {
                return hasSuch(player,such);
            }
            case HoeItem such -> {
                return hasSuch(player,such);
            }
            case TridentItem such -> {
                return hasSuch(player,such);
            }
            case ShieldItem such -> {
                return hasSuch(player,such);
            }
            case ShearsItem such -> {
                return hasSuch(player,such);
            }
            case BoatItem such -> {
                return hasSuch(player,such);
            }
            case ElytraItem such -> {
                return hasSuch(player,such);
            }
            case BowItem such -> {
                return hasSuch(player,such);
            }
            case CrossbowItem such -> {
                return hasSuch(player,such);
            }
            case MaceItem such -> {
                return hasSuch(player,such);
            }
            case ArmorItem armorItem -> {
                return hasOrSwearing(player,armorItem);
            }
            default -> {
                return !canMerge(player,stack);
            }
        }
    }

    private static boolean canMerge(Player player, ItemStack stack){
        return player.inventoryMenu.slots.stream().anyMatch(slot -> ItemStack.isSameItemSameComponents(slot.getItem(), stack));
    }

    private static boolean hasSuch(Player player, Item item){
        return player.inventoryMenu.slots.stream().anyMatch(slot->slot.getItem().getItem().getClass()==item.getClass());
    }

    private static boolean hasOrSwearing(Player player,ArmorItem armor){
        EquipmentSlot slot = armor.getEquipmentSlot();
        ItemStack equipped = player.getItemBySlot(slot);
        if(equipped.isEmpty()){
            return player.inventoryMenu.slots.stream().anyMatch(slt-> slt.getItem().getItem() instanceof ArmorItem a1 && a1.getEquipmentSlot() == slot);
        }
        return true;
    }


    //copied from ExperienceOrb.java
    private static int repairPlayerItems(ServerPlayer player, int value) {
        Optional<EnchantedItemInUse> optional = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (optional.isPresent()) {
            ItemStack itemstack = optional.get().itemStack();
            int i = EnchantmentHelper.modifyDurabilityToRepairFromXp(player.serverLevel(), itemstack, (int) (value * itemstack.getXpRepairRatio()));
            int j = Math.min(i, itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - j);
            if (j > 0) {
                int k = value - j * value / i;
                if (k > 0) {
                    return repairPlayerItems(player, k);
                }
            }

            return 0;
        } else {
            return value;
        }
    }

    private static boolean isPlayerEnabledAutoPick(Player player){
        return ModRegistries.NbtAttachments.getSyncedConfig().computeIfAbsent(player).autoPicking();
    }

    private static void directEntityToPlayer(Entity entity,
                                             Player player,
                                             double speed) {
        var from = entity.position();
        var to = player.position().add(0.0, player.getBbHeight() * 0.75, 0.0);
        var dir = to.subtract(from);
        if (dir.lengthSqr() < 1.0e-6) return;

        dir = dir.scale(speed);
        entity.setDeltaMovement(dir);
    }

    private static void tpEntityToPlayer(Entity entity,
                                         Player player
    ) {
        var to = player.position();
        entity.setPos(to);
    }
}
