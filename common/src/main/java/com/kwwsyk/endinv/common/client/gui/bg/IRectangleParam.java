package com.kwwsyk.endinv.common.client.gui.bg;

import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;

import java.util.List;

public interface IRectangleParam {
    int XPos();

    int YPos();

    int XSize();

    int YSize();

    boolean hasClickedOn(int mouseX, int mouseY);

    static ConfigEntryImpl.ListEntry<Integer> createRectangleConfigEntry(String key, String[] comments, IRectangleParam defaultValue){
        return new ConfigEntryImpl.ListEntry<Integer>(
                key,
                comments,
                List.of(
                        defaultValue.XPos(),
                        defaultValue.YPos(),
                        defaultValue.XSize(),
                        defaultValue.YSize()
                ),
                ()->0,
                n -> n instanceof Integer
        ).setRange(4,4);
    }

    static IRectangleParam fromConfigEntry(ConfigEntryImpl.ListEntry<Integer> entry){
        List<Integer> list = entry.get();
        if(list.size()<4) throw new IllegalArgumentException("Invalid rectangle config, which provides a list with less than 4 size.");
        return new ScreenRectangleWidgetParam(list.get(0),list.get(1),list.get(2),list.get(3));
    }
}
