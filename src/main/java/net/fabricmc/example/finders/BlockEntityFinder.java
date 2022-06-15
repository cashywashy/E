package net.fabricmc.example.finders;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The object with the methods for finding nearby blocks.
 **/
public class BlockEntityFinder {
    /**
     * Finds all the blocks of the given type near the player.
     * @param world The world that the blocks and the player is in.
     * @param player The player in question. Used to find the chunkpos the player is in. I could probably just have the user pass in the chunkpos directly instead of doing this, but I'm too lazy to change that right now.
     * @param type The type of block that we're looking for
     **/
    public static List<? extends BlockEntity> find(ServerWorld world, ServerPlayerEntity player, BlockEntityType type) {
        // theoretically unnecessary, but I have it here just because
        int range = 4;
        // chunk manager for reading the chunks and stuff and final List of blockentities that will be returned
        ChunkManager chunkyCheese = world.getChunkManager();
        List<BlockEntity> finalStretch = new ArrayList<>();

        ChunkPos pos = player.getChunkPos();
        List<ChunkPos> poses = new ArrayList<>();
        poses.add(pos);

        poses.add(new ChunkPos(pos.x, pos.z+1));
        poses.add(new ChunkPos(pos.x, pos.z-1));
        for(int x = 1; x <= range; x++){
            poses.add(new ChunkPos(pos.x+x, pos.z));
            poses.add(new ChunkPos(pos.x-x, pos.z));
            for (int z = 1; z <= range; z++) {
                poses.add(new ChunkPos(pos.x+x, pos.z+z));
                poses.add(new ChunkPos(pos.x+x, pos.z-z));
                poses.add(new ChunkPos(pos.x-x, pos.z+z));
                poses.add(new ChunkPos(pos.x-x, pos.z-z));
            }
        }

        poses.forEach(chunkPos -> {
            WorldChunk chunky = chunkyCheese.getWorldChunk(chunkPos.x, chunkPos.z);
                Map<BlockPos, BlockEntity> blockyWocky = chunky.getBlockEntities();
                blockyWocky.forEach((blockPos, blockEntity) -> {
                    if (blockEntity.getType().equals(type)) {
                        finalStretch.add(blockEntity);
                    }
                });
        });


        return finalStretch;
    }

}
