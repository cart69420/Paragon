package com.paragon.impl.module.hud

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.panel.impl.CategoryPanel
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import java.io.IOException

/**
 * @author SooStrator1136
 */
class HUDEditorGUI : GuiScreen() {

    private var draggingComponent = false
    private val panel = CategoryPanel(null, Category.HUD, 200f, 20f, 80f, 22f, 200.0)

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        val scaledResolution = ScaledResolution(mc)

        RenderUtil.drawRect(0f, 0f, scaledResolution.scaledWidth.toFloat(), scaledResolution.scaledHeight.toFloat(), Color(0, 0, 0, 180))

        RenderUtil.drawRect(scaledResolution.scaledWidth / 2f - 0.5f, 0f, 1f, scaledResolution.scaledHeight.toFloat(), Color(255, 255, 255, 100))
        RenderUtil.drawRect(0f, scaledResolution.scaledHeight / 2f - 0.5f, scaledResolution.scaledWidth.toFloat(), 1f, Color(255, 255, 255, 100))

        // mc font renderer does not work with \n
        FontUtil.drawStringWithShadow("Hold Left Click to drag", 3f, scaledResolution.scaledHeight - FontUtil.getHeight() * 3f - 4, Color.WHITE)
        FontUtil.drawStringWithShadow("Middle Click to align (left/center/right)", 3f, scaledResolution.scaledHeight - FontUtil.getHeight() * 2f - 4, Color.WHITE)
        FontUtil.drawStringWithShadow("Right Click to hide", 3f, scaledResolution.scaledHeight - FontUtil.getHeight() - 4, Color.WHITE)

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.animation.getAnimationFactor() > 0 }.forEach {
            (it as HUDModule).updateComponent(mouseX, mouseY)

            RenderUtil.scaleTo(it.x + (it.width / 2), it.y + (it.height / 2), 0f, it.animation.getAnimationFactor(), it.animation.getAnimationFactor(), 0.0) {
                if (it.isDragging || it.isHovered(it.x, it.y, it.width, it.height, mouseX, mouseY)) {
                    RenderUtil.drawRoundedRect(it.x - 2, it.y - 2, it.width + 4, it.height + 4, 5f, Colours.mainColour.value.integrateAlpha(150f))
                }

                it.render()
            }
        }

        glColor4f(1f, 1f, 1f, 1f)
        panel.draw(mouseX.toFloat(), mouseY.toFloat(), Mouse.getDWheel())
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        Paragon.INSTANCE.moduleManager.modules.reverse()

        run {
            Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
                if (!draggingComponent) {
                    if ((it as HUDModule).mouseClicked(mouseX, mouseY, mouseButton)) {
                        return@run
                    }

                    if (it.isDragging) {
                        draggingComponent = true
                    }
                }
            }
        }

        Paragon.INSTANCE.moduleManager.modules.reverse()

        if (!draggingComponent) {
            panel.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton))
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        draggingComponent = false
        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).mouseReleased(mouseX, mouseY, state)
        }

        panel.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(state))

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun onGuiClosed() {
        draggingComponent = false

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).mouseReleased(0, 0, 0)
        }
    }

}