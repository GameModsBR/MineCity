package minecity.minecraft

import br.com.gamemods.minecity.api.command.Message
import br.com.gamemods.minecity.api.world.*
import br.com.gamemods.minecity.reactive.game.block.*
import br.com.gamemods.minecity.reactive.game.block.data.*
import br.com.gamemods.minecity.reactive.reaction.*

import static br.com.gamemods.minecity.api.permission.PermissionFlag.*

class Chorus implements ReactiveBlockType {

    BlockRole blockRole = BlockRole.HARVESTEABLE
    BlockTypeData flower


    @Override
    Reaction reactBeingBroken(Modification event) {
        if(event.blockChange.original.blockTypeData.matches('minecraft:chorus_flower')
                && event.blockChange.original.blockStateData.getTraitString('minecraft:chorus_flower_age') != '5') {
            return new SingleBlockReaction(event.position, MODIFY).onDenySendDenialMessage( HARVEST,
                    new Message('action.harvest.not-fully-grown', 'This plant is not fully grown yet.')
            )
        }



        def changedPos = event.position
        def chunk = event.chunk
        def rootPos = event.position
        def scanOrder = Direction.cardinal.collect()
        scanOrder.add 0, Direction.DOWN

        Set scanned = []
        def previousPos = null
        while (previousPos != rootPos && !chunk.getBlockTypeData(rootPos.add(Direction.DOWN)).matches('minecraft:end_stone')) {
            previousPos = rootPos
            scanned.add(rootPos)

            for(Direction direction: scanOrder) {
                def nextPos = rootPos.add direction
                if(!scanned.contains(nextPos) && chunk.getBlockTypeData(nextPos).matches('minecraft:chorus_plant')) {
                    rootPos = nextPos
                    break
                }
            }
        }

        if(!chunk.getBlockTypeData(rootPos.add(Direction.DOWN)).matches('minecraft:end_stone'))
            return new SingleBlockReaction(event.position, MODIFY)

        scanOrder.set 0, Direction.UP
        scanned.clear()
        def remaining = [rootPos] as Queue
        def flowers = [] as Queue<BlockPos>

        BlockPos current
        while (current = remaining.poll()) {
            scanned.add current

            def type = current == changedPos? event.blockChange.original.blockTypeData : chunk.getBlockTypeData(current)

            if(type.matches('minecraft:chorus_flower'))
                flowers.add(current)

            else if(type.matches('minecraft:chorus_plant'))
                scanOrder.stream().map{ current.add(it) }.filter{ !scanned.contains(it) }.forEach{ remaining.add(it) }
        }

        if(flowers.empty)
            new SingleBlockReaction(event.position, MODIFY)
                .onDenySendDenialMessage HARVEST, new Message('action.harvest.chorus-no-flower',"You can't harvest a chorus plant that does not have a flower.")

        else if(flowers.stream().anyMatch{ chunk.getBlockStateData(it).getTraitString('minecraft:chorus_flower_age') != '5' })
            new SingleBlockReaction(event.position, MODIFY).onDenySendDenialMessage( HARVEST,
                    new Message('action.harvest.not-fully-grown', 'This plant is not fully grown yet.')
            )

        else if(flowers.size() == 1 && flowers.element() == rootPos || flowers.element() == changedPos)
            new SingleBlockReaction(rootPos, MODIFY)

        else if(flowers.size() > 1 && event.blockChange.original.blockTypeData.matches('minecraft:chorus_flower'))
            return new SingleBlockReaction(event.position, HARVEST)

        else {

            def flowerState = chunk.getBlockStateData(flowers.element()).withTrait('minecraft:chorus_flower_age', 0).orElse(flower?.defaultBlockStateData)
            if(!flowerState)
                new SingleBlockReaction(event.position, MODIFY)
            else
                return new SingleBlockReaction(rootPos, HARVEST).onAllowExecNextTick {
                    chunk.setAir flowers.remove()
                    chunk.setBlock rootPos, flowerState
                    flowers.forEach {
                        chunk.dropAsItem it
                    }
                }
        }
    }
}
