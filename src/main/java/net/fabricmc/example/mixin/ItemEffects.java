package net.fabricmc.example.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Unless Michael makes items with these tags, they shouldn't be able to affect the server at all. Please don't tell him about it
@Mixin(ServerPlayerInteractionManager.class)
public final class ItemEffects {
	public final String EXPLOSION = "explosion";
	public final String YOUSHOULDKILLYOURSELFNOW = "youShouldKillYourselfNOW";

	/**
	 * Checks if {@code stack.getNbt().contains("SpecialEffect")} and parses then activates correlated effect.
	 * @param player the {@link ServerPlayerEntity} that has interacted with the item.
	 * @param world the {@link World} that the event happens in.
	 * @param stack the {@link ItemStack} that the {@link ServerPlayerEntity} interacted with.
	 * @param hand the {@link  Hand}... I don't know what this is for, but it's in the original method, so I had to add it here.
	 * @param cir the {@link CallbackInfoReturnable<ActionResult>}.
	 **/
	@Inject(at = @At("HEAD"), method = "interactItem", cancellable = true)
	public void interactItemOverride(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir){
		EffectActivation(player,world,stack, cir);
	}

	@Inject(at = @At("HEAD"), method = "interactBlock", cancellable = true)
	public void interactBlockOverride(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir){
		EffectActivation(player,world,stack, cir);
	}

	public void EffectActivation(ServerPlayerEntity player, World world, ItemStack stack, CallbackInfoReturnable<ActionResult> cir) {
		if (stack.hasNbt() && stack.getNbt().contains("SpecialEffect")) {
			NbtCompound effect = stack.getNbt().getCompound("SpecialEffect");
			if (effect.contains("id", NbtElement.STRING_TYPE) && effect.contains("power")) {
				switch (effect.getString("id")) {
					case (EXPLOSION) -> BoomBoom(player, effect, world);
					case (YOUSHOULDKILLYOURSELFNOW) -> YouShouldKillYourselfNOW(player, effect, world);
				}
				cir.setReturnValue(ActionResult.SUCCESS);
			}
		}

	}

	/**
	 * Bakuretsu! Bakuretsu! La la la!
	 * @param player the {@link PlayerEntity} that activated the effect.
	 * @param effect the {@link NbtCompound} containing information about the effect. The power param is used to determine explosive strength.
	 * @param world the {@link World} the effect takes place in.
	 **/
	private void BoomBoom(PlayerEntity player, NbtCompound effect, World world){
		Vec3d pos = player.raycast(20,2f,!player.isSubmergedInWater()).getPos();


		if (effect.contains("power", NbtElement.INT_TYPE)) {
			world.createExplosion(player, pos.getX(), pos.getY(), pos.getZ(), effect.getInt("power"), Explosion.DestructionType.DESTROY);
		}
		else {
			world.createExplosion(player, pos.getX(), pos.getY(), pos.getZ(), 10, Explosion.DestructionType.DESTROY);
		}
	}

	/**
	 * Your life is NOTHING. You serve ZERO purpose.
	 * @param player the {@link PlayerEntity} that activated the effect.
	 * @param effect the {@link NbtCompound} containing information about the effect.
	 *               If the power parameter is a {@link NbtString} with a value of {@code NOW} then smite randomly around the player in a 16x8x16 cube.
	 *               If the power parameter is an {@link NbtInt} then smite the {@code player.raycast()} location {@code power} times. The range is also determined by {@code power}.
	 * @param world the {@link World} the effect takes place in.
	 **/
	private void YouShouldKillYourselfNOW(ServerPlayerEntity player, NbtCompound effect, World world){

		if (effect.get("power") instanceof NbtString power && power.asString().equals("NOW")){
				int iterations = (int) (Math.round(Math.random() * 5) + 5);
				for (int i = 0; i < iterations; i++) {
					LightningEntity boomBooms = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
					boomBooms.setChanneler(player);
					boomBooms.damage(DamageSource.player(player), 20);
					float offsetX = (Math.round(Math.random() * 8));
					float offsetY = (Math.round(Math.random() * 8) - 4);
					float offsetZ;
					if (offsetX < 4) offsetZ = (Math.round(Math.random() * 4) + 4);
					else offsetZ = (Math.round(Math.random() * 8));
					offsetX *= Math.random() < 0.5? 1f : -1f;
					offsetZ *= Math.random() < 0.5? 1f : -1f;
					boomBooms.setPos(player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ);
					world.spawnEntity(boomBooms);
				}
		}
		else if (effect.get("power") instanceof NbtInt power){
			Vec3d location = player.raycast(power.intValue()*5, 2f, !player.isSubmergedInWater()).getPos();
			for (int i = 0; i < power.intValue(); i++){
				LightningEntity boomBooms = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
				boomBooms.setChanneler(player);
				boomBooms.damage(DamageSource.player(player), 20);
				boomBooms.setPos(location.x,location.y,location.z);
				world.spawnEntity(boomBooms);
			}
		}
	}
}