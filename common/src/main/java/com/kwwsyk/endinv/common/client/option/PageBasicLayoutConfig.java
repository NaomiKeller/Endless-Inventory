package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.options.config.ComplexConfigEntryImpl;
import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;

/*Left and right mode options*/

/**Controls pages' layout and location in AS.
 *
 */
public class PageBasicLayoutConfig extends ComplexConfigEntryImpl<PageBasicLayoutConfig.Param> {

    public final ConfigEntryImpl<Integer> Rows = new ConfigEntryImpl.IntEntry(
            "rows",
            new String[]{"The default rows of page, if auto suit rows, it's the max row count."},
            15,0,255
    );
    public final ConfigEntryImpl<Integer> Columns = new ConfigEntryImpl.IntEntry(
            "columns",
            new String[]{"The default columns of page, if auto suit columns, it's the max column count."},
            9,0,255
    );
    public final ConfigEntryImpl<Integer> yOffset = new ConfigEntryImpl.IntEntry(
            "yOffset",
            new String[]{"The vertical offset of page to original position of Attached Menu."},
            0,Integer.MIN_VALUE,Integer.MAX_VALUE
    );
    public final ConfigEntryImpl<Integer> xOffset = new ConfigEntryImpl.IntEntry(
            "xOffset",
            new String[]{"The horizontal offset of page to original position of Attached Menu."},
            0,Integer.MIN_VALUE,Integer.MAX_VALUE
    );
    public final ConfigEntryImpl<Boolean> autoRows = new ConfigEntryImpl.BooleanEntry(
            "auto suit row count",
            new String[]{"Auto suit row count based on the GuiSize in case the page exceeded the gui screen."},
            true
    );
    public final ConfigEntryImpl<Boolean> autoColumns = new ConfigEntryImpl.BooleanEntry(
            "auto suit column count",
            new String[]{"Auto suit column count based on the GuiSize in case the page covered the menu or exceeded the gui screen."},
            true
    );

    public PageBasicLayoutConfig(String key, String[] comments, Param defaultValue) {
        super(key, comments, defaultValue);
    }

    /**
     * The getter of a complex config entry may be null.
     * Let it return {@link #fields()}'s returns.
     *
     * @return the default value of the complex config entry.
     */
    @Override
    public Param get() {
        return new Param(Rows.get(), Columns.get(), yOffset.get(), xOffset.get(), autoRows.get(), autoColumns.get());
    }

    /**
     * The setter of a complex config entry may be null.
     * Let it set every field of {@link #fields()}.
     *
     * @param param to be used to set fields.
     */
    @Override
    public void set(Param param) {
        Rows.set(param.rows());
        Columns.set(param.columns());
        yOffset.set(param.yOffset());
        xOffset.set(param.xOffset());
        autoRows.set(param.autoRows());
        autoColumns.set(param.autoColumns());
        save();
    }

    @Override
    public ConfigEntryImpl<?>[] fields() {
        return new ConfigEntryImpl[]{
                Rows, Columns, yOffset, xOffset, autoRows, autoColumns
        };
    }

    public record Param(int rows, int columns, int yOffset, int xOffset, boolean autoRows, boolean autoColumns){
        public static final Param DEFAULT = new Param(15,9,0,0,true,true);
    }
}
