package com.paragon.client.systems.module.impl.render

import com.paragon.api.event.render.tileentity.RenderTileEntityEvent
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.OutlineUtil.renderFive
import com.paragon.api.util.render.OutlineUtil.renderFour
import com.paragon.api.util.render.OutlineUtil.renderOne
import com.paragon.api.util.render.OutlineUtil.renderThree
import com.paragon.api.util.render.OutlineUtil.renderTwo
import com.paragon.api.util.render.RenderUtil.drawBoundingBox
import com.paragon.api.util.render.RenderUtil.drawFilledBox
import com.paragon.api.util.string.StringUtil
import com.paragon.api.util.world.BlockUtil.getBlockBox
import com.paragon.asm.mixins.accessor.IEntityRenderer
import com.paragon.client.shader.shaders.OutlineShader
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.shader.Framebuffer
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityEnderChest
import net.minecraft.tileentity.TileEntityShulkerBox
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glUseProgram
import java.awt.Color
import java.util.function.Consumer

@SideOnly(Side.CLIENT)
object StorageESP : Module("StorageESP", Category.RENDER, "Highlights storage blocks in the world") {

    private val chests = Setting("Chests", true)
        .setDescription("Highlight chests")

    private val shulkers = Setting("Shulkers", true)
        .setDescription("Highlight shulker boxes")

    private val enderChests = Setting("EnderChests", true)
        .setDescription("Highlight Ender Chests")

    // Render settings
    private val mode = Setting("Mode", Mode.SHADER)
        .setDescription("How to render the entities")

    private val lineWidth = Setting("LineWidth", 1f, 0.1f, 8f, 0.1f)
        .setDescription("How thick to render the outlines")

    // Outline shader
    private val outline = Setting("Outline", true)
        .setDescription("Outline the fill")
        .setParentSetting(mode)
        .setVisibility { mode.value == Mode.SHADER }

    private val fill = Setting("Fill", true)
        .setDescription("Fill the outline")
        .setParentSetting(mode)
        .setVisibility { mode.value == Mode.SHADER }

    private val colour = Setting("Colour", Color(185, 17, 255))
        .setDescription("The colour to highlight items in")

    // Shaders
    private val outlineShader = OutlineShader()
    private var frameBuffer: Framebuffer? = null
    private var lastScaleFactor = 0f
    private var lastScaleWidth = 0f
    private var lastScaleHeight = 0f

    override fun onRender3D() {
        if (mode.value == Mode.BOX) {
            minecraft.world.loadedTileEntityList.forEach(Consumer { tileEntity: TileEntity ->
                if (isStorageValid(tileEntity)) {
                    if (fill.value) {
                        drawFilledBox(getBlockBox(tileEntity.pos), colour.value)
                    }

                    if (outline.value) {
                        drawBoundingBox(
                            getBlockBox(tileEntity.pos),
                            lineWidth.value,
                            colour.value.integrateAlpha(255f)
                        )
                    }
                }
            })
        }
    }

    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && mode.value == Mode.SHADER) {
            // Pretty much just taken from Cosmos, all credit goes to them (sorry linus!)
            // https://github.com/momentumdevelopment/cosmos/blob/main/src/main/java/cope/cosmos/client/features/modules/visual/ESPModule.java
            GlStateManager.enableAlpha()
            GlStateManager.pushMatrix()
            GlStateManager.pushAttrib()

            // Delete old frameBuffer
            if (frameBuffer != null) {
                frameBuffer!!.framebufferClear()

                if (lastScaleFactor != event.resolution.scaleFactor.toFloat() || lastScaleWidth != event.resolution.scaledWidth.toFloat() || lastScaleHeight != event.resolution.scaledHeight.toFloat()) {
                    frameBuffer!!.deleteFramebuffer()
                    frameBuffer = Framebuffer(minecraft.displayWidth, minecraft.displayHeight, true)
                    frameBuffer!!.framebufferClear()
                }

                lastScaleFactor = event.resolution.scaleFactor.toFloat()
                lastScaleWidth = event.resolution.scaledWidth.toFloat()
                lastScaleHeight = event.resolution.scaledHeight.toFloat()
            } else {
                frameBuffer = Framebuffer(minecraft.displayWidth, minecraft.displayHeight, true)
            }

            frameBuffer!!.bindFramebuffer(false)
            val previousShadows = minecraft.gameSettings.entityShadows
            minecraft.gameSettings.entityShadows = false
            (minecraft.entityRenderer as IEntityRenderer).setupCamera(event.partialTicks, 0)

            for (tileEntity in minecraft.world.loadedTileEntityList) {
                if (isStorageValid(tileEntity)) {
                    val x = minecraft.renderManager.viewerPosX
                    val y = minecraft.renderManager.viewerPosY
                    val z = minecraft.renderManager.viewerPosZ
                    TileEntityRendererDispatcher.instance.render(
                        tileEntity,
                        tileEntity.pos.x - x,
                        tileEntity.pos.y - y,
                        tileEntity.pos.z - z,
                        minecraft.renderPartialTicks
                    )
                }
            }

            minecraft.gameSettings.entityShadows = previousShadows
            GlStateManager.enableBlend()
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            frameBuffer!!.unbindFramebuffer()
            minecraft.framebuffer.bindFramebuffer(true)
            minecraft.entityRenderer.disableLightmap()
            RenderHelper.disableStandardItemLighting()
            GlStateManager.pushMatrix()

            // Render shader
            outlineShader.setColour(colour.value)
            outlineShader.setWidth(lineWidth.value)
            outlineShader.setFill(if (fill.value) 1 else 0)
            outlineShader.setOutline(if (outline.value) 1 else 0)
            outlineShader.startShader()
            minecraft.entityRenderer.setupOverlayRendering()

            glBindTexture(GL_TEXTURE_2D, frameBuffer!!.framebufferTexture)
            glBegin(GL_QUADS)
            glTexCoord2d(0.0, 1.0)
            glVertex2d(0.0, 0.0)
            glTexCoord2d(0.0, 0.0)
            glVertex2d(0.0, event.resolution.scaledHeight.toDouble())
            glTexCoord2d(1.0, 0.0)
            glVertex2d(event.resolution.scaledWidth.toDouble(), event.resolution.scaledHeight.toDouble())
            glTexCoord2d(1.0, 1.0)
            glVertex2d(event.resolution.scaledWidth.toDouble(), 0.0)
            glEnd()

            // Stop drawing shader
            glUseProgram(0)
            glPopMatrix()

            minecraft.entityRenderer.enableLightmap()
            GlStateManager.popMatrix()
            GlStateManager.popAttrib()
            minecraft.entityRenderer.setupOverlayRendering()
        }
    }

    @Listener
    fun onTileEntityRender(event: RenderTileEntityEvent) {
        if (mode.value == Mode.OUTLINE && isStorageValid(event.tileEntityIn)) {
            val tileEntityIn = event.tileEntityIn
            val partialTicks = event.partialTicks
            val blockPos = tileEntityIn.pos

            event.tileEntityRendererDispatcher.render(
                tileEntityIn,
                blockPos.x.toDouble() - event.staticPlayerX,
                blockPos.y.toDouble() - event.staticPlayerY,
                blockPos.z.toDouble() - event.staticPlayerZ,
                partialTicks
            )
            renderOne(lineWidth.value)
            event.tileEntityRendererDispatcher.render(
                tileEntityIn,
                blockPos.x.toDouble() - event.staticPlayerX,
                blockPos.y.toDouble() - event.staticPlayerY,
                blockPos.z.toDouble() - event.staticPlayerZ,
                partialTicks
            )
            renderTwo()
            event.tileEntityRendererDispatcher.render(
                tileEntityIn,
                blockPos.x.toDouble() - event.staticPlayerX,
                blockPos.y.toDouble() - event.staticPlayerY,
                blockPos.z.toDouble() - event.staticPlayerZ,
                partialTicks
            )
            renderThree()
            renderFour(colour.value)
            event.tileEntityRendererDispatcher.render(
                tileEntityIn,
                blockPos.x.toDouble() - event.staticPlayerX,
                blockPos.y.toDouble() - event.staticPlayerY,
                blockPos.z.toDouble() - event.staticPlayerZ,
                partialTicks
            )
            renderFive()
        }
    }

    private fun isStorageValid(tileEntity: TileEntity?): Boolean {
        return when (tileEntity) {
            is TileEntityChest -> chests.value
            is TileEntityShulkerBox -> shulkers.value
            is TileEntityEnderChest -> enderChests.value
            else -> false
        }
    }

    override fun getData(): String = StringUtil.getFormattedText(mode.value)

    enum class Mode {
        /**
         * Draws a box around the storage block
         */
        BOX,

        /**
         * Uses a shader
         */
        SHADER,

        /**
         * Uses GL Stencil to outline the storage block
         */
        OUTLINE
    }

}