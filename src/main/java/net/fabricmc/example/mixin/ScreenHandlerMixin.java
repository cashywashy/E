package net.fabricmc.example.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow
    protected abstract void internalOnSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player);

    @Shadow @Final public int syncId;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), method = "onSlotClick")
    public void PooPooPeePee(ScreenHandler instance, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex != -999 && slotIndex != -1) {
            Slot slot = instance.slots.get(slotIndex);
            ItemStack stack = slot.getStack();
            if (stack.hasNbt()){
                NbtCompound compound = stack.getNbt();
                if (compound.contains("position", NbtElement.INT_ARRAY_TYPE)){
                    int[] nbt = compound.getIntArray("position");
                    BlockPos pos = new BlockPos(nbt[0],nbt[1],nbt[2]);
                    if (player.getWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest){


                        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player1) -> {
                            ScreenHandlerType type = chest.createMenu(syncId, inv,player1).getType();
                            GenericContainerScreenHandler screenHandler = new GenericContainerScreenHandler(type, syncId, inv, chest, chest.size()/9) {
                                @Override
                                public boolean canUse(PlayerEntity player) {
                                    return true;
                                }
                            };

                            return screenHandler;
                        },chest.hasCustomName()?chest.getCustomName():chest.getName()));
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
