package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class BlockDragonEggTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.block.BlockDragonEgg"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        method:
        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;" +
                    "Lnet/minecraft/util/EnumHand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumFacing;FFF)Z"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC, hookClass, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false));
                method.instructions.insert(list);
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while(iter.hasNext())
                {
                    AbstractInsnNode ins = iter.next();
                    if(ins.getOpcode() == ICONST_1)
                    {
                        list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new VarInsnNode(ALOAD, 4));
                        list.add(new VarInsnNode(ALOAD, 1));
                        list.add(new VarInsnNode(ALOAD, 2));
                        list.add(new VarInsnNode(ALOAD, 3));
                        list.add(new MethodInsnNode(INVOKESTATIC,
                                hookClass, "onDragonEggTeleport",
                                "(Lnet/minecraft/block/BlockDragonEgg;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V",
                                false
                        ));
                        method.instructions.insertBefore(ins, list);
                        break method;
                    }
                }
            }
            else if(method.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC, hookClass, "startCapturingBlocks", "(Lnet/minecraft/world/World;)V", false));
                method.instructions.insert(list);
                ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
                while(iter.hasNext())
                {
                    AbstractInsnNode ins = iter.next();
                    if(ins.getOpcode() == ICONST_1)
                    {
                        list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new VarInsnNode(ALOAD, 5));
                        list.add(new VarInsnNode(ALOAD, 1));
                        list.add(new VarInsnNode(ILOAD, 2));
                        list.add(new VarInsnNode(ILOAD, 3));
                        list.add(new VarInsnNode(ILOAD, 4));
                        list.add(new VarInsnNode(ILOAD, 6));
                        list.add(new MethodInsnNode(INVOKESTATIC,
                                hookClass, "onDragonEggTeleport",
                                "(Lnet/minecraft/block/BlockDragonEgg;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIII)V",
                                false
                        ));
                        method.instructions.insertBefore(ins, list);
                        break method;
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
