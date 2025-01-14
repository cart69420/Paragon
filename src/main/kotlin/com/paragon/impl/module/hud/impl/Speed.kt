package com.paragon.impl.module.hud.impl

import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.util.render.font.FontUtil.getStringWidth
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer
import net.minecraft.util.text.TextFormatting
import java.util.*
import kotlin.math.hypot

/**
 * @author Surge
 */
object Speed : HUDModule("Speed", "Displays your current speed") {

    private val unit = Setting(
        "Unit", Unit.BPS
    ) describedBy "The unit to display the speed in"

    override fun render() {
        drawStringWithShadow(
            "Speed " + TextFormatting.WHITE + String.format(
                "%.2f", unit.value.algorithm(
                    getPlayerSpeed(
                        minecraft.player.posX - minecraft.player.lastTickPosX, minecraft.player.posZ - minecraft.player.lastTickPosZ
                    )
                )
            ) + unit.value.name.lowercase(
                Locale.getDefault()
            ), x, y, Colours.mainColour.value, alignment.value
        )
    }

    override var width = 10F
        get() = getStringWidth(
            "Speed " + String.format(
                "%.2f", unit.value.algorithm(
                    getPlayerSpeed(
                        minecraft.player.posX - minecraft.player.lastTickPosX, minecraft.player.posZ - minecraft.player.lastTickPosZ
                    )
                )
            ) + unit.value.name.lowercase(
                Locale.getDefault()
            )
        )

    override var height = FontUtil.getHeight()
        get() = FontUtil.getHeight()

    enum class Unit(val algorithm: (Double) -> Double) {
        /**
         * Speed in blocks per second
         */
        BPS({ it }),

        /**
         * Speed in kilometers (1000 blocks) per hour
         */
        KMH({ it * 3.6 }),

        /**
         * Speed in miles (1.60934 km) per hour
         */
        MPH({ it * 2.237 });

    }

    fun getPlayerSpeed(distX: Double, distZ: Double): Double {
        return hypot(distX, distZ) * (1000 / ((minecraft as IMinecraft).hookGetTimer() as ITimer).hookGetTickLength()).toDouble()
    }

}