package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;

public class Init implements ModInitializer {
    public static final String MOD_ID = "modid";

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                dispatcher.register(CommandManager.literal("peepEc")
                                .requires(source -> source.hasPermissionLevel(4))
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(AdminCommands::peepEnderChest)
        )));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) ->
                dispatcher.register(CommandManager.literal("peepInv")
                        .requires(source -> source.hasPermissionLevel(4))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(AdminCommands::peepInventory)
                        )));


    }

}
