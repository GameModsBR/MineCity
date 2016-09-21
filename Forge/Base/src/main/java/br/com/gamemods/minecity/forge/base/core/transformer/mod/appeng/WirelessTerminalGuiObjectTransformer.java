package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class WirelessTerminalGuiObjectTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public WirelessTerminalGuiObjectTransformer()
    {
        super("appeng.helpers.WirelessTerminalGuiObject");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("testWap"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new FieldInsnNode(GETFIELD, name.replace('.','/'), "myPlayer", "Lnet/minecraft/entity/player/EntityPlayer;"));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKEINTERFACE,
                        "appeng.api.implementations.tiles.IWirelessAccessPoint".replace('.','/'),
                        "getLocation", "()Lappeng.api.util.DimensionalCoord;".replace('.','/'),
                        true
                ));
                list.add(new MethodInsnNode(INVOKEVIRTUAL, "appeng.api.util.DimensionalCoord".replace('.','/'), "getWorld", "()Lnet/minecraft/world/World;", false));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKEINTERFACE,
                        "appeng.api.implementations.tiles.IWirelessAccessPoint".replace('.','/'),
                        "getLocation", "()Lappeng.api.util.DimensionalCoord;".replace('.','/'),
                        true
                ));
                list.add(new FieldInsnNode(GETFIELD, "appeng.api.util.WorldCoord".replace('.','/'), "x", "I"));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKEINTERFACE,
                        "appeng.api.implementations.tiles.IWirelessAccessPoint".replace('.','/'),
                        "getLocation", "()Lappeng.api.util.DimensionalCoord;".replace('.','/'),
                        true
                ));
                list.add(new FieldInsnNode(GETFIELD, "appeng.api.util.WorldCoord".replace('.','/'), "y", "I"));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKEINTERFACE,
                        "appeng.api.implementations.tiles.IWirelessAccessPoint".replace('.','/'),
                        "getLocation", "()Lappeng.api.util.DimensionalCoord;".replace('.','/'),
                        true
                ));
                list.add(new FieldInsnNode(GETFIELD, "appeng.api.util.WorldCoord".replace('.','/'), "z", "I"));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        "br.com.gamemods.minecity.forge.base.protection.appeng.AppengHooks".replace('.','/'),
                        "onPlayerAccessWap",
                        "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;III)Z",
                        false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(ICONST_0));
                list.add(new InsnNode(IRETURN));
                list.add(labelNode);
                method.instructions.insert(list);
                break;
            }
        }
    }
}
