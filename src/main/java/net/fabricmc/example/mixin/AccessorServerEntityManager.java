package net.fabricmc.example.mixin;

import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.storage.ChunkDataAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerEntityManager.class)
public interface AccessorServerEntityManager<T extends EntityLike> {

    @Accessor
    ChunkDataAccess<T> getDataAccess();
}
