package net.fabricmc.example.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.storage.ChunkDataAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Unique
    public ChunkDataAccess getAccessor(){
        return ((AccessorServerEntityManager)((AccessorServerWorld)this).getEntityManager()).getDataAccess();
    }
}
