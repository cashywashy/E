package net.fabricmc.example.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class ThatRenderOverLayThing {
    private static void renderOverlay(MatrixStack matrixStack, Identifier texture, float opacity, float[] posX, float posY[]) {
        final var window = MinecraftClient.getInstance().getWindow();
        final float minHeight = window.getScaledHeight()*posY[0];
        final float minWidth = window.getScaledWidth()*posX[0];
        final float maxHeight = window.getScaledHeight()*posY[1];
        final float maxWidth = window.getScaledWidth()*posX[1];
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);
        RenderSystem.setShaderTexture(0, texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        final Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        bufferBuilder.vertex(matrix4f, minWidth, maxHeight, -90.0f).texture(0.0f, 1.0f).next();
        bufferBuilder.vertex(matrix4f, maxWidth, maxHeight, -90.0f).texture(1.0f, 1.0f).next();
        bufferBuilder.vertex(matrix4f, maxWidth, minHeight, -90.0f).texture(1.0f, 0.0f).next();
        bufferBuilder.vertex(matrix4f, minWidth, minHeight, -90.0f).texture(0.0f, 0.0f).next();
        tessellator.draw();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
