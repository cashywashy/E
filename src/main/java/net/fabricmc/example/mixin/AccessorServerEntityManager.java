package net.fabricmc.example.mixin;

import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.storage.ChunkDataAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.stream.Stream;

@Mixin(ServerEntityManager.class)
public interface AccessorServerEntityManager<T extends EntityLike> {

    @Accessor
    ChunkDataAccess<T> getDataAccess();
    @Accessor
    SectionedEntityCache<T> getCache();
}
