package minecity.opencomputers

import br.com.gamemods.minecity.api.permission.PermissionFlag
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData
import br.com.gamemods.minecity.reactive.reaction.*
import li.cil.oc.api.internal.Robot

class RobotBlock implements ReactiveBlockType {

    UUID getOwner(TileEntityData block) {
        block.tileEntity.with {
            if(it instanceof Robot)
                it.ownerUUID()
            else null
        }
    }

    @Override
    Reaction reactPreModification(PreModification event) {
        event.entityCause.with {
            if(isPresent()) {
                event.snapshot.tileEntityData.map { getOwner(it) }.map {
                    if (it == get().identity.uniqueId)
                        new ApproveReaction(event.snapshot.position, PermissionFlag.MODIFY)
                }.orElseGet {
                    new SingleBlockReaction(event.snapshot.position, PermissionFlag.MODIFY)
                }
            }
            else {
                new SingleBlockReaction(event.snapshot.position, PermissionFlag.MODIFY)
            }
        }
    }

    @Override
    Reaction reactRightClick(Interaction event) {
        event.block.tileEntity.map{ getOwner(it) }.map {
            if(it == event.entity.identity.uniqueId)
                new ApproveReaction(event.block.position, PermissionFlag.MODIFY)
        }.orElseGet {
            new SingleBlockReaction(event.block.position, PermissionFlag.MODIFY)
        }
    }

    @Override
    Reaction reactBeingBroken(Modification event) {
        event.entityCause.flatMap { entity->
            event.blockChange.original.tileEntityData.map{ getOwner(it) }.map {
                if(it == entity.identity.uniqueId)
                    new ApproveReaction(event.position, PermissionFlag.MODIFY)
            }
        }.orElseGet {
            new SingleBlockReaction(event.position, PermissionFlag.MODIFY)
        }
    }
}
