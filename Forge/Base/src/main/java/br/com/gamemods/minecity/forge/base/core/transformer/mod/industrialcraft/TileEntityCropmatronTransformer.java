package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class TileEntityCropmatronTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public TileEntityCropmatronTransformer()
    {
        super("ic2.core.block.machine.tileentity.TileEntityCropmatron");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("scan"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(III)Lnet/minecraft/tileentity/TileEntity;")
                                    ||ins.desc.equals("(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"))
                        .anyMatch(ins->{
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                                    "onTileAccessOther",
                                    "(Lbr.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;Lbr.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;)Lbr.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;"
                                        .replace('.','/'),
                                    false
                            ));
                            list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/tileentity/TileEntity"));
                            method.instructions.insert(ins, list);
                            return true;
                        });
                break;
            }
        }
    }
}
