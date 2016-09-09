package br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EventHandlerTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("codechicken.multipart.minecraft.EventHandler"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(!(method.name.equals("place") && method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;)Z")))
                continue;

            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins -> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                    .filter(ins -> ins.owner.equals("codechicken/multipart/TileMultipart"))
                    .filter(ins -> ins.name.equals("addPart"))
                    .filter(ins -> ins.desc.equals(
                            "(Lnet/minecraft/world/World;Lcodechicken/lib/vec/BlockCoord;Lcodechicken/multipart/TMultiPart;)Lcodechicken/multipart/TileMultipart;"))
                    .anyMatch(ins ->
                    {
                        MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "addPart$MCHook",
                                "(Lnet/minecraft/world/World;Lcodechicken/lib/vec/BlockCoord;Lcodechicken/multipart/TMultiPart;Lnet/minecraft/entity/player/EntityPlayer;)Lcodechicken/multipart/TileMultipart;",
                                null, null
                        );

                        wrapper.visitCode();
                        wrapper.visitVarInsn(ALOAD, 3);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ALOAD, 1);
                        wrapper.visitFieldInsn(GETFIELD, "codechicken/lib/vec/BlockCoord", "x", "I");
                        wrapper.visitVarInsn(ALOAD, 1);
                        wrapper.visitFieldInsn(GETFIELD, "codechicken/lib/vec/BlockCoord", "y", "I");
                        wrapper.visitVarInsn(ALOAD, 1);
                        wrapper.visitFieldInsn(GETFIELD, "codechicken/lib/vec/BlockCoord", "z", "I");
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                "br/com/gamemods/minecity/forge/base/protection/forgemultipart/MultiPartHooks",
                                "onPartPlace",
                                "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;III)Z",
                                false
                        );
                        Label label = new Label();
                        wrapper.visitJumpInsn(IFEQ, label);
                        wrapper.visitInsn(ACONST_NULL);
                        wrapper.visitInsn(ARETURN);
                        wrapper.visitLabel(label);

                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ALOAD, 1);
                        wrapper.visitVarInsn(ALOAD, 2);
                        wrapper.visitMethodInsn(INVOKESTATIC, ins.owner, ins.name, ins.desc, ins.itf);
                        wrapper.visitInsn(ARETURN);
                        wrapper.visitEnd();

                        node.methods.add(wrapper);

                        int index = method.instructions.indexOf(ins);
                        method.instructions.remove(method.instructions.get(index + 1));

                        InsnList list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new MethodInsnNode(INVOKESTATIC,
                                "codechicken/multipart/minecraft/EventHandler", "addPart$MCHook",
                                wrapper.desc, false
                        ));
                        LabelNode labelNode = new LabelNode();
                        list.add(new JumpInsnNode(IFNONNULL, labelNode));
                        list.add(new InsnNode(ICONST_0));
                        list.add(new InsnNode(IRETURN));
                        list.add(labelNode);

                        method.instructions.insert(ins, list);
                        method.instructions.remove(ins);

                        return true;
                    });
            break;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
