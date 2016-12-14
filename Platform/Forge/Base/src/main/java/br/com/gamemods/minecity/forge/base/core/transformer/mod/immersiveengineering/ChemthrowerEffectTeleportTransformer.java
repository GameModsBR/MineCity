package br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class ChemthrowerEffectTeleportTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!"blusunrize.immersiveengineering.common.util.compat.ThermalFoundationHelper$ChemthrowerEffect_Teleport".equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        MethodNode wrapper = new MethodNode(ACC_PUBLIC + ACC_STATIC, "mineCity$wrap",
                "(Lnet/minecraftforge/event/entity/living/EnderTeleportEvent;Lblusunrize/immersiveengineering/common/util/compat/ThermalFoundationHelper$ChemthrowerEffect_Teleport;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraftforge/event/entity/living/EnderTeleportEvent;",
                null, null
        );
        wrapper.visitCode();
        wrapper.visitVarInsn(ALOAD, 0);
        if(ModEnv.seven)
        {
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitFieldInsn(GETFIELD, "net/minecraftforge/event/entity/living/LivingEvent", "entityLiving", "Lnet/minecraft/entity/EntityLivingBase;");
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitFieldInsn(GETFIELD, "net/minecraftforge/event/entity/living/EnderTeleportEvent", "targetX", "D");
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitFieldInsn(GETFIELD, "net/minecraftforge/event/entity/living/EnderTeleportEvent", "targetY", "D");
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitFieldInsn(GETFIELD, "net/minecraftforge/event/entity/living/EnderTeleportEvent", "targetZ", "D");
        }
        else
        {
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitMethodInsn(INVOKEVIRTUAL, "net/minecraftforge/event/entity/living/LivingEvent", "getEntityLiving", "()Lnet/minecraft/entity/EntityLivingBase;", false);
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitMethodInsn(INVOKEVIRTUAL, "net/minecraftforge/event/entity/living/EnderTeleportEvent", "getTargetX", "()D", false);
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitMethodInsn(INVOKEVIRTUAL, "net/minecraftforge/event/entity/living/EnderTeleportEvent", "getTargetY", "()D", false);
            wrapper.visitVarInsn(ALOAD, 0);
            wrapper.visitMethodInsn(INVOKEVIRTUAL, "net/minecraftforge/event/entity/living/EnderTeleportEvent", "getTargetZ", "()D", false);
        }
        wrapper.visitVarInsn(ALOAD, 1);
        wrapper.visitVarInsn(ALOAD, 2);
        wrapper.visitMethodInsn(INVOKESTATIC,
                "br/com/gamemods/minecity/forge/base/protection/immersiveengineering/ImmersiveHooks",
                "onChemthrowerTeleport",
                "(Lnet/minecraftforge/event/entity/living/EnderTeleportEvent;Lbr/com/gamemods/minecity/forge/base/accessors/entity/base/IEntity;DDDLbr/com/gamemods/minecity/forge/base/protection/immersiveengineering/IChemthrowerEffect;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraftforge/event/entity/living/EnderTeleportEvent;",
                false
        );
        wrapper.visitInsn(ARETURN);
        wrapper.visitEnd();
        node.methods.add(wrapper);


        node.methods.stream().filter(method-> method.name.equals("applyToEntity"))
                .anyMatch(method -> {
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKESPECIAL).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.name.equals("<init>"))
                            .filter(ins-> ins.owner.equals("net/minecraftforge/event/entity/living/EnderTeleportEvent"))
                            .anyMatch(ins-> {
                                InsnList list = new InsnList();
                                list.add(new VarInsnNode(ALOAD, 0));
                                list.add(new VarInsnNode(ALOAD, 2));
                                list.add(new MethodInsnNode(INVOKESTATIC,
                                        "blusunrize/immersiveengineering/common/util/compat/ThermalFoundationHelper$ChemthrowerEffect_Teleport",
                                        wrapper.name, wrapper.desc, false
                                ));
                                method.instructions.insert(ins, list);
                                return true;
                            });
                    return true;
                });

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
