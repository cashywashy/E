package net.fabricmc.example.finders;

import com.mojang.logging.LogUtils;
import net.fabricmc.example.mixin.AccessorServerEntityManager;
import net.fabricmc.example.mixin.AccessorServerWorld;
import net.fabricmc.example.mixin.AccessorThreadedAnvilChunkStorage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.storage.EntityChunkDataAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains all the methods for finding stuff
 **/
public class Finder {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    @FunctionalInterface
    interface ChunkConsumer<T> {
        T apply(ChunkPos chunkPos) throws Exception;
    }
    /**
     * for getting the chunkdata of unrendered chunks. Michael suggested this.
     * Quite recently I realised that {@link net.minecraft.world.World}'s getChunk method doesn't actually load the chunk,
     * but due to the lost cause fallacy and also because it is currently 1:30 in the morning, I'm keeping this.
     * @param world the {@link ServerWorld} of the chunk we are searching.
     * @param chunkPos the {@link ChunkPos} of the chunk we are analyzing.
    **/
    @Nullable
    public static WorldChunk loadChunkIndependent(ServerWorld world, ChunkPos chunkPos) throws ExecutionException, InterruptedException {
        AccessorThreadedAnvilChunkStorage chunkStorage = (AccessorThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage;
        NbtCompound nbtCompound = chunkStorage.invokeGetUpdatedChunkNbt(chunkPos);
        // if the nbtCompound is null, then it's already null
        if (nbtCompound != null) {
            WorldChunk chunky;
            // this thing is called nbtList4 because I stole some stuff from the original minecraft code
            NbtList nbtList4 = new NbtList();
            if (nbtCompound.contains("block_entities")) {
                nbtList4 = nbtCompound.getList("block_entities", 10);
            }
            // The chunk deserializer decides whether to make the chunk read-only or not based on the chunk type. it caused me quite a bit of trouble.
            boolean bl = nbtCompound.contains("Status", NbtElement.STRING_TYPE);
            if (bl) {
                ProtoChunk chunk = chunkStorage.getMainThreadExecutor().submit(() -> ChunkSerializer.deserialize(world, chunkStorage.getPointOfInterestStorage(), chunkPos, nbtCompound)).get();
                // I kid you not, the deserializer LEAVES OUT INFORMATION from the protochunk if it's a levelChunk, so I have to add it in myself, BUT I CAN'T IF IT'S A READONLYCHUNK
                if (chunk instanceof ReadOnlyChunk screwYouDeserializer) {
                    chunky = screwYouDeserializer.getWrappedChunk();
                    NbtList list = new NbtList();
                    chunk.getBlockEntities().forEach((blockPos, blockEntity) -> list.add(blockEntity.createNbtWithIdentifyingData()));
                    // adds the blockentities to the WorldChunk
                    if (!nbtList4.equals(list)) {
                        for (int p = 0; p < nbtList4.size(); ++p) {
                            NbtCompound nbtCompound4 = nbtList4.getCompound(p);
                            chunky.addPendingBlockEntityNbt(nbtCompound4);
                            BlockPos pos = BlockEntity.posFromNbt(nbtCompound4);
                            BlockEntity block = BlockEntity.createFromNbt(pos, world.getBlockState(pos), nbtCompound4);
                            chunky.setBlockEntity(block);
                        }
                    }
                    return chunky;
                }
            }
            LOGGER.warn("Chunk file at {} is missing level data, skipping", chunkPos); // LOGGER.warn("I f*cked something up");
        }
        return null;
    }

/**
 * As the name implies, it iterates over every chunk in a {@literal range} chunk radius and runs {@literal callable} for each one.
 * @param centre the center of the group of chunks that will be iterated over.
 **/
    public static <T> Future<T[]> forEachChunk(ChunkPos centre, int range, T[] empty, ChunkConsumer<T> callable) {
        int len = range * 2 + 1;

        int i = 0;
        Future<T>[] futures = new Future[len * len];
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                ChunkPos chunkPos = new ChunkPos(centre.x + x, centre.z + z);
                futures[i++] = (EXECUTOR_SERVICE.submit(() -> callable.apply(chunkPos)));
            }
        }

        return EXECUTOR_SERVICE.submit(() -> {
            T[] results = Arrays.copyOf(empty, futures.length);
            for (int j = 0; j < futures.length; j++) {
                results[j] = futures[j].get();
            }
            return results;
        });
    }

    /**
     * Finds entities of a certain type in a group of chunks
     * @param centre the center of the group of chunks.
     * @param range the radius of the group of chunks.
     * @param predicate the filter used to get the other entities out of the entity list.
    **/
    public static Future<Stream<? extends Entity>> findEntities(ServerWorld world, ChunkPos centre, int range, Predicate<Entity> predicate) throws ExecutionException, InterruptedException, IOException {
        return EXECUTOR_SERVICE.submit(() -> {
            AccessorServerEntityManager<Entity> entityManager = (AccessorServerEntityManager<Entity>) ((AccessorServerWorld) world).getEntityManager();
            EntityChunkDataAccess entityChunkDataAccess = (EntityChunkDataAccess) entityManager.getDataAccess();

            List<Entity>[] lists = forEachChunk(centre, range, (List<Entity>[]) new List[0], (ChunkPos chunkPos) -> {
                final Stream<Entity> steem;
                if (world.isChunkLoaded(chunkPos.toLong())) {
                    steem = entityManager.getCache().getTrackingSections(chunkPos.toLong()).flatMap(EntityTrackingSection::stream);
                } else {
                    steem = entityChunkDataAccess.readChunkData(chunkPos).exceptionally(throwable -> {
                        LogUtils.getLogger().error("Failed to read chunk {}", chunkPos, throwable);
                        return null;
                    }).get().stream().filter(Objects::nonNull);
                }
                return steem.filter(predicate).collect(Collectors.toList());
            }).get();

            return Arrays.stream(lists).flatMap(Collection::stream);
        });

    }

    /**
     * Finds all the blocks of the given type near the player.
     * @param type instead of a predicate, I just ask for the {@link BlockEntityType} and make the predicate myself. Why? Not sure. I don't remember the reasoning
     * @param refer to the entityFind methods
     **/
     public static <T extends BlockEntity> Future<List<T>> findBlocks(ServerWorld world, ChunkPos centre, int range, BlockEntityType<T> type) {
        return EXECUTOR_SERVICE.submit(() -> {
            List<T>[] lists = forEachChunk(centre, range, (List<T>[]) new List[0], (ChunkPos chunkPos) -> {
                final Map<BlockPos, BlockEntity> blockEntities;
                if (world.isChunkLoaded(chunkPos.toLong())) {
                    blockEntities = world.getChunk(chunkPos.x, chunkPos.z).getBlockEntities();
                } else {
                    WorldChunk chunk = loadChunkIndependent(world, chunkPos);

                    if (chunk != null) {
                         blockEntities = chunk.getBlockEntities();
                    }
                    else {
                        blockEntities = null;
                    }
                }
                //noinspection unchecked
                return blockEntities != null ? (List<T>) blockEntities.values().stream().filter(blockEntity -> blockEntity.getType().equals(type)).collect(Collectors.toList()) : List.<T>of();
            }).get();
            return Arrays.stream(lists).flatMap(Collection::stream).collect(Collectors.toList());
        });
    }
}
