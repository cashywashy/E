package net.fabricmc.example.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
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

	@Shadow @Final protected ServerPlayerEntity player;

	@Shadow protected ServerWorld world;

	@Inject(at = @At("HEAD"), method = "interactItem")
	public void boomboom(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir){
		if (world.isClient()) return;
		if (stack.hasNbt()) {
			if (stack.getNbt().contains("SpecialEffect")) {
				NbtCompound effect = stack.getNbt().getCompound("SpecialEffect");
				if (effect.contains("id", NbtElement.STRING_TYPE) && effect.contains("power"))
					switch (effect.getString("id")) {
						case ("explosion") -> BoomBoom(player, effect, world);
						case ("youShouldKillYourselfNOW") -> YouShouldKillYourselfNOW(player, effect, world);
					}
			}
		}
	}
	private void BoomBoom(PlayerEntity player, NbtCompound effect, World world){
		if (effect.contains("power", NbtElement.INT_TYPE)) {
			world.createExplosion(player, player.getX(), player.getY(), player.getZ(), effect.getInt("power"), Explosion.DestructionType.DESTROY);
		}
		else {
			world.createExplosion(player, player.getX(), player.getY(), player.getZ(), 10, Explosion.DestructionType.DESTROY);
		}
	}
	private void YouShouldKillYourselfNOW(PlayerEntity player, NbtCompound effect, World world){

		if (effect.get("power") instanceof NbtString power && power.asString().equals("NOW")){
				int iterations = (int) (Math.round(Math.random() * 5) + 5);
				for (int i = 0; i < iterations; i++) {
					LightningEntity boomBooms = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
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
			Vec3d location = player.raycast(power.intValue()*5, 2f, true).getPos();
			for (int i = 0; i < power.intValue(); i++){
				LightningEntity boomBooms = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
				boomBooms.setPos(location.x,location.y,location.z);
				world.spawnEntity(boomBooms);
			}
		}
	}
}