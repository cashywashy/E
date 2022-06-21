package net.fabricmc.example;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class InventoryPeeper extends GenericContainerScreenHandler {
/**
 * this is for identifying the inventory peeper in the screenhandler mixin. don't ask why I didn't just use {@link PeeperScreenHandler}. I'm not sure myself.
 * @param syncId I'm too tired to document this. I'll do it tomorrow
 **/
    public InventoryPeeper(int syncId, PlayerInventory playerInventory, Inventory inventory){
        super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
    }
}
