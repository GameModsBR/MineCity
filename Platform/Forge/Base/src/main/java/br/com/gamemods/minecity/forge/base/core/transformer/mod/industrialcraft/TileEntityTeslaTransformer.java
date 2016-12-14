package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@MethodPatcher
public class TileEntityTeslaTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public TileEntityTeslaTransformer()
    {
        super("ic2.core.block.machine.tileentity.TileEntityTesla");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("shock"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;")
                                    ||ins.desc.equals("(Ljava/lang/Class;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"))
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                                    "onTeslaDamage", "(Ljava/util/List;Lbr.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;)Ljava/util/List;".replace('.','/'), false));
                            method.instructions.insert(ins, list);
                            return true;
                        });
                break;
            }
        }
    }
}
