package com.paragon.api.event.render.entity

import me.wolfsurge.cerauno.event.CancellableEvent
import net.minecraft.client.model.ModelBase
import net.minecraft.entity.Entity

/**
 * @author Surge
 */
class RenderEntityEvent(
    val modelBase: ModelBase,
    val entity: Entity,
    val limbSwing: Float,
    val limbSwingAmount: Float,
    val ageInTicks: Float,
    val netHeadYaw: Float,
    val headPitch: Float,
    val scale: Float
) : CancellableEvent() {

    /**
     * Renders the model
     */
    fun renderModel() {
        modelBase.render(
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            netHeadYaw,
            headPitch,
            scale
        )
    }

}