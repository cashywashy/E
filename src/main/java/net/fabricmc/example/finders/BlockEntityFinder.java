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

public class BlockEntityFinder {
    public static List<? extends BlockEntity> find(ServerWorld world, ServerPlayerEntity player, BlockEntityType type) {
        int range = 5;
        ChunkManager chunkyCheese = world.getChunkManager();
        List<BlockEntity> finalStretch = new ArrayList<>();

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
