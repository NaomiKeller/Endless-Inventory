package com.kwwsyk.endinv.fabric.event;

import com.kwwsyk.endinv.common.commands.EndInvCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class Commands {

    private Commands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> EndInvCommand.register(dispatcher));
    }
}
