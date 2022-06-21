package net.fabricmc.example.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface AccessorThreadedAnvilChunkStorage {
    @Invoker
    NbtCompound invokeGetUpdatedChunkNbt(ChunkPos pos);

    @Accessor
    PointOfInterestStorage getPointOfInterestStorage();

    @Accessor
    ThreadExecutor<Runnable> getMainThreadExecutor();

}
