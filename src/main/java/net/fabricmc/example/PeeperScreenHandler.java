package net.fabricmc.example;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class PeeperScreenHandler extends GenericContainerScreenHandler {
/**
 * this class was made for the sole purpose of identifying the screenhandler of the chestpeeper in the screenhandler mixin.
 * @param syncId I don't understand it very much myself, but from my knowledge, it's an id that syncs the user with the chest?
 * @param playerInventory the inventory of the player
 * @param inventory the other inventory that isn't the player's inventory
 **/
    public PeeperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
    }
}
