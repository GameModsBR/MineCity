package br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class BlockMultiPartTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("codechicken.multipart.BlockMultipart"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            ListIterator<AbstractInsnNode> iter = method.instructions.iterator();
            while(iter.hasNext())
            {
                AbstractInsnNode ins = iter.next();
                if(ins.getOpcode() == INVOKEVIRTUAL)
                {
                    MethodInsnNode methodNode = (MethodInsnNode) ins;
                    if((methodNode.name.equals("activate") || methodNode.name.equals("click"))
                            && methodNode.owner.equals("codechicken/multipart/TMultiPart")
                            && methodNode.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/item/ItemStack;)Z"))
                    {
                        methodNode.setOpcode(INVOKESTATIC);
                        methodNode.owner = "codechicken/multipart/BlockMultipart";
                        methodNode.name = "MineCity$"+methodNode.name;
                        methodNode.desc = "(Lcodechicken/multipart/TMultiPart;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/item/ItemStack;)Z";
                    }
                }
            }
        }

        MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "MineCity$activate",
                "(Lcodechicken/multipart/TMultiPart;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/item/ItemStack;)Z",
                null, null
        );

        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/protection/forgemultipart/ITMultiPart");
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitMethodInsn(INVOKESTATIC, "br/com/gamemods/minecity/forge/base/protection/forgemultipart/MultiPartHooks",
                "onPartActivate", "(Lbr/com/gamemods/minecity/forge/base/protection/forgemultipart/ITMultiPart;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z",
                false
        );
        Label label = new Label();
        wrapper.visitJumpInsn(IFEQ, label);
        wrapper.visitInsn(ICONST_0);
        wrapper.visitInsn(IRETURN);
        wrapper.visitLabel(label);

        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ALOAD, 2);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "codechicken/multipart/TMultiPart", "activate",
                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/item/ItemStack;)Z",
                false
        );
        wrapper.visitInsn(IRETURN);
        wrapper.visitEnd();

        node.methods.add(wrapper);

        wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "MineCity$click",
                "(Lcodechicken/multipart/TMultiPart;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/item/ItemStack;)Z",
                null, null
        );

        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/protection/forgemultipart/ITMultiPart");
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitMethodInsn(INVOKESTATIC, "br/com/gamemods/minecity/forge/base/protection/forgemultipart/MultiPartHooks",
                "onPartClick", "(Lbr/com/gamemods/minecity/forge/base/protection/forgemultipart/ITMultiPart;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;)Z",
                false
        );
        label = new Label();
        wrapper.visitJumpInsn(IFEQ, label);
        wrapper.visitInsn(ICONST_0);
        wrapper.visitInsn(IRETURN);
        wrapper.visitLabel(label);

        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ALOAD, 2);
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitMethodInsn(INVOKEVIRTUAL, "codechicken/multipart/TMultiPart", "click",
                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MovingObjectPosition;Lnet/minecraft/item/ItemStack;)Z",
                false
        );
        wrapper.visitInsn(IRETURN);
        wrapper.visitEnd();

        node.methods.add(wrapper);


        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
