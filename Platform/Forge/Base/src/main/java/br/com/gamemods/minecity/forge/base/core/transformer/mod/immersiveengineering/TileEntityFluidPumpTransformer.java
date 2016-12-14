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
import org.objectweb.asm.tree.VarInsnNode;

import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class TileEntityFluidPumpTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        AtomicReference<MethodNode> wrapperRef = new AtomicReference<>();
        for(MethodNode method : node.methods)
        {
            while(true)
                if(CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("blusunrize/immersiveengineering/common/util/Utils"))
                        .filter(ins-> ins.name.equals("drainFluidBlock"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/world/World;IIIZ)Lnet/minecraftforge/fluids/FluidStack;"))
                        .noneMatch(ins-> {
                            MethodNode wrapper = wrapperRef.get();
                            if(wrapper == null)
                            {
                                wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$drainFluidBlock",
                                        "(Lnet/minecraft/world/World;IIIZLnet/minecraft/tileentity/TileEntity;)Lnet/minecraftforge/fluids/FluidStack;",
                                        null, null
                                );
                                wrapperRef.set(wrapper);
                                wrapper.visitCode();
                                wrapper.visitVarInsn(ALOAD, 5);
                                wrapper.visitVarInsn(ALOAD, 0);
                                wrapper.visitVarInsn(ILOAD, 1);
                                wrapper.visitVarInsn(ILOAD, 2);
                                wrapper.visitVarInsn(ILOAD, 3);
                                wrapper.visitVarInsn(ILOAD, 4);
                                wrapper.visitMethodInsn(INVOKESTATIC,
                                        "br/com/gamemods/minecity/forge/base/protection/immersiveengineering/ImmersiveHooks",
                                        "onBlockDrain",
                                        "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;IIIZ)Z",
                                        false
                                );
                                Label label = new Label();
                                wrapper.visitJumpInsn(IFEQ, label);
                                wrapper.visitInsn(ACONST_NULL);
                                wrapper.visitInsn(ARETURN);
                                wrapper.visitLabel(label);
                                wrapper.visitVarInsn(ALOAD, 0);
                                wrapper.visitVarInsn(ILOAD, 1);
                                wrapper.visitVarInsn(ILOAD, 2);
                                wrapper.visitVarInsn(ILOAD, 3);
                                wrapper.visitVarInsn(ILOAD, 4);
                                wrapper.visitMethodInsn(INVOKESTATIC, ins.owner, ins.name, ins.desc, ins.itf);
                                wrapper.visitInsn(ARETURN);
                                wrapper.visitEnd();
                            }

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.name = wrapper.name;
                            ins.owner = srg.replace('.','/');
                            ins.desc = wrapper.desc;
                            ins.itf = false;
                            return true;
                        }))
                {
                    break;
                }
        }

        node.methods.add(wrapperRef.get());

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        bytes = ModEnv.saveClass(srg, writer.toByteArray());
        return bytes;
    }
}
