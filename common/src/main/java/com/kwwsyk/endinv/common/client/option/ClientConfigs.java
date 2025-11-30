package com.kwwsyk.endinv.common.client.option;

import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;

import java.util.List;

public final class ClientConfigs {

    private ClientConfigs(){}

    private static final List<ConfigEntryImpl<?>> configs = List.of();

    public static List<ConfigEntryImpl<?>> getConfigs(){
        return configs;
    }


}
