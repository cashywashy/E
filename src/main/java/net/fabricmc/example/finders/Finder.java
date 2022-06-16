package net.fabricmc.example.finders;

import com.mojang.logging.LogUtils;
import net.fabricmc.example.mixin.AccessorServerEntityManager;
import net.fabricmc.example.mixin.AccessorServerWorld;
import net.fabricmc.example.mixin.AccessorThreadedAnvilChunkStorage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.storage.EntityChunkDataAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Finder {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    @FunctionalInterface
    interface ChunkConsumer<T> {
        T apply(ChunkPos chunkPos);
    }

    private static ProtoChunk loadChunkIndependent(ServerWorld world, ChunkPos chunkPos) {
        AccessorThreadedAnvilChunkStorage chunkStorage = (AccessorThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage;


        NbtCompound nbtCompound = chunkStorage.invokeGetUpdatedChunkNbt(chunkPos);
        if (nbtCompound != null) {
            boolean bl = nbtCompound.contains("Status", 8);
            if (bl) {
                return ChunkSerializer.deserialize(world, chunkStorage.getPointOfInterestStorage(), chunkPos, nbtCompound);
            }
            LOGGER.error("Chunk file at {} is missing level data, skipping", chunkPos);
        }
        return null;
    }


    public static <T> Future<T[]> forEachChunk(ServerWorld world, ServerPlayerEntity player, int range, T[] empty, ChunkConsumer<T> callable) {
        ChunkPos pos = player.getChunkPos();
        int len = range * 2 + 1;

        int i = 0;
        Future<T>[] futures = new Future[len * len];
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                ChunkPos chunkPos = new ChunkPos(pos.x + x, pos.z + z);
                futures[i++] = (EXECUTOR_SERVICE.submit(() -> callable.apply(chunkPos)));
            }
        }

        CompletableFuture<T[]> completableFuture = new CompletableFuture<>();
        EXECUTOR_SERVICE.submit(() -> {
            T[] results = Arrays.copyOf(empty, futures.length);
            for (int j = 0; j < futures.length; j++) {
                try {
                    results[j] = futures[j].get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            completableFuture.complete(results);
        });
        return completableFuture;
    }

    public static Stream<? extends Entity> findEntities(ServerWorld world, ServerPlayerEntity player, Predicate<Entity> predicate) throws ExecutionException, InterruptedException, IOException {
        int range = 4;
        ServerEntityManager<Entity> entityManager = ((AccessorServerWorld) world).getEntityManager();
        EntityChunkDataAccess entityChunkDataAccess = (EntityChunkDataAccess) ((AccessorServerEntityManager) ((AccessorServerWorld) world).getEntityManager()).getDataAccess();

        List<Entity>[] lists = forEachChunk(world, player, range, (List<Entity>[]) new List[0], (ChunkPos chunkPos) -> {
            final Stream<Entity> steem;
            if (world.isChunkLoaded(chunkPos.toLong())) {
                steem = ((AccessorServerEntityManager<Entity>) entityManager).getCache().getTrackingSections(chunkPos.toLong()).flatMap(section -> section.stream());
            } else {
                steem = entityChunkDataAccess.readChunkData(chunkPos).exceptionally(throwable -> {
                    LogUtils.getLogger().error("Failed to read chunk {}", chunkPos, throwable);
                    return null;
                }).join().stream();
            }
            return steem.filter(predicate).collect(Collectors.toList());
        }).get();


        return Arrays.stream(lists).flatMap(Collection::stream);
    }

    /**
     * Finds all the blocks of the given type near the player.
     *
     * @param world  The world that the blocks and the player is in.
     * @param player The player in question. Used to find the chunkpos the player is in. I could probably just have the user pass in the chunkpos directly instead of doing this, but I'm too lazy to change that right now.
     * @param type   The type of block that we're looking for
     **/
    public static List<? extends BlockEntity> findBlocks(ServerWorld world, ServerPlayerEntity player, BlockEntityType type) throws ExecutionException, InterruptedException {
        int range = 4;

        ChunkManager entityManager = world.getChunkManager();

        List<BlockEntity>[] lists = forEachChunk(world, player, range, (List<BlockEntity>[]) new List[0], (ChunkPos chunkPos) -> {
            final Stream<BlockEntity> steem;
            if (world.isChunkLoaded(chunkPos.toLong())) {
                steem = world.getChunk(chunkPos.x, chunkPos.z).getBlockEntities().values().stream();
            } else {
                steem = loadChunkIndependent(world, chunkPos).getBlockEntities().values().stream();
            }
            return steem.filter(blockEntity -> blockEntity.getType().equals(type)).collect(Collectors.toList());
        }).get();


        return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
