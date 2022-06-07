package net.fabricmc.example.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
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

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;internalOnSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), method = "onSlotClick")
    public void PooPooPeePee(ScreenHandler instance, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex != -999 && slotIndex != -1) {
            Slot slot = instance.slots.get(slotIndex);
            if (instance instanceof GenericContainerScreenHandler containerInstance && !containerInstance.getInventory().isValid(slotIndex, slot.getStack()) && actionType == SlotActionType.QUICK_MOVE) return;
            if (!slot.inventory.isValid(slotIndex, slot.getStack())) return;
        }
        this.internalOnSlotClick(slotIndex, button, actionType, player);
    }

    @Inject(at = @At("HEAD"), method = "canInsertIntoSlot(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;)Z", cancellable = true)
    public void PeePeePooPoo(ItemStack stack, Slot slot, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(slot.inventory.isValid(slot.id, stack));
    }
}
