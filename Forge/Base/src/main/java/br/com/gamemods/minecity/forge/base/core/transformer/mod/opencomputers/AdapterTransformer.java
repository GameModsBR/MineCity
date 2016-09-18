package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class AdapterTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("li.cil.oc.common.tileentity.Adapter"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("neighborChanged"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;")
                                    ||ins.desc.equals("(III)Lnet/minecraft/tileentity/TileEntity;"))
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks",
                                    "onAdapterAccess", "(Lbr/com/gamemods/minecity/forge/base/accessors/block/ITileEntity;Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IAdapter;)Lnet/minecraft/tileentity/TileEntity;",
                                    false
                            ));
                            method.instructions.insert(ins, list);
                            return true;
                        });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
