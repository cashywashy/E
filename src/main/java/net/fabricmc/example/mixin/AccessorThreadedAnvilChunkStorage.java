package net.fabricmc.example.mixin;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface AccessorThreadedAnvilChunkStorage {
    @Invoker
    NbtCompound invokeGetUpdatedChunkNbt(ChunkPos pos);



    @Accessor
    PointOfInterestStorage getPointOfInterestStorage();

    @Accessor
    ThreadExecutor<Runnable> getMainThreadExecutor();

}
