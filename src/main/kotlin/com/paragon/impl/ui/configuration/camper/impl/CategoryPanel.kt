package com.paragon.impl.ui.configuration.camper.impl

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.shared.Panel
import com.paragon.impl.ui.configuration.camper.CamperCheatGUI
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.string.StringUtil
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import me.surge.animation.Easing
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.lang.Double.max


class CategoryPanel(val gui: CamperCheatGUI?, val category: Category, x: Float, y: Float, width: Float, height: Float, private val maxHeight: Double = 320.0) : Panel(x, y, width, height) {

    private val hover = ColourAnimation(Color(30, 30, 30), Color(40, 40, 40), { 200f }, false, { Easing.LINEAR })
    private val topGradient = ColourAnimation(Color(0, 0, 0, 0), Color(0, 0, 0, 100), { 500f }, false, { Easing.LINEAR })
    private val bottomGradient = ColourAnimation(Color(0, 0, 0, 0), Color(0, 0, 0, 100), { 500f }, false, { Easing.LINEAR })

    val expanded = Animation({ ClickGUI.animationSpeed.value }, true, { ClickGUI.easing.value })
    val elements = arrayListOf<ModuleElement>()

    private var scroll = 0f
    private var real = 0f
    private val scrollAnimation = Animation({ 800f }, false) { Easing.LINEAR }

    var moduleHeight = 0.0

    override var width: Float = width
        get() = field
        set(value) {}

    init {
        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate {  it.category == category }.forEach {
            elements.add(ModuleElement(this, it, x + 1, y, width - 2, 12f))
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        val totalHeight = getFilteredModules().sumOf { it.getAbsoluteHeight().toDouble() + 1 }.toFloat() + 1

        hover.state = isHovered(mouseX, mouseY)
        topGradient.state = scroll < 0
        bottomGradient.state = elements.last().y + elements.last().height > y + height + moduleHeight
        
        RenderUtil.drawRect(x, y, width, height, Colours.mainColour.value.fade(Colours.mainColour.value.darker(), hover.getAnimationFactor()))

        if (mouseX in x..x + width && mouseY in y + height..y + height + totalHeight) {
            real += mouseDelta * 0.2f
        }

        scrollAnimation.state = scroll != real

        if (scroll > real) {
            val scrollDiff = scroll - real
            scroll -= scrollDiff / 2 * scrollAnimation.getAnimationFactor().toFloat()
        }

        if (scroll < real) {
            val scrollDiff = real - scroll
            scroll += scrollDiff / 2 * scrollAnimation.getAnimationFactor().toFloat()
        }

        scroll = MathHelper.clamp(scroll.toDouble(), -max(0.0, totalHeight - moduleHeight), 0.0).toFloat() * expanded.getAnimationFactor().toFloat()
        real = MathHelper.clamp(real.toDouble(), -max(0.0, totalHeight - moduleHeight), 0.0).toFloat() * expanded.getAnimationFactor().toFloat()

        FontUtil.drawCenteredString(StringUtil.getFormattedText(category), x + width / 2f, y + 3f, Color.WHITE, false)

        moduleHeight = MathHelper.clamp(totalHeight.toDouble(), 0.0, maxHeight) * expanded.getAnimationFactor()

        if (moduleHeight < maxHeight) {
            topGradient.state = false
            bottomGradient.state = false
        }

        RenderUtil.drawRect(x, y + height, width, moduleHeight.toFloat(), Color(0, 0, 0, 100))
        RenderUtil.pushScissor(x, y + height, width, moduleHeight.toFloat())

        var offset = y + height + scroll + 1
        getFilteredModules().forEach {
            it.x = x + 1
            it.y = offset

            it.draw(mouseX, mouseY, mouseDelta)

            offset += it.getAbsoluteHeight() + 1
        }

        RenderUtil.drawVerticalGradientRect(x, y + height + moduleHeight.toFloat() - 5f, width, 5f, Color(0, 0, 0, 0), bottomGradient.getColour())

        RenderUtil.popScissor()

        RenderUtil.drawBorder(x, y, width, height + moduleHeight.toFloat(), 0.5f, Colours.mainColour.value)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && click == Click.RIGHT) {
            expanded.state = !expanded.state
            return
        }

        if (expanded.getAnimationFactor() > 0 && mouseX in x..x + width && mouseY in y + height..y + height + moduleHeight.toFloat()) {
            getFilteredModules().forEach {
                it.mouseClicked(mouseX, mouseY, click)
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.getAnimationFactor() > 0) {
            getFilteredModules().forEach {
                it.mouseReleased(mouseX, mouseY, click)
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded.getAnimationFactor() > 0) {
            getFilteredModules().forEach {
                it.keyTyped(character, keyCode)
            }
        }
    }

    private fun getFilteredModules(): List<ModuleElement> {
        if (gui == null) {
            return elements
        }

        return elements.filter { it.module.isValidSearch(gui.search) }
    }

}