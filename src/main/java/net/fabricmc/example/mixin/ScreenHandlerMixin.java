package net.fabricmc.example.mixin;

import net.fabricmc.example.InventoryPeeper;
import net.fabricmc.example.PeeperScreenHandler;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow
    protected abstract void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player);

    /**
     * added in a thing where if the screenhandler is a {@link PeeperScreenHandler}, things are treated differently.
     * @param instance literally the instance of the object that we're redirecting.
     * @param slotIndex the index that was clicked
     * @param button to be completely honest, I'm not actually sure what this is. If I used to know, then I don't know anymore. Unfortunately, I can't just read minecraft's original documentation because it straight up doesn't have any.
     * @param actionType the type of action that the player is making.
     * @param player the player that's doing the thing
     **/
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), method = "onSlotClick")
    public void onSlotClickRedirect(ScreenHandler instance, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        // this means the method will function normally if it's just a normal screenHandler.
        if (player.getWorld().isClient || !(instance instanceof PeeperScreenHandler)|| !(instance instanceof InventoryPeeper)) {

            internalOnSlotClick(slotIndex, button, actionType, player);
            return;
        }
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

        if (slotIndex != -999 && slotIndex != -1) {
            Slot slot = instance.slots.get(slotIndex);
            ItemStack stack = slot.getStack();
            if (stack.hasNbt()) {
                NbtCompound compound = stack.getNbt();
                if (compound.contains("position", NbtElement.INT_ARRAY_TYPE)) {
                    int[] nbt = compound.getIntArray("position");
                    BlockPos pos = new BlockPos(nbt[0], nbt[1], nbt[2]);
                    ServerWorld world = serverPlayer.getWorld();

                    // if it's a chest then you get to check out its contents and screw around with it.
                    if (world.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player1) -> {
                            ScreenHandlerType type = chest.createMenu(syncId, inv, player1).getType();
                            GenericContainerScreenHandler screenHandler = new GenericContainerScreenHandler(type, syncId, inv, chest, chest.size() / 9) {
                                @Override
                                public boolean canUse(PlayerEntity player) {
                                    return true;
                                }
                            };

                            return screenHandler;
                        }, chest.hasCustomName() ? chest.getCustomName() : chest.getName()));
                    }


                    if (compound.contains("chestCart", NbtElement.INT_ARRAY_TYPE)) {
                        // this only works for entities in rendered chunks and I cannot be bothered to figure out why. It's 12:15, and I want to sleep.
                        UUID id = compound.getUuid("chestCart");
                        Entity theEntity = world.getEntity(id);

                        if (theEntity == null) {
                            return;
                        }

                        if (theEntity instanceof ChestMinecartEntity chest) {
                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player1) -> {
                                ScreenHandlerType type = chest.createMenu(syncId, inv, player1).getType();
                                GenericContainerScreenHandler screenHandler = new GenericContainerScreenHandler(type, syncId, inv, chest, chest.size() / 9) {
                                    @Override
                                    public boolean canUse(PlayerEntity player) {
                                        return true;
                                    }
                                };
                                return screenHandler;
                            }, chest.hasCustomName() ? chest.getCustomName() : chest.getName()));
                        } else {
                            LOGGER.warn("ENTITY REFERENCE BY ID WAS NOT THE EXPECTED TYPE CHEST MINECART ENTITY");
                            LOGGER.warn("in fact, the actual type is {}", theEntity.getType());
                        }
                    }
                }
            }
            // when you shift-click an item in a full inventory, the item gets voided. it's not a bug, it's a feature.
            if (instance instanceof InventoryPeeper containerInstance && !containerInstance.getInventory().isValid(slotIndex, slot.getStack()) && actionType == SlotActionType.QUICK_MOVE){
                return;
            }
            if (!slot.inventory.isValid(slotIndex, slot.getStack())) return;
        }
        this.internalOnSlotClick(slotIndex, button, actionType, player);
    }

    /**
     * I had to inject this because the original just returns true without checking if it's actually valid.
     * @param slot the slot that I'm checking to be valid.
     * @param no. I'm not explaining the rest of this.
     **/
// This is around the time when I was still delirious after writing my English novel essay, so don't mind the name.
    @Inject(at = @At("HEAD"), method = "canInsertIntoSlot(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;)Z", cancellable = true)
    public void canInsertIntoSlotInject(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(slot.inventory.isValid(slot.id, stack));
    }
}
