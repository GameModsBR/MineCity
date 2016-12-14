package br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering;

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

import static org.objectweb.asm.Opcodes.*;


@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class ItemIEToolTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!"blusunrize.immersiveengineering.common.items.ItemIETool".equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$createStructure",
                "(Lblusunrize/immersiveengineering/api/MultiblockHandler$IMultiblock;Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;)Z", null, null
        );
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ILOAD, 2);
        wrapper.visitVarInsn(ILOAD, 3);
        wrapper.visitVarInsn(ILOAD, 4);
        wrapper.visitVarInsn(ILOAD, 5);
        wrapper.visitVarInsn(ALOAD, 6);
        wrapper.visitMethodInsn(INVOKESTATIC,
                "br/com/gamemods/minecity/forge/base/protection/immersiveengineering/ImmersiveHooks",
                "preCreateStructure",
                "(Ljava/lang/Object;Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;)Z",
                false
        );
        Label label = new Label();
        wrapper.visitJumpInsn(IFEQ, label);
        wrapper.visitInsn(ICONST_0);
        wrapper.visitInsn(IRETURN);
        wrapper.visitLabel(label);

        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ILOAD, 2);
        wrapper.visitVarInsn(ILOAD, 3);
        wrapper.visitVarInsn(ILOAD, 4);
        wrapper.visitVarInsn(ILOAD, 5);
        wrapper.visitVarInsn(ALOAD, 6);
        wrapper.visitMethodInsn(INVOKEINTERFACE,
                "blusunrize/immersiveengineering/api/MultiblockHandler$IMultiblock",
                "createStructure",
                "(Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;)Z",
                true
        );

        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ILOAD, 2);
        wrapper.visitVarInsn(ILOAD, 3);
        wrapper.visitVarInsn(ILOAD, 4);
        wrapper.visitVarInsn(ILOAD, 5);
        wrapper.visitVarInsn(ALOAD, 6);
        wrapper.visitMethodInsn(INVOKESTATIC,
                "br/com/gamemods/minecity/forge/base/protection/immersiveengineering/ImmersiveHooks",
                "postCreateStructure",
                "(ZLjava/lang/Object;Lnet/minecraft/world/World;IIIILnet/minecraft/entity/player/EntityPlayer;)Z",
                false
        );
        wrapper.visitInsn(IRETURN);
        wrapper.visitEnd();
        node.methods.add(wrapper);


        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.name.equals("createStructure"))
                        .anyMatch(ins -> {
                            ins.setOpcode(INVOKESTATIC);
                            ins.owner = transformedName.replace('.','/');
                            ins.desc = wrapper.desc;
                            ins.itf = false;
                            ins.name = wrapper.name;
                            return true;
                        });
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
