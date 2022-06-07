package net.fabricmc.example;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdminCommands {
    public static int peepEnderChest(CommandContext context) {
        try {
            ServerPlayerEntity player2 = EntityArgumentType.getPlayer(context, "player");
            ServerPlayerEntity owner = ((ServerCommandSource)context.getSource()).getPlayer();
            EnderChestInventory enderChestInventory = player2.getEnderChestInventory();



            owner.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, enderChestInventory), player2.getName()));

        } catch (CommandSyntaxException e) {
            // TODO: shout at caller
            e.printStackTrace();
        }


        return 0;
    }

    public static int peepInventory(CommandContext context) {
        try {
            ServerPlayerEntity player2 = EntityArgumentType.getPlayer(context, "player");
            PlayerInventory playerInventory = player2.getInventory();
            ServerPlayerEntity owner = ((ServerCommandSource)context.getSource()).getPlayer();
            playerInventory.onOpen(player2);

            SimpleNamedScreenHandlerFactory menu = new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
                Inventory e = new Inventory() {
                    @Override
                    public int size() {
                        return 54;
                    }

                    @Override
                    public boolean isEmpty() {
                        return playerInventory.isEmpty();
                    }

                    /**
                     * Adjusts the view slot index to the {@link PlayerInventory.combinedInventory} slot index.
                     *
                     * @param slot the view slot index.
                     * @return the {@link PlayerInventory.combinedInventory} slot index, or -1 if the view slot does not have a corresponding real slot.
                     */
                    public int getSlot(int slot) {
                        if (slot < playerInventory.main.size()) {
                            return slot;
                        }

                        // remove padding row.
                        slot -= 9;

                        if (slot >= playerInventory.main.size() && slot < playerInventory.main.size() + playerInventory.armor.size()) {
                            return slot;
                        }

                        // remove offhand alignment to the right side of the container.
                        slot -= 4;

                        if (slot == playerInventory.main.size() + playerInventory.armor.size()) {
                            return slot;
                        }

                        return -1;
                    }

                    @Override
                    public ItemStack getStack(int slot) {
                        slot = getSlot(slot);
                        return slot == -1 ? ItemStack.EMPTY : playerInventory.getStack(slot);
                    }

                    @Override
                    public ItemStack removeStack(int slot, int amount) {
                        slot = getSlot(slot);
                        return slot == -1 ? ItemStack.EMPTY : playerInventory.removeStack(slot, amount);
                    }

                    @Override
                    public ItemStack removeStack(int slot) {
                        slot = getSlot(slot);
                        return slot == -1 ? ItemStack.EMPTY : playerInventory.removeStack(slot);
                    }

                    @Override
                    public void setStack(int slot, ItemStack stack) {
                        slot = getSlot(slot);
                        if (slot != -1) {
                            playerInventory.setStack(slot, stack);
                        }
                    }

                    @Override
                    public void markDirty() {
                        playerInventory.markDirty();
                    }

                    @Override
                    public boolean canPlayerUse(PlayerEntity player) {
                        return true;
                    }

                    @Override
                    public void clear() {
                        playerInventory.clear();
                    }

                    @Override
                    public boolean isValid(int slot, ItemStack stack) {
                        return getSlot(slot) != -1;
                    }
                };
                return GenericContainerScreenHandler.createGeneric9x6(syncId, inventory, e);

            }, player2.getName());


            owner.openHandledScreen(menu);

        } catch (CommandSyntaxException e) {
            // TODO: shout at caller
            e.printStackTrace();
        }


        return 0;
    }
}
