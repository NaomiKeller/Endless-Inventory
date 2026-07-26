package com.kwwsyk.endinv.neoforge.integrates.jei;

import com.kwwsyk.endinv.common.client.gui.AttachingScreen;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.neoforge.client.events.ScreenAttachment;
import com.mojang.logging.LogUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IClickableIngredientFactory;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public class AttachmentGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {

    static final Logger LOGGER = LogUtils.getLogger();

    public AttachmentGuiHandler(){}

    @Override
    public List<Rect2i> getGuiExtraAreas(AbstractContainerScreen<?> containerScreen) {
        AttachingScreen<?> attachedScreen = ScreenAttachment.ATTACHMENT_MANAGER;
        if(attachedScreen!=null){
            return attachedScreen.getArea();
        }
        return IGuiContainerHandler.super.getGuiExtraAreas(containerScreen);
    }

    /**
     * Return a clickable ingredient under the mouse that JEI could not normally detect, used for JEI recipe lookups.
     * <p>
     * This is useful for guis that don't have normal slots (which is how JEI normally detects items under the mouse).
     * <p>
     * This can also be used to let JEI look up liquids in tanks directly, by returning a FluidStack.
     * Works with any ingredient type that has been registered with {@code IModIngredientRegistration}.
     *
     * @param builder
     * @param containerScreen
     * @param mouseX          the current X position of the mouse in screen coordinates.
     * @param mouseY          the current Y position of the mouse in screen coordinates.
     * @since 19.23.0
     */
    @Override
    public Optional<? extends IClickableIngredient<?>> getClickableIngredientUnderMouse(
            IClickableIngredientFactory builder,
            AbstractContainerScreen<?> containerScreen,
            double mouseX, double mouseY
    ) {
        var sf = ScreenFramework.getInstance();
        if (sf == null) return Optional.empty();

        var page = sf.getDisplayingPage();

        ItemStack hovered = page.getHoveredOrClickedItem(mouseX, mouseY);
        if (hovered == null || hovered.isEmpty()) return Optional.empty();

        Rect2i area = page.getInteractableAreaUnderMouse(mouseX, mouseY);
        if (area == null) return Optional.empty();

        return Optional.of(new ItemClickEventWrapper(hovered, area));
    }

    public static class ItemClickEventWrapper implements IClickableIngredient<ItemStack> {
        private final ItemStack hovered;
        private final Rect2i area;

        public ItemClickEventWrapper(ItemStack hovered, Rect2i area) {
            this.hovered = hovered;
            this.area = area;
        }

        @Override @SuppressWarnings("removal")
        public IIngredientType<ItemStack> getIngredientType() { return VanillaTypes.ITEM_STACK; }
        @Override @SuppressWarnings("removal")
        public ItemStack getIngredient() { return hovered; }
        @Override public Rect2i getArea() { return area; }

        @Override @Deprecated @SuppressWarnings({"nonextendable"})
        public ITypedIngredient<ItemStack> getTypedIngredient() {
            return new ITypedIngredient<>() {
                @Override public IIngredientType<ItemStack> getType() { return VanillaTypes.ITEM_STACK; }
                @Override public ItemStack getIngredient() { return hovered; }
            };
        }
    }
}
