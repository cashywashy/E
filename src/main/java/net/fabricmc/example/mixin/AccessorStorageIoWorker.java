package net.fabricmc.example.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(StorageIoWorker.class)
public interface AccessorStorageIoWorker {
    @Invoker
    CompletableFuture<NbtCompound> invokeReadChunkData(ChunkPos pos);

    @Accessor
    Map getResults();

}
