package br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class ChemthrowerHandlerTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.startsWith("blusunrize.immersiveengineering.api.tool.ChemthrowerHandler"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        node.methods.forEach(method-> {
            while(true)
            {
                if(CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("net/minecraft/entity/EntityLivingBase"))
                        .filter(ins -> ins.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z"))
                        .noneMatch(ins -> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/immersiveengineering/ImmersiveHooks",
                                    "onChemthrowerDamage",
                                    "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;FLbr/com/gamemods/minecity/forge/base/protection/immersiveengineering/IChemthrowerEffect;Lnet/minecraft/entity/player/EntityPlayer;)Z",
                                    false
                            ));
                            method.instructions.insertBefore(ins, list);
                            method.instructions.remove(ins);
                            return true;
                        }) &&
                    CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("net/minecraft/entity/EntityLivingBase"))
                        .filter(ins -> ins.desc.equals("(Lnet/minecraft/potion/PotionEffect;)V"))
                        .noneMatch(ins -> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
                            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/immersiveengineering/ImmersiveHooks",
                                    "onPotionApplyEffect",
                                    "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/PotionEffect;Lbr/com/gamemods/minecity/forge/base/protection/immersiveengineering/IChemthrowerEffect;Lnet/minecraft/entity/player/EntityPlayer;)V",
                                    false
                            ));
                            method.instructions.insertBefore(ins, list);
                            method.instructions.remove(ins);
                            return true;
                        })
                )
                {
                    break;
                }
            }
        });

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
