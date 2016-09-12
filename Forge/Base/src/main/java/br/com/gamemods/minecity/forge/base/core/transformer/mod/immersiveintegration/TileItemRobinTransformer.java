package br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveintegration;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class TileItemRobinTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!"unwrittenfun.minecraft.immersiveintegration.tiles.TileItemRobin".equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("()V") && method.access == ACC_PUBLIC && !method.name.equals("<init>"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(III)Lnet/minecraft/tileentity/TileEntity;"))
                        .anyMatch(ins-> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$getTileEntity",
                                    "(Lnet/minecraft/world/World;IIILnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/tileentity/TileEntity;",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 4);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(INVOKEVIRTUAL, ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/immersiveintegrations/ImmersiveIntegrationHooks",
                                    "onTileOpen",
                                    "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;III)Lnet/minecraft/tileentity/TileEntity;",
                                    false
                            );
                            wrapper.visitInsn(ARETURN);
                            wrapper.visitEnd();
                            node.methods.add(wrapper);

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.name = wrapper.name;
                            ins.owner = transformedName.replace('.','/');
                            ins.desc = wrapper.desc;
                            ins.itf = false;
                            return true;
                        });

                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
