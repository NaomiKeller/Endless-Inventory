package com.kwwsyk.endinv.neoforge.client;

import com.kwwsyk.endinv.common.client.CachedSrcInv;
import com.kwwsyk.endinv.common.client.gui.ScreenFramework;
import com.kwwsyk.endinv.common.client.gui.page.CommonFluidPage;
import com.kwwsyk.endinv.common.integrate.FluidData;
import com.kwwsyk.endinv.common.menu.page.PageType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FluidPage extends CommonFluidPage<FluidStack> {

    public FluidPage(PageType pageType, ScreenFramework framework) {
        super(pageType, framework);
    }

    /**
     * Should return a static FluidStack instance like FluidStack#EMPTY
     *
     * @return a nonnull static object representing empty fluid stack
     */
    @Override
    protected @NotNull FluidStack unit() {
        return FluidStack.EMPTY;
    }

    private FluidStack convert(FluidData data){
        try {
            Holder<Fluid> fluidHolder = BuiltInRegistries.FLUID.getValue(ResourceLocation.parse(data.id())).builtInRegistryHolder();
            return new FluidStack(fluidHolder, (int)data.amountMB(), data.component());
        } catch (Exception e) {
            return FluidStack.EMPTY;
        }
    }

    @Override
    protected void refreshFluids() {
        //todo request fluids
        var src = new ArrayList<>(CachedSrcInv.INSTANCE.affinities.fluids);
        var size = src.size();
        for(int i = startIndex, j = 0; i < startIndex + length && i < size; i++, j++){
            this.fluids.set(j, convert(src.get(i)));
        }
    }

    @Override
    public void renderPage(GuiGraphics graphics) {

    }


    /**
     * Extract fluid from fluidContainer.
     *
     * @param fluidContainer if empty, let page select container, default is {@link net.minecraft.world.item.Items#BUCKET}.
     * @return extracted fluid in the container, or empty if failed.
     */
    @Override
    public ItemStack extractFluid(ItemStack fluidContainer) {
        return null;
    }

    /**
     * Invoked when carrying an item on the blank part of the fluid page.
     * Insert fluid into EndInv from fluidContainer. If the item is not a fluidContainer, perform {@link #tryInsertItem(ItemStack)}
     *
     * @param fluidContainer the itemStack carrying and to perform insert action with the fluid page.
     * @param button         1 for put all, 2 for put 1 Block fluid.
     * @return the container remained after insert.
     */
    @Override
    public ItemStack insertFluidAction(ItemStack fluidContainer, int button) {
        return null;
    }

    @Override
    protected void handlePickup(FluidStack clicked, ItemStack carried, int keyCode) {

    }

    @Override
    protected void handleQuickMove(FluidStack clicked, ItemStack carried, int keyCode) {

    }

    @Override
    protected void handleSwap(FluidStack clicked, ItemStack carried, int inventorySlotId) {

    }

    @Override
    protected void handleThrow(FluidStack clicked, ItemStack carried, int button) {

    }

    /**
     * handle creative clone fluid, equals to extract fluid without consume EndInv's fluid
     *
     * @param clicked should not be empty for creative clone
     * @param carried should be empty for creative clone
     * @param button  ignore (default to 3)
     */
    @Override
    protected void handleClone(FluidStack clicked, ItemStack carried, int button) {

    }

    /**
     * Collect all the same fluids in inventory's fluid containers..?
     *
     * @param clicked to collect
     * @param carried should be empty for pickup all
     * @param keyCode ignore (default to 0)
     */
    @Override
    protected void handlePickUpAll(FluidStack clicked, ItemStack carried, int keyCode) {

    }

    /**
     * Get mouse hovered or clicked item by mouse offset.
     *
     * @param XOffset mouseX-pageX
     * @param YOffset mouseY-pageY
     * @return hovered or clicked item
     */
    @Override
    public ItemStack getItemByMouseOffset(double XOffset, double YOffset) {
        return null;
    }

    @Override
    public void handleStarItem(double XOffset, double YOffset) {

    }
}
