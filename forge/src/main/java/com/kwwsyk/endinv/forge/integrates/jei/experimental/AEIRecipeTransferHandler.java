package com.kwwsyk.endinv.forge.integrates.jei.experimental;

import com.kwwsyk.endinv.common.ServerLevelEndInv;
import com.kwwsyk.endinv.common.SourceInventory;
import com.kwwsyk.endinv.common.menu.page.pageManager.PageMetaDataManager;
import com.kwwsyk.endinv.common.util.ItemKey;
import com.kwwsyk.endinv.forge.client.events.ScreenAttachment;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Attached Endless Inventory (Screen) Recipe Transfer Handler<br>
 * Transfer items of EndInv in any menu
 */
public final class AEIRecipeTransferHandler {

    private AEIRecipeTransferHandler() {
    }

    public record TransferContext(
            int containerId,
            ResourceLocation recipeId,
            boolean maxTransfer,
            boolean requireCompleteSets,
            List<Integer> craftingSlotIndexes,
            List<Integer> inventorySlotIndexes
    ) {
    }

    public static <C extends AbstractContainerMenu, R> Optional<TransferContext> prepareClientContext(
            C container,
            R recipe,
            IRecipeSlotsView recipeSlotsView,
            Player player,
            boolean maxTransfer,
            IRecipeTransferInfo<C, R> transferInfo,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots
    ) {
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            return Optional.empty();
        }
        SourceInventory endInv = getClientAttachedInventory(container);
        if (endInv == null) {
            return Optional.empty();
        }
        TransferPlan plan = createTransferPlan(craftingRecipe, craftingSlots, inventorySlots, endInv, maxTransfer);
        if (plan == null || plan.isMissing() || plan.craftsWanted() <= 0) {
            return Optional.empty();
        }
        ResourceLocation recipeId = craftingRecipe.getId();
        boolean requireCompleteSets = transferInfo.requireCompleteSets(container, recipe);
        List<Integer> craftingIndexes = craftingSlots.stream().map(slot -> slot.index).toList();
        List<Integer> inventoryIndexes = inventorySlots.stream().map(slot -> slot.index).toList();
        return Optional.of(new TransferContext(
                container.containerId,
                recipeId,
                maxTransfer,
                requireCompleteSets,
                craftingIndexes,
                inventoryIndexes
        ));
    }

    @Nullable
    private static SourceInventory getClientAttachedInventory(AbstractContainerMenu container) {
        if (EffectiveSide.get() == LogicalSide.SERVER) {
            return null;
        }
        return ClientAccess.getAttachedInventory(container);
    }

    public static void performServerTransfer(
            AbstractContainerMenu container,
            CraftingRecipe recipe,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            ServerPlayer player,
            PageMetaDataManager manager,
            boolean maxTransfer,
            boolean requireCompleteSets
    ) {
        if (manager.getMenu() != container) {
            return;
        }
        SourceInventory endInv = manager.getSourceInventory();
        TransferPlan plan = createTransferPlan(recipe, craftingSlots, inventorySlots, endInv, maxTransfer);
        if (plan == null || plan.isMissing() || plan.craftsWanted() <= 0) {
            return;
        }
        if (requireCompleteSets && plan.craftsWanted() <= 0) {
            return;
        }
        performTransfer(container, plan, player, craftingSlots, inventorySlots, endInv);
        manager.sendEndInvData();
    }

    @Nullable
    public static PageMetaDataManager getServerManager(ServerPlayer player) {
        return ServerLevelEndInv.checkAndGetManagerForPlayer(player).orElse(null);
    }

    private static TransferPlan createTransferPlan(
            CraftingRecipe recipe,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            SourceInventory endInv,
            boolean maxTransfer
    ) {
        Ingredient[] layout = buildRecipeLayout(recipe);
        List<ItemStack> pageItems = endInv.getItemsAsList();
        ItemAvailability availability = ItemAvailability.build(inventorySlots, pageItems);
        Reservation reservation = new Reservation();
        ItemStack[] chosenPerSlot = new ItemStack[layout.length];
        Arrays.fill(chosenPerSlot, ItemStack.EMPTY);
        boolean missing = false;
        int perSlotStackLimit = Integer.MAX_VALUE;

        for (int i = 0; i < layout.length; i++) {
            Ingredient ing = layout[i];
            if (ing.isEmpty()) {
                continue;
            }
            Selection selection = chooseBestCandidate(ing, availability, reservation);
            if (selection.isEmpty()) {
                missing = true;
                continue;
            }
            reservation.reserve(selection);
            chosenPerSlot[i] = selection.stack();
            perSlotStackLimit = Math.min(perSlotStackLimit, selection.stack().getMaxStackSize());
        }

        int craftsPossible;
        if (missing || reservation.isEmpty()) {
            craftsPossible = 0;
        } else {
            craftsPossible = Integer.MAX_VALUE;
            for (Map.Entry<ItemKey, Integer> entry : reservation.totalDemand().entrySet()) {
                ItemCounts counts = availability.lookup(entry.getKey());
                if (counts == null) {
                    craftsPossible = 0;
                    missing = true;
                    break;
                }
                int possible = counts.total() / entry.getValue();
                craftsPossible = Math.min(craftsPossible, possible);
            }
            if (craftsPossible == Integer.MAX_VALUE) {
                craftsPossible = 0;
            }
        }

        if (perSlotStackLimit == Integer.MAX_VALUE) {
            perSlotStackLimit = 64;
        }

        int craftsWanted = maxTransfer ? Math.min(craftsPossible, perSlotStackLimit) : (craftsPossible > 0 ? 1 : 0);
        boolean finalMissing = missing || craftsWanted <= 0;
        return new TransferPlan(layout, chosenPerSlot, craftsWanted, finalMissing);
    }

    private static void performTransfer(
            AbstractContainerMenu container,
            TransferPlan plan,
            Player player,
            List<Slot> craftingSlots,
            List<Slot> inventorySlots,
            SourceInventory endInv
    ) {
        for (Slot cSlot : craftingSlots) {
            if (!cSlot.hasItem()) continue;
            int count = cSlot.getItem().getCount();
            if (count <= 0) continue;
            ItemStack removed = cSlot.safeTake(count, count, player);
            if (!removed.isEmpty()) {
                player.getInventory().placeItemBackInInventory(removed);
            }
        }

        Ingredient[] layout = plan.layout();
        for (int i = 0; i < Math.min(craftingSlots.size(), layout.length); i++) {
            Ingredient ing = layout[i];
            if (ing.isEmpty()) continue;
            Slot target = craftingSlots.get(i);
            int toTake = plan.craftsWanted();
            if (toTake <= 0) continue;

            ItemStack chosen = plan.chosenPerSlot()[i];
            ItemStack placedStack = ItemStack.EMPTY;

            for (Slot invSlot : inventorySlots) {
                if (toTake <= 0) break;
                if (!invSlot.hasItem()) continue;
                ItemStack in = invSlot.getItem();
                if (!(chosen.isEmpty() ? ing.test(in) : sameType(chosen, in))) continue;
                int can = Math.min(toTake, in.getCount());
                if (can <= 0) continue;
                ItemStack taken = invSlot.safeTake(can, can, player);
                if (!taken.isEmpty()) {
                    if (placedStack.isEmpty()) {
                        placedStack = taken;
                    } else if (sameType(placedStack, taken)) {
                        placedStack.grow(taken.getCount());
                    } else {
                        player.getInventory().placeItemBackInInventory(taken);
                        break;
                    }
                    toTake -= taken.getCount();
                }
            }

            if (toTake > 0) {
                ItemStack template = !chosen.isEmpty() ? chosen : (ing.getItems().length > 0 ? ing.getItems()[0] : ItemStack.EMPTY);
                if (!template.isEmpty()) {
                    ItemStack extracted = endInv.takeItem(template, toTake);
                    if (!extracted.isEmpty()) {
                        if (placedStack.isEmpty()) {
                            placedStack = extracted;
                        } else if (sameType(placedStack, extracted)) {
                            placedStack.grow(extracted.getCount());
                        } else {
                            player.getInventory().placeItemBackInInventory(extracted);
                        }
                        toTake -= extracted.getCount();
                    }
                }
            }

            if (!placedStack.isEmpty()) {
                int cap = Math.min(placedStack.getMaxStackSize(), target.getMaxStackSize());
                if (placedStack.getCount() > cap) {
                    ItemStack overflow = placedStack.copyWithCount(placedStack.getCount() - cap);
                    placedStack.setCount(cap);
                    player.getInventory().placeItemBackInInventory(overflow);
                }
                target.set(placedStack);
            }
        }
        container.broadcastChanges();
    }

    private static boolean sameType(ItemStack a, ItemStack b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        if (a.getItem() != b.getItem()) return false;
        var ta = a.getTag();
        var tb = b.getTag();
        return Objects.equals(ta, tb);
    }

    private static Ingredient[] buildRecipeLayout(CraftingRecipe recipe) {
        Ingredient[] layout = new Ingredient[9];
        Arrays.fill(layout, Ingredient.EMPTY);
        if (recipe instanceof ShapedRecipe shaped) {
            int width = Math.min(3, shaped.getWidth());
            int height = Math.min(3, shaped.getHeight());
            List<Ingredient> ingredients = shaped.getIngredients();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int srcIndex = y * shaped.getWidth() + x;
                    if (srcIndex < ingredients.size()) {
                        layout[y * 3 + x] = ingredients.get(srcIndex);
                    }
                }
            }
        } else {
            List<Ingredient> ingredients = recipe.getIngredients();
            for (int i = 0; i < Math.min(ingredients.size(), layout.length); i++) {
                layout[i] = ingredients.get(i);
            }
        }
        return layout;
    }

    private static Selection chooseBestCandidate(Ingredient ing, ItemAvailability availability, Reservation reservation) {
        Candidate best = Candidate.NONE;
        Set<ItemKey> seen = new HashSet<>();

        for (ItemStack cand : ing.getItems()) {
            ItemCounts counts = countAvailableOfType(cand, availability);
            Candidate candidate = Candidate.fromCounts(counts, reservation);
            if (!candidate.isValid()) {
                continue;
            }
            if (!seen.add(candidate.selection().key())) {
                continue;
            }
            best = Candidate.pickBetter(best, candidate);
        }

        for (ItemCounts counts : availability.all()) {
            if (!ing.test(counts.representative())) {
                continue;
            }
            Candidate candidate = Candidate.fromCounts(counts, reservation);
            if (!candidate.isValid()) {
                continue;
            }
            if (!seen.add(candidate.selection().key())) {
                continue;
            }
            best = Candidate.pickBetter(best, candidate);
        }

        return best.selection();
    }

    @Nullable
    private static ItemCounts countAvailableOfType(ItemStack type, ItemAvailability availability) {
        if (type.isEmpty()) {
            return null;
        }
        return availability.lookup(ItemKey.asKey(type));
    }

    private record TransferPlan(Ingredient[] layout, ItemStack[] chosenPerSlot, int craftsWanted, boolean missing) {
        boolean isMissing() {
            return missing;
        }
    }

    private static final class ItemAvailability {
        private final Map<ItemKey, ItemCounts> counts = new HashMap<>();

        private ItemAvailability() {
        }

        static ItemAvailability build(List<Slot> invSlots, List<ItemStack> pageItems) {
            ItemAvailability availability = new ItemAvailability();
            for (Slot slot : invSlots) {
                if (!slot.hasItem()) continue;
                availability.add(slot.getItem(), Source.INVENTORY);
            }
            for (ItemStack stack : pageItems) {
                if (stack.isEmpty()) continue;
                availability.add(stack, Source.PAGE);
            }
            return availability;
        }

        private void add(ItemStack stack, Source source) {
            ItemKey key = ItemKey.asKey(stack);
            ItemCounts counts = this.counts.computeIfAbsent(key, k -> new ItemCounts(k, stack.copyWithCount(1)));
            counts.add(stack.getCount(), source);
        }

        @Nullable
        ItemCounts lookup(ItemKey key) {
            return counts.get(key);
        }

        Collection<ItemCounts> all() {
            return counts.values();
        }

        private enum Source {
            INVENTORY,
            PAGE
        }
    }

    private static final class ItemCounts {
        private final ItemKey key;
        private final ItemStack representative;
        private int inventoryCount;
        private int pageCount;

        ItemCounts(ItemKey key, ItemStack representative) {
            this.key = key;
            this.representative = representative;
        }

        void add(int amount, ItemAvailability.Source source) {
            if (source == ItemAvailability.Source.INVENTORY) {
                inventoryCount += amount;
            } else {
                pageCount += amount;
            }
        }

        ItemKey key() {
            return key;
        }

        ItemStack representative() {
            return representative.copy();
        }

        int total() {
            return inventoryCount + pageCount;
        }

        int totalRemaining(Reservation reservation) {
            return total() - reservation.totalReserved(key);
        }

        int inventoryRemaining(Reservation reservation) {
            return inventoryCount - reservation.inventoryReserved(key);
        }

        boolean isPlain() {
            return key.tag() == null;
        }
    }

    private static final class Reservation {
        private final Map<ItemKey, Integer> total = new HashMap<>();
        private final Map<ItemKey, Integer> inventory = new HashMap<>();

        void reserve(Selection selection) {
            if (selection.key() == null) return;
            total.merge(selection.key(), 1, Integer::sum);
            if (selection.useInventory()) {
                inventory.merge(selection.key(), 1, Integer::sum);
            }
        }

        int totalReserved(ItemKey key) {
            return total.getOrDefault(key, 0);
        }

        int inventoryReserved(ItemKey key) {
            return inventory.getOrDefault(key, 0);
        }

        Map<ItemKey, Integer> totalDemand() {
            return total;
        }

        boolean isEmpty() {
            return total.isEmpty();
        }
    }

    private record Selection(ItemStack stack, ItemKey key, boolean useInventory) {
        static final Selection EMPTY = new Selection(ItemStack.EMPTY, null, false);

        boolean isEmpty() {
            return stack.isEmpty();
        }
    }

    private record Candidate(Selection selection, int priority, int totalRemaining, int inventoryRemaining) {
        private static final Candidate NONE = new Candidate(Selection.EMPTY, -1, 0, 0);

        boolean isValid() {
            return selection != null && !selection.isEmpty();
        }

        static Candidate fromCounts(@Nullable ItemCounts counts, Reservation reservation) {
            if (counts == null) {
                return NONE;
            }
            int totalRemaining = counts.totalRemaining(reservation);
            if (totalRemaining <= 0) {
                return NONE;
            }
            int inventoryRemaining = counts.inventoryRemaining(reservation);
            boolean hasInventory = inventoryRemaining > 0;
            boolean hasPlainInventory = hasInventory && counts.isPlain();
            int priority = hasPlainInventory ? 3 : (hasInventory ? 2 : 1);
            Selection selection = new Selection(counts.representative(), counts.key(), hasInventory);
            return new Candidate(selection, priority, totalRemaining, inventoryRemaining);
        }

        static Candidate pickBetter(Candidate current, Candidate challenger) {
            if (!challenger.isValid()) {
                return current;
            }
            if (!current.isValid()) {
                return challenger;
            }
            if (challenger.priority != current.priority) {
                return challenger.priority > current.priority ? challenger : current;
            }
            if (challenger.totalRemaining != current.totalRemaining) {
                return challenger.totalRemaining > current.totalRemaining ? challenger : current;
            }
            if (challenger.inventoryRemaining != current.inventoryRemaining) {
                return challenger.inventoryRemaining > current.inventoryRemaining ? challenger : current;
            }
            return current;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static class ClientAccess {
        @Nullable
        private static SourceInventory getAttachedInventory(AbstractContainerMenu container) {
            var attachment = ScreenAttachment.ATTACHMENT_MANAGER;
            if (attachment == null) {
                return null;
            }
            if (Minecraft.getInstance().player == null) {
                return null;
            }
            if (attachment.getScreen().getMenu() != container) {
                return null;
            }
            return attachment.getPageManager().getSourceInventory();
        }
    }
}
