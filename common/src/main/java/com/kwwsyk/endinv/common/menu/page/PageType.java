package com.kwwsyk.endinv.common.menu.page;

import com.kwwsyk.endinv.common.ModInfo;
import com.kwwsyk.endinv.common.client.gui.page.*;
import com.kwwsyk.endinv.common.client.gui.page.manager.PageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static net.minecraft.tags.ItemTags.*;

@SuppressWarnings("removal")
public class PageType {

    public static final String DEFAULT_KEY = "all_items";

    //segments shown (in order) on the combined Gear page: weapons, tools, then armor pieces and shield/elytra
    private static final List<Predicate<ItemStack>> gearSubclassifications = List.of(
            PageType::isWeapon,
            PageType::isTool,
            is-> is.getItem() instanceof ArmorItem armor && armor.getType()==ArmorItem.Type.HELMET,
            is-> is.getItem() instanceof ArmorItem armor && armor.getType()==ArmorItem.Type.CHESTPLATE,
            is-> is.getItem() instanceof ArmorItem armor && armor.getType()==ArmorItem.Type.LEGGINGS,
            is-> is.getItem() instanceof ArmorItem armor && armor.getType()==ArmorItem.Type.BOOTS,
            is-> is.getItem() instanceof ShieldItem || is.getItem() instanceof ElytraItem
    );

    //substrings matched against an item's registry path (e.g. "cobblestone", "polished_andesite")
    private static final String[] STONE_KEYWORDS = {
            "stone","andesite","diorite","granite","deepslate","blackstone","basalt","tuff","calcite","dripstone"
    };
    private static final String[] WOOD_KEYWORDS = {
            "oak","spruce","birch","jungle","acacia","dark_oak","mangrove","cherry","bamboo","crimson","warped"
    };

    //curated rather than name-matched: material keywords like "iron"/"gold"/"diamond" also appear in
    //unrelated items (golden_apple, diamond_sword, iron_horse_armor), so those would be misclassified
    private static final Set<Item> ORE_AND_MINERAL_ITEMS = Set.of(
            Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE,
            Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE,
            Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE,
            Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.NETHER_GOLD_ORE,
            Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE,
            Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE,
            Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE,
            Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE,
            Items.NETHER_QUARTZ_ORE, Items.ANCIENT_DEBRIS,
            Items.RAW_IRON, Items.RAW_GOLD, Items.RAW_COPPER,
            Items.RAW_IRON_BLOCK, Items.RAW_GOLD_BLOCK, Items.RAW_COPPER_BLOCK,
            Items.IRON_INGOT, Items.IRON_NUGGET,
            Items.GOLD_INGOT, Items.GOLD_NUGGET,
            Items.COPPER_INGOT,
            Items.NETHERITE_INGOT, Items.NETHERITE_SCRAP,
            Items.DIAMOND, Items.EMERALD, Items.LAPIS_LAZULI, Items.REDSTONE, Items.COAL, Items.CHARCOAL,
            Items.QUARTZ, Items.AMETHYST_SHARD, Items.GLOWSTONE_DUST,
            Items.IRON_BLOCK, Items.GOLD_BLOCK, Items.COPPER_BLOCK, Items.DIAMOND_BLOCK,
            Items.EMERALD_BLOCK, Items.LAPIS_BLOCK, Items.REDSTONE_BLOCK, Items.COAL_BLOCK, Items.NETHERITE_BLOCK,
            Items.AMETHYST_BLOCK, Items.BUDDING_AMETHYST, Items.AMETHYST_CLUSTER,
            Items.EXPOSED_COPPER, Items.WEATHERED_COPPER, Items.OXIDIZED_COPPER,
            Items.WAXED_COPPER_BLOCK, Items.WAXED_EXPOSED_COPPER, Items.WAXED_WEATHERED_COPPER, Items.WAXED_OXIDIZED_COPPER
    );

    public static final List<TagKey<Item>> WEAPON_TAGS = new ArrayList<>();
    public static final List<TagKey<Item>> TOOL_TAGS = new ArrayList<>();
    public static final List<TagKey<Item>> EQUIPPABLE_TAGS = new ArrayList<>();

    public static final PageType ALL_ITEMS = createClassifiedPage(DEFAULT_KEY,null,"chest");
    public static final PageType BLOCK_ITEMS = createClassifiedPage("block_items",PageType::isSolidBlock,"grass_block");
    public static final PageType MOD_ITEMS = createClassifiedPage("mod_items",PageType::isModItem,"structure_block");
    public static final PageType GEAR = new PageType(
            (type,manager)->new SegClassifyItemDisplay(type,manager,gearSubclassifications,false,true),
            "gear",is-> isWeapon(is)||isTool(is)||isDefenceEquipment(is),new ResourceLocation("minecraft","iron_sword")
    );
    public static final PageType STONE = createClassifiedPage("stone",PageType::isStoneMaterial,"stone");
    public static final PageType WOOD = createClassifiedPage("wood",PageType::isWoodMaterial,"oak_log");
    public static final PageType ORES_AND_MINERALS = createClassifiedPage("ores_and_minerals",is->ORE_AND_MINERAL_ITEMS.contains(is.getItem()),"diamond");
    public static final PageType CONSUMABLE = createClassifiedPage("consumable",PageType::isFoodOrPotion,"bread");
    public static final PageType ENCHANTED_BOOKS = createItemEntry("enchanted_books",stack->stack.getItem() instanceof EnchantedBookItem,"enchanted_book");
    public static final PageType BOOKMARK = new PageType(StarredItemPage::new,"bookmark",null,new ResourceLocation("minecraft","book"));

    private final PageConstructor constructor;
    @Nullable
    public final Predicate<ItemStack> itemClassify;
    public ResourceLocation icon = null;
    public final String registerName;

    @FunctionalInterface
    public interface PageConstructor {
        /**
         * DisplayPage's constructor or it's variation.
         * Called by {@link com.kwwsyk.endinv.common.client.gui.ScreenFramework}'s constructor
         *
         * @since 1.1.0
         *
         * @param pageType registered page type
         * @param manager since 1.1.0, it's always ScreenFramework/
         * @return
         */
        DisplayPage create(PageType pageType, PageManager manager);
    }

    public PageType(PageConstructor constructor, String registerName){
        this.constructor = constructor;
        this.registerName = registerName;
        this.itemClassify = null;
    }

    public PageType(PageConstructor constructor, String registerName,@Nullable Predicate<ItemStack> itemClassify){
        this.constructor = constructor;
        this.itemClassify = itemClassify;
        this.registerName = registerName;
    }

    public PageType(PageConstructor constructor, String registerName,@Nullable Predicate<ItemStack> itemClassify,@Nullable ResourceLocation icon){
        this.constructor = constructor;
        this.itemClassify = itemClassify;
        this.icon = icon;
        this.registerName = registerName;
    }

    public static PageType createClassifiedPage(String registerName, Predicate<ItemStack> itemClassify, String icon){
        return new PageType(ItemDisplay::new,registerName,itemClassify,new ResourceLocation("minecraft", icon));
    }

    public static PageType createItemEntry(String registerName, Predicate<ItemStack> itemClassify, String icon){
        return new PageType(ItemEntryDisplay::new,registerName,itemClassify, new ResourceLocation("minecraft", icon));
    }

    /**
     * Build new DisplayPage.<p>
     * Called by ScreenFramework's constructor on client.
     *
     * @param meta
     * @return
     */
    public DisplayPage buildPage(PageManager meta){
        var page =  constructor.create(this, meta);
        if(icon!=null) page.icon = icon;
        return page;
    }

    public String toString(){
        return this.registerName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PageType pageType
                && Objects.equals(pageType.constructor,constructor)
                && Objects.equals(pageType.itemClassify, itemClassify)
                && Objects.equals(pageType.registerName, registerName);
    }

    private static boolean isWeapon(ItemStack itemStack){
        Item item = itemStack.getItem();
        return item instanceof SwordItem ||
                item instanceof  AxeItem ||
                item instanceof  TridentItem ||
                item instanceof ProjectileWeaponItem ||
                WEAPON_TAGS.stream().anyMatch(itemStack::is);
    }

    private static boolean isTool(ItemStack itemStack){
        Item item = itemStack.getItem();
        return item instanceof PickaxeItem ||
                item instanceof AxeItem ||
                item instanceof ShearsItem ||
                item instanceof ShovelItem ||
                item instanceof FlintAndSteelItem ||
                item instanceof FishingRodItem ||
                TOOL_TAGS.stream().anyMatch(itemStack::is);
    }

    private static boolean isDefenceEquipment(ItemStack itemStack){
        Item item = itemStack.getItem();
        return item instanceof ArmorItem ||
                item instanceof ShieldItem ||
                item instanceof ElytraItem ||
                EQUIPPABLE_TAGS.stream().anyMatch(itemStack::is);
    }

    private static boolean isFoodOrPotion(ItemStack itemStack){
        Item item = itemStack.getItem();
        return item instanceof PotionItem ||
                itemStack.isEdible();
    }

    //requiring BlockItem excludes stone tools/weapons (stone_sword, stone_pickaxe, ...), which are
    //plain Items, not BlockItems, without needing to special-case every one of them here
    private static boolean isStoneMaterial(ItemStack itemStack){
        if(!(itemStack.getItem() instanceof BlockItem) || ORE_AND_MINERAL_ITEMS.contains(itemStack.getItem())) return false;
        String path = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
        for(String keyword : STONE_KEYWORDS){
            if(path.contains(keyword)) return true;
        }
        return false;
    }

    private static boolean isWoodMaterial(ItemStack itemStack){
        String path = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getPath();
        for(String keyword : WOOD_KEYWORDS){
            if(path.contains(keyword)) return true;
        }
        return false;
    }

    /**
     * A full, opaque 1x1 cube: excludes thin/partial shapes (slabs, stairs, fences, panes, carpets,
     * pressure plates, ...) via the collision/outline shape, and excludes see-through blocks
     * (glass, leaves, ice, ...) via canOcclude(), since those are marked noOcclusion() in vanilla
     * specifically because they don't render as solid.
     */
    private static boolean isSolidBlock(ItemStack itemStack){
        if(!(itemStack.getItem() instanceof BlockItem blockItem)) return false;
        BlockState state = blockItem.getBlock().defaultBlockState();
        return state.canOcclude() && Block.isShapeFullBlock(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
    }

    private static boolean isModItem(ItemStack itemStack){
        String namespace = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).getNamespace();
        return !namespace.equals("minecraft") && !namespace.equals(ModInfo.MOD_ID);
    }

    static {
        WEAPON_TAGS.add(SWORDS);
        WEAPON_TAGS.add(AXES);
        TOOL_TAGS.add(AXES);
        TOOL_TAGS.add(PICKAXES);
        TOOL_TAGS.add(HOES);
        TOOL_TAGS.add(SHOVELS);
    }
}
