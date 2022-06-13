package net.fabricmc.example.finders;

import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import net.fabricmc.example.mixin.AccessorServerEntityManager;
import net.fabricmc.example.mixin.AccessorServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.ChunkDataList;
import net.minecraft.world.storage.EntityChunkDataAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EntityFinder {

    public static List<? extends Entity> find(ServerWorld world, ServerPlayerEntity player, EntityType type) throws ExecutionException, InterruptedException, IOException {
        int range = 4;
        ChunkPos pos = player.getChunkPos();
        List<ChunkPos> poses = new ArrayList<>();
        poses.add(pos);

        for(int x = 1; x <= range; x++){
            for (int z = 1; z <= range; z++) {
                poses.add(new ChunkPos(pos.x+x, pos.z+z));
                poses.add(new ChunkPos(pos.x+x, pos.z-z));
                poses.add(new ChunkPos(pos.x-x, pos.z+z));
                poses.add(new ChunkPos(pos.x-x, pos.z-z));
            }
        }

        ServerEntityManager<Entity> entityManager = ((AccessorServerWorld) world).getEntityManager();
        EntityChunkDataAccess entityChunkDataAccess = (EntityChunkDataAccess) ((AccessorServerEntityManager) ((AccessorServerWorld) world).getEntityManager()).getDataAccess();
        Queue<ChunkDataList<Entity>> loadingQueue = Queues.newConcurrentLinkedQueue();

        entityManager.flush();

        poses.forEach(chunkPos -> {
            ((CompletableFuture) entityChunkDataAccess.readChunkData(chunkPos).thenAccept(loadingQueue::add)).exceptionally(throwable -> {
                LogUtils.getLogger().error("Failed to read chunk {}", chunkPos, throwable);
                return null;
            });
        });


        entityChunkDataAccess.awaitAll(true);
        List<ChunkDataList<Entity>> dataLists = loadingQueue.stream().collect(Collectors.toList());

        List<Entity> finalList = new ArrayList<>();

        for (int i = 0; i < dataLists.size(); i++) {
            List<Entity> listOfEntities = dataLists.get(i).stream().filter(Predicate.isEqual(type.getClass())).collect(Collectors.toList());
            finalList.addAll(listOfEntities);
        }

        entityChunkDataAccess.awaitAll(false);
        return finalList;
    }

}
