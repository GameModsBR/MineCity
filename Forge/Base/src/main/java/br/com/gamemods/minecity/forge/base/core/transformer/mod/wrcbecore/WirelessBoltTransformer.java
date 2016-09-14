package br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbecore;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class WirelessBoltTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!"codechicken.wirelessredstone.core.WirelessBolt".equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        node.interfaces.add("br/com/gamemods/minecity/forge/base/protection/wrcbe/IWirelessBolt");

        MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$etherJamEntity",
                "(Lcodechicken/wirelessredstone/core/RedstoneEther;Lnet/minecraft/entity/EntityLivingBase;ZLcodechicken/wirelessredstone/core/WirelessBolt;)V",
                null, null
        );
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 3);
        wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/protection/wrcbe/IWirelessBolt");
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitTypeInsn(CHECKCAST, "br/com/gamemods/minecity/forge/base/accessors/entity/base/IEntity");
        wrapper.visitMethodInsn(INVOKESTATIC, "br/com/gamemods/minecity/forge/base/protection/wrcbe/WRCBEHooks",
                "onBoltJamEntity", "(Lbr/com/gamemods/minecity/forge/base/protection/wrcbe/IWirelessBolt;Lbr/com/gamemods/minecity/forge/base/accessors/entity/base/IEntity;)Z",
                false
        );
        final Label label = new Label();
        wrapper.visitJumpInsn(IFNE, label);
        wrapper.visitVarInsn(ALOAD, 0);
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ILOAD, 2);
        AtomicBoolean completed = new AtomicBoolean();

        MethodNode jamWrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$etherJamNode",
                "(Lcodechicken/wirelessredstone/core/RedstoneEtherServer;Lnet/minecraft/world/World;Lcodechicken/lib/vec/BlockCoord;IILcodechicken/wirelessredstone/core/ITileWireless;Lcodechicken/wirelessredstone/core/WirelessBolt;)V",
                null, null
        );
        jamWrapper.visitCode();
        jamWrapper.visitVarInsn(ALOAD, 5);
        jamWrapper.visitVarInsn(ALOAD, 6);
        jamWrapper.visitMethodInsn(INVOKESTATIC,
                "br/com/gamemods/minecity/forge/base/protection/wrcbe/WRCBEHooks", "onBoltJamTile",
                "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                false
        );
        Label label2 = new Label();
        jamWrapper.visitJumpInsn(IFNE, label2);
        jamWrapper.visitVarInsn(ALOAD, 0);
        jamWrapper.visitVarInsn(ALOAD, 1);
        jamWrapper.visitVarInsn(ALOAD, 2);
        jamWrapper.visitVarInsn(ILOAD, 3);
        jamWrapper.visitVarInsn(ILOAD, 4);
        jamWrapper.visitMethodInsn(INVOKEVIRTUAL,
                "codechicken/wirelessredstone/core/RedstoneEtherServer", "jamNode",
                "(Lnet/minecraft/world/World;Lcodechicken/lib/vec/BlockCoord;II)V",
                false
        );
        jamWrapper.visitVarInsn(ALOAD, 5);
        jamWrapper.visitMethodInsn(INVOKEINTERFACE,
                "codechicken/wirelessredstone/core/ITileWireless",
                "jamTile", "()V", true
        );
        jamWrapper.visitLabel(label2);
        jamWrapper.visitInsn(RETURN);
        jamWrapper.visitEnd();



        for(MethodNode method : node.methods)
        {
            if(method.name.equals("jamTile"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("codechicken/wirelessredstone/core/RedstoneEtherServer"))
                        .filter(ins -> ins.name.equals("jamNode"))
                        .anyMatch(ins ->
                        {
                            AbstractInsnNode prev = method.instructions.get(method.instructions.indexOf(ins)-1);
                            ListIterator<AbstractInsnNode> iter = method.instructions.iterator(
                                    method.instructions.indexOf(ins)
                            );
                            int var = -1;
                            while(true)
                            {
                                AbstractInsnNode other = iter.next();
                                iter.remove();
                                int opcode = other.getOpcode();
                                if(opcode == INVOKEINTERFACE)
                                {
                                    MethodInsnNode mInst = (MethodInsnNode)other;
                                    if(mInst.name.equals("jamTile"))
                                        break;
                                }
                                else if(opcode == ALOAD)
                                    var = ((VarInsnNode) other).var;
                            }

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, var));
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    transformedName.replace('.','/'),
                                    jamWrapper.name, jamWrapper.desc, false
                            ));
                            method.instructions.insert(prev, list);
                            return true;
                        });
            }
            else if(method.name.equals("vecBBDamageSegment"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z"))
                        .map(ins-> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                        .forEachOrdered(index -> {
                            MethodInsnNode ins = (MethodInsnNode) method.instructions.get(index);
                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.owner = "br/com/gamemods/minecity/forge/base/protection/wrcbe/WRCBEHooks";
                            ins.desc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;FLbr/com/gamemods/minecity/forge/base/protection/wrcbe/IWirelessBolt;)Z";
                            ins.itf = false;
                            ins.name = "onBoltAttackEntity";
                        });

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.name.equals("jamEntity"))
                        .filter(ins-> ins.owner.equals("codechicken/wirelessredstone/core/RedstoneEther"))
                        .anyMatch(ins -> {
                            if(!completed.get())
                            {
                                wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                                wrapper.visitLabel(label);
                                wrapper.visitInsn(RETURN);
                                wrapper.visitEnd();
                                completed.set(true);
                            }

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            ins.owner = transformedName.replace('.','/');
                            ins.itf = false;
                            return true;
                        });
            }
        }

        node.methods.add(wrapper);
        node.methods.add(jamWrapper);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
