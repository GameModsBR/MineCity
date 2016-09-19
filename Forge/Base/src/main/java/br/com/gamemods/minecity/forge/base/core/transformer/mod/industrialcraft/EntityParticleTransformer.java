package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityParticleTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("ic2.core.item.tool.EntityParticle"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        AtomicReference<MethodNode> setBlock = new AtomicReference<>();
        AtomicReference<MethodNode> setBlockMeta = new AtomicReference<>();
        AtomicReference<MethodNode> setBlockToAir = new AtomicReference<>();

        String hookClass = ModEnv.hookClass.replace('.', '/');

        for(MethodNode method : node.methods)
        {
            // 1.7.10
            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                    .filter(ins-> ins.desc.equals("(IIILnet/minecraft/block/Block;II)Z"))
                    .map(ins-> method.instructions.indexOf(ins))
                    .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                    .map(method.instructions::get).map(MethodInsnNode.class::cast)
                    .forEachOrdered(ins-> {
                        MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC,
                                "mineCity$setBlock",
                                "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;IIL"+transformedName.replace('.','/')+";)Z",
                                null, null
                        );
                        wrapper.visitCode();
                        wrapper.visitVarInsn(ALOAD, 7);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                hookClass, "toPoint", "(III)Lbr/com/gamemods/minecity/api/shape/Point;",
                                false
                        );
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                "br/com/gamemods/minecity/forge/base/protection/industrialcraft/ICHooks",
                                "onEntityChangeBlock", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                false
                        );
                        Label label = new Label();
                        wrapper.visitJumpInsn(IFEQ, label);
                        wrapper.visitInsn(ICONST_0);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitLabel(label);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitVarInsn(ALOAD, 4);
                        wrapper.visitVarInsn(ILOAD, 5);
                        wrapper.visitVarInsn(ILOAD, 6);
                        wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitEnd();
                        setBlockMeta.set(wrapper);
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });

            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                    .filter(ins-> ins.desc.equals("(IIILnet/minecraft/block/Block;)Z"))
                    .map(ins-> method.instructions.indexOf(ins))
                    .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                    .map(method.instructions::get).map(MethodInsnNode.class::cast)
                    .forEachOrdered(ins-> {
                        MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC,
                                "mineCity$setBlock",
                                "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;L"+transformedName.replace('.','/')+";)Z",
                                null, null
                        );
                        wrapper.visitCode();
                        wrapper.visitVarInsn(ALOAD, 5);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                hookClass, "toPoint", "(III)Lbr/com/gamemods/minecity/api/shape/Point;",
                                false
                        );
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                "br/com/gamemods/minecity/forge/base/protection/industrialcraft/ICHooks",
                                "onEntityChangeBlock", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                false
                        );
                        Label label = new Label();
                        wrapper.visitJumpInsn(IFEQ, label);
                        wrapper.visitInsn(ICONST_0);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitLabel(label);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitVarInsn(ALOAD, 4);
                        wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitEnd();
                        setBlock.set(wrapper);
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });

            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                    .filter(ins-> ins.desc.equals("(III)Z"))
                    .map(ins-> method.instructions.indexOf(ins))
                    .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                    .map(method.instructions::get).map(MethodInsnNode.class::cast)
                    .forEachOrdered(ins-> {
                        MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC,
                                "mineCity$setBlockToAir",
                                "(Lnet/minecraft/world/World;IIIL"+transformedName.replace('.','/')+";)Z",
                                null, null
                        );
                        wrapper.visitCode();
                        wrapper.visitVarInsn(ALOAD, 4);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                hookClass, "toPoint", "(III)Lbr/com/gamemods/minecity/api/shape/Point;",
                                false
                        );
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                "br/com/gamemods/minecity/forge/base/protection/industrialcraft/ICHooks",
                                "onEntityChangeBlock", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                false
                        );
                        Label label = new Label();
                        wrapper.visitJumpInsn(IFEQ, label);
                        wrapper.visitInsn(ICONST_0);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitLabel(label);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ILOAD, 1);
                        wrapper.visitVarInsn(ILOAD, 2);
                        wrapper.visitVarInsn(ILOAD, 3);
                        wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitEnd();
                        setBlockToAir.set(wrapper);
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });

            // 1.10.2
            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                    .filter(ins-> ins.desc.equals("(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z"))
                    .map(ins-> method.instructions.indexOf(ins))
                    .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                    .map(method.instructions::get).map(MethodInsnNode.class::cast)
                    .forEachOrdered(ins-> {
                        MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC,
                                "mineCity$setBlock",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;L"+transformedName.replace('.','/')+";)Z",
                                null, null
                        );
                        wrapper.visitCode();
                        wrapper.visitVarInsn(ALOAD, 3);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ALOAD, 1);
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                hookClass, "toPoint", "(Ljava/lang/Object;)Lbr/com/gamemods/minecity/api/shape/Point;",
                                false
                        );
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                "br/com/gamemods/minecity/forge/base/protection/industrialcraft/ICHooks",
                                "onEntityChangeBlock", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
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
                        wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitEnd();
                        setBlockMeta.set(wrapper);
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });

            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                    .filter(ins-> ins.desc.equals("(Lnet/minecraft/util/math/BlockPos;)Z"))
                    .map(ins-> method.instructions.indexOf(ins))
                    .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                    .map(method.instructions::get).map(MethodInsnNode.class::cast)
                    .forEachOrdered(ins-> {
                        MethodNode wrapper = new MethodNode(ACC_PUBLIC+ACC_STATIC,
                                "mineCity$setBlockToAir",
                                "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;L"+transformedName.replace('.','/')+";)Z",
                                null, null
                        );
                        wrapper.visitCode();
                        wrapper.visitVarInsn(ALOAD, 2);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ALOAD, 1);
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                hookClass, "toPoint", "(Ljava/lang/Object;)Lbr/com/gamemods/minecity/api/shape/Point;",
                                false
                        );
                        wrapper.visitMethodInsn(INVOKESTATIC,
                                "br/com/gamemods/minecity/forge/base/protection/industrialcraft/ICHooks",
                                "onEntityChangeBlock", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                false
                        );
                        Label label = new Label();
                        wrapper.visitJumpInsn(IFEQ, label);
                        wrapper.visitInsn(ICONST_0);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitLabel(label);
                        wrapper.visitVarInsn(ALOAD, 0);
                        wrapper.visitVarInsn(ALOAD, 1);
                        wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                        wrapper.visitInsn(IRETURN);
                        wrapper.visitEnd();
                        setBlockToAir.set(wrapper);
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = transformedName.replace('.','/');
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });
        }

        if(ModEnv.seven)
            node.methods.add(Objects.requireNonNull(setBlock.get(), "Failed to patch the setBlock() call"));
        node.methods.add(Objects.requireNonNull(setBlockMeta.get(), "Failed to patch the setBlock(IBlockState) call"));
        node.methods.add(Objects.requireNonNull(setBlockToAir.get(), "Failed to patch the setBlockToAir() call"));

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
