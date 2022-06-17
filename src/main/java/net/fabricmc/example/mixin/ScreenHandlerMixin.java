package net.fabricmc.example.mixin;

import net.fabricmc.example.PeeperScreenHandler;
import net.fabricmc.example.finders.Finder;
import net.minecraft.block.entity.ChestBlockEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow
    protected abstract void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), method = "onSlotClick")
    public void PooPooPeePee(ScreenHandler instance, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        //noinspection ConstantConditions
        if (player.getWorld().isClient || !((Object) this instanceof PeeperScreenHandler)) {
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
                    if (player.getWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest) {
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
                        UUID id = compound.getUuid("chestCart");
                        ChunkPos chunkPos = new ChunkPos(pos);
                        ChestMinecartEntity chest = null;
                        try {
                            chest = (ChestMinecartEntity) (Finder.findEntities(serverPlayer.getWorld(), chunkPos, 0, e -> e.getUuid().equals(id))).get(10, TimeUnit.SECONDS).findFirst().orElse(null);
                        } catch (Exception e) {
                            LOGGER.error("Caught exception in findEntities", e);
                        }

                        if (chest != null) {
                            ChestMinecartEntity finalChest = chest;
                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player1) -> {
                                ScreenHandlerType type = finalChest.createMenu(syncId, inv, player1).getType();
                                GenericContainerScreenHandler screenHandler = new GenericContainerScreenHandler(type, syncId, inv, finalChest, finalChest.size() / 9) {
                                    @Override
                                    public boolean canUse(PlayerEntity player) {
                                        return true;
                                    }
                                };

                                return screenHandler;
                            }, chest.hasCustomName() ? chest.getCustomName() : chest.getName()));
                        } else {
                            LOGGER.warn("ENTITY REFERENCE BY ID WAS NOT THE EXPECTED TYPE CHEST MINECART ENTITY!!!!?!?!?!!?!!!?!!!?!????!?!");
                        }

                    }
                }
            }

            if (instance instanceof GenericContainerScreenHandler containerInstance && !containerInstance.getInventory().isValid(slotIndex, slot.getStack()) && actionType == SlotActionType.QUICK_MOVE){
                return;
            }
            if (!slot.inventory.isValid(slotIndex, slot.getStack())) return;
        }

        this.internalOnSlotClick(slotIndex, button, actionType, player);
    }


    @Inject(at = @At("HEAD"), method = "canInsertIntoSlot(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;)Z", cancellable = true)
    public void PeePeePooPoo(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(slot.inventory.isValid(slot.id, stack));
    }
}
