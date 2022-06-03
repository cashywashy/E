package net.fabricmc.example.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public final class ItemEffects {
	@Shadow protected ServerWorld world;


	@Shadow @Final protected ServerPlayerEntity player;

	@Inject(at = @At("HEAD"), method = "interactItem")
	public void boomboom(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir){
		if (world.isClient()) return;
		if (stack.hasNbt()) {
			if (stack.getNbt().contains("SpecialEffect")) {
				NbtCompound effect = stack.getNbt().getCompound("SpecialEffect");
				if (effect.contains("id", NbtElement.STRING_TYPE) && effect.contains("power", NbtElement.INT_TYPE))
					switch (effect.getString("id")) {
						case ("explosion"):
							BoomBoom(player, effect, world);
							break;
					}
			}
		}
	}
	private void BoomBoom(PlayerEntity player, NbtCompound effect, World world){
		world.createExplosion(player, player.getX(), player.getY(), player.getZ(), effect.getInt("power"), Explosion.DestructionType.DESTROY);
	}
}