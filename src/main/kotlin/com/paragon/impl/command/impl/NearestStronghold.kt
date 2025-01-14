package com.paragon.impl.command.impl

import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.SyntaxBuilder

/**
 * @author EBS
 */
object NearestStronghold : Command("Nearest", SyntaxBuilder()) {

    private val endPortalCoords = arrayOf(
        intArrayOf(1888, -32), intArrayOf(-560, 1504), intArrayOf(2064, -4400), intArrayOf(-4992, -512), intArrayOf(2960, 4208), intArrayOf(-3200, 4480), intArrayOf(-5568, 608), intArrayOf(-2496, 5296)
    )

    override fun whenCalled(args: Array<String>, fromConsole: Boolean) {
        // check if server is 2b2t.org using Minecraft.getCurrentServerData()
        if ((minecraft.currentServerData ?: return).serverIP == "connect.2b2t.org") {

            // Check if player is in the nether
            if (minecraft.player.dimension == 1) {
                sendMessage("don't you feel stupid... don't you feel a little ashamed...")
            }

            // get stronghold location nearest to player on 2b2t.org
            var closestX = endPortalCoords[0][0]
            var closestZ = endPortalCoords[0][1]

            var shortestDistance = minecraft.player.getDistanceSq(
                endPortalCoords[0][0].toDouble(), 0.0, endPortalCoords[0][1].toDouble()
            )

            for (i in 1 until endPortalCoords.size) {
                val distance = minecraft.player.getDistanceSq(
                    endPortalCoords[i][0].toDouble(), 0.0, endPortalCoords[i][1].toDouble()
                )

                if (distance < shortestDistance) {
                    closestX = endPortalCoords[i][0]
                    closestZ = endPortalCoords[i][1]
                    shortestDistance = distance
                }
            }

            sendMessage("Nearest stronghold around ($closestX, $closestZ)")
        } else {
            sendMessage("you are not in 2b2t, please join to use this")
        }
    }
}