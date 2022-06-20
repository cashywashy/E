package net.fabricmc.example.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VersionedChunkStorage.class)
public interface AccessorVersionedChunkStorage {
    @Accessor
    StorageIoWorker getWorker();

    @Invoker
    NbtCompound invokeGetNbt(ChunkPos pos);
}
