package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;

import javax.annotation.Nullable;
import java.util.Objects;

public class Init implements ModInitializer {
    // this has no practical purpose right now, but it's here just in case.
    public static final String MOD_ID = "chadmin";

    /**
     * The method that happens whenever the mod is initialized... I think.
    **/
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
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) ->
                dispatcher.register(CommandManager.literal("peepChest")
                        .requires(source -> source.hasPermissionLevel(4))
                        .executes(AdminCommands::peepChest)
                )));
    }

}
