package minecity

import mcjty.rftools.blocks.blockprotector.BlockProtectorBlock
import mcjty.rftools.blocks.elevator.ElevatorBlock
import mcjty.rftools.blocks.endergen.*
import mcjty.rftools.blocks.logic.generic.LogicSlabBlock
import mcjty.rftools.blocks.monitor.*
import mcjty.rftools.blocks.relay.RelayBlock
import mcjty.rftools.blocks.screens.ScreenControllerBlock
import mcjty.rftools.blocks.teleporter.*
import mcjty.rftoolscontrol.blocks.multitank.MultiTankBlock
import mcjty.rftoolscontrol.blocks.node.NodeBlock
import minecity.forge.Wrench

blockType([
        MatterTransmitterBlock, MatterReceiverBlock, DialingDeviceBlock, ScreenControllerBlock,
        LogicSlabBlock, ElevatorBlock, EnderMonitorBlock, NodeBlock, MultiTankBlock,
        EndergenicBlock, BlockProtectorBlock, LiquidMonitorBlock, RFMonitorBlock,
        RelayBlock
]) {
    setReactive modifiableBlock
}

itemType('rftools:smartwrench') {
    setReactive new Wrench()
}
