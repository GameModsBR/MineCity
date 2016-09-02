package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class BlockTNTTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.block.BlockTNT"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        node.interfaces.add("br/com/gamemods/minecity/forge/base/accessors/block/IBlockTNT");

        for(MethodNode method : node.methods)
        {
            boolean frost = method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V");
            boolean seven = method.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)V");
            if(frost || seven)
            {
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                int index = -1;
                while(iter.hasNext())
                {
                    index++;
                    AbstractInsnNode ins = iter.next();
                    if(ins.getOpcode() == CHECKCAST)
                    {
                        InsnList add = new InsnList();
                        if(frost)
                        {
                            add.add(new VarInsnNode(ALOAD, 1));
                            add.add(new VarInsnNode(ALOAD, 2));
                            add.add(new VarInsnNode(ALOAD, 3));
                            add.add(new VarInsnNode(ALOAD, 4));
                            add.add(new TypeInsnNode(CHECKCAST, ((TypeInsnNode) ins).desc));
                            add.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onArrowIgnite",
                                    "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/projectile/EntityArrow;)Z",
                                    false
                            ));
                        }
                        else
                        {
                            add.add(new VarInsnNode(ALOAD, 1));
                            add.add(new VarInsnNode(ILOAD, 2));
                            add.add(new VarInsnNode(ILOAD, 3));
                            add.add(new VarInsnNode(ILOAD, 4));
                            add.add(new VarInsnNode(ALOAD, 0));
                            add.add(new VarInsnNode(ALOAD, 5));
                            add.add(new TypeInsnNode(CHECKCAST, ((TypeInsnNode) ins).desc));
                            add.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onArrowIgnite",
                                    "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;Lnet/minecraft/entity/projectile/EntityArrow;)Z",
                                    false
                            ));
                        }

                        LabelNode elseLabel = new LabelNode(new Label());
                        add.add(new JumpInsnNode(IFEQ, elseLabel));
                        add.add(new InsnNode(RETURN));
                        add.add(elseLabel);

                        method.instructions.insertBefore(method.instructions.get(index-1), add);
                        break;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
