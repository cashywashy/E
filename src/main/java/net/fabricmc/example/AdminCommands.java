package net.fabricmc.example;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.example.finders.BlockEntityFinder;
import net.fabricmc.example.finders.EntityFinder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.NbtText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AdminCommands {
    public static int peepEnderChest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player2 = context.getArgument("player", ServerPlayerEntity.class);
        ServerPlayerEntity owner = context.getSource().getPlayer();
        EnderChestInventory enderChestInventory = player2.getEnderChestInventory();

        owner.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, enderChestInventory), player2.getName()));


        return 0;
    }

    public static int peepInventory(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerPlayerEntity player2 = EntityArgumentType.getPlayer(context, "player");
        PlayerInventory playerInventory = player2.getInventory();
        ServerPlayerEntity owner = context.getSource().getPlayer();


        SimpleNamedScreenHandlerFactory menu = new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            Inventory viewInv = new Inventory() {
                @Override
                public int size() {
                    return 54;
                }

                @Override
                public boolean isEmpty() {
                    return playerInventory.isEmpty();
                }

                /**
                 * Adjusts the view slot index to the {@link PlayerInventory } slot index.
                 *
                 * @param slot the view slot index.
                 * @return the {@link PlayerInventory} slot index, or -1 if the view slot does not have a corresponding real slot.
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
            return GenericContainerScreenHandler.createGeneric9x6(syncId, inventory, viewInv);

        }, player2.getName());


        owner.openHandledScreen(menu);

        return 0;
    }

    public static int peepChest(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity owner = context.getSource().getPlayer();
        DefaultedList<ItemStack> menuSlots = DefaultedList.ofSize(54, ItemStack.EMPTY);

        try {
            List<ChestBlockEntity> blockEntityList = (List<ChestBlockEntity>) BlockEntityFinder.find(owner.getWorld(), owner, BlockEntityType.CHEST);
            List<ChestMinecartEntity> entityList = (List<ChestMinecartEntity>) (EntityFinder.find(owner.getWorld(), owner, EntityType.CHEST_MINECART));

            for (int i = 0; i < menuSlots.size(); i++){
                if (blockEntityList.size() > i){
                    NbtCompound compound = new NbtCompound();
                    BlockPos pos = blockEntityList.get(i).getPos();
                    compound.putIntArray("position" , new int[]{pos.getX(), pos.getY(), pos.getZ()});

                    NbtCompound display = new NbtCompound();
                    NbtList lore = new NbtList();

                    lore.add(NbtString.of("\"KILL ME NOW\"".replace("KILL ME NOW", pos.toString())));

                    display.put("Lore", lore);
                    compound.put("display", display);

                    ItemStack stack = Items.CHEST.getDefaultStack();
                    stack.setNbt(compound);
                    stack.setCustomName(blockEntityList.get(i).getCustomName());

                    menuSlots.set(i, stack);
                }
                else if (entityList.size() > i-blockEntityList.size()){

                    NbtCompound compound = new NbtCompound();
                    int id = entityList.get(i- blockEntityList.size()).getId();
                    compound.putInt("chestCart" , id);

                    NbtCompound display = new NbtCompound();
                    NbtList lore = new NbtList();

                    lore.add(NbtString.of(String.valueOf(id)));

                    display.put("Lore", lore);
                    compound.put("display", display);

                    ItemStack stack = Items.CHEST_MINECART.getDefaultStack();
                    stack.setNbt(compound);

                    menuSlots.set(i, stack);
                }
                else {
                    menuSlots.set(i, ItemStack.EMPTY);
                }
            }
            Inventory menu = new Inventory() {
                int size = menuSlots.size();
                DefaultedList<ItemStack> slots = menuSlots;

                @Override
                public int size() {
                    return this.size;
                }

                @Override
                public boolean isEmpty() {
                    return this.slots.isEmpty();
                }

                @Override
                public ItemStack getStack(int slot) {
                    return this.slots.get(slot);
                }

                @Override
                public ItemStack removeStack(int slot, int amount) {
                    return ItemStack.EMPTY;
                }

                @Override
                public ItemStack removeStack(int slot) {
                    return ItemStack.EMPTY;
                }

                @Override
                public void setStack(int slot, ItemStack stack){
                    this.slots.set(slot,stack);
                }

                @Override
                public void markDirty() {

                }

                @Override
                public boolean canPlayerUse(PlayerEntity player) {
                    return true;
                }

                @Override
                public void clear() {
                    this.slots.clear();
                }

                @Override
                public boolean isValid(int slot, ItemStack stack) {
                    return false;
                }
            };


            owner.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> GenericContainerScreenHandler.createGeneric9x6(syncId, inv, menu), Text.of("Nearby Containers")));

        } catch (ExecutionException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }


        return 0;
    }

}
