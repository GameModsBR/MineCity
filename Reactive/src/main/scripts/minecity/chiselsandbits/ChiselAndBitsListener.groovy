package minecity.chiselsandbits

import br.com.gamemods.minecity.api.permission.*
import br.com.gamemods.minecity.reactive.ReactiveLayer
import minecity.forge.ForgeListener
import mod.chiselsandbits.api.EventBlockBitModification
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class ChiselAndBitsListener extends ForgeListener {

    @SubscribeEvent
    onBlockBitModification(EventBlockBitModification event) {
        try {
            def player = ReactiveLayer.getEntityData(event.player).get()
            def world = ReactiveLayer.getWorldData(event.world).get()
            def pos = world.getBlockPos(event.pos).get()
            player.can(PermissionFlag.MODIFY, pos).ifPresent {
                event.setCanceled(true)
                player.send(FlagHolder.wrapDeny(it))
                player.sendInventoryUpdate()
            }
        }
        catch (ex) {
            ex.printStackTrace()
            event.canceled = true
        }
    }

}
