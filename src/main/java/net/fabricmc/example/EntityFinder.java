package net.fabricmc.example;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.storage.ChunkDataAccess;
import net.minecraft.world.storage.EntityChunkDataAccess;

public class EntityFinder {

    public static void find(ServerWorld world, ServerPlayerEntity player){
        ChunkManager chunkyCheese =  world.getChunkManager();
        // ServerWorld, ChunkDataAccess EntityChunkDataAccess, ChunkPos
    }

}
