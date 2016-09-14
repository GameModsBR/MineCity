package br.com.gamemods.minecity.forge.base.core.transformer.mod.mrcrayfishfurniture;

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
public class MessageTVServerTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("com.mrcrayfish.furniture.network.message.MessageTVServer"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("onMessage"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.desc.equals("(III)Lnet/minecraft/tileentity/TileEntity;"))
                        .anyMatch(ins -> {
                            int index = method.instructions.indexOf(ins);
                            VarInsnNode var = (VarInsnNode) method.instructions.get(index+1);
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, var.var-1));
                            list.add(new VarInsnNode(ALOAD, var.var));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/mrcrayfishfurniture/FurnitureHooks",
                                    "onPlayerChangeTVChannel",
                                    "(Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;Lbr.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;)Z"
                                            .replace('.','/'),
                                    false
                            ));
                            LabelNode labelNode = new LabelNode();
                            list.add(new JumpInsnNode(IFEQ, labelNode));
                            list.add(new InsnNode(ACONST_NULL));
                            list.add(new InsnNode(ARETURN));
                            list.add(labelNode);
                            method.instructions.insert(var, list);
                            return true;
                        });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = ModEnv.saveClass(srg, writer.toByteArray());
        return bytes;
    }
}
