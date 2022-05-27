package net.fabricmc.example;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdminCommands {
    public static int peepEnderChest(CommandContext context){
        try {
            ServerPlayerEntity player2 = EntityArgumentType.getPlayer(context, "player");
            EnderChestInventory enderChestInventory = player2.getEnderChestInventory();

            player2.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, enderChestInventory), player2.getName()));

        } catch (CommandSyntaxException e) {
            // TODO: shout at caller
            e.printStackTrace();
        }


        return 0;
    }
    public static int peepInventory(CommandContext context){
        try {
            ServerPlayerEntity player2 = EntityArgumentType.getPlayer(context, "player");
            PlayerInventory playerInventory = player2.getInventory();
            playerInventory.onOpen(player2);
//            player2.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> GenericContainerScreenHandler.createGeneric9x6(syncId, inventory, playerInventory), player2.getName()));
//            player2.openHandledScreen(SimpleNamedScreenHandlerFactory)
        } catch (CommandSyntaxException e) {
            // TODO: shout at caller
            e.printStackTrace();
        }


        return 0;
    }
}
