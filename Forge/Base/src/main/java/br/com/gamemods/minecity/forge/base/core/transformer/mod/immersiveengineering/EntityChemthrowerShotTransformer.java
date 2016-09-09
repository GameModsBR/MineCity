package br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityChemthrowerShotTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String srg, byte[] bytes)
    {
        if(!"blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot".equals(srg))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        node.methods.forEach(method-> {
            while(true)
            {
                if(CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.owner.equals("net/minecraft/entity/Entity"))
                        .filter(ins -> ins.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z"))
                        .noneMatch(ins -> {
                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.owner = "br/com/gamemods/minecity/forge/base/protection/immersiveengineering/ImmersiveHooks";
                            ins.desc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;FLnet/minecraft/entity/projectile/EntityArrow;)Z";
                            ins.name = "onChemthrowerDamage";
                            return true;
                        }))
                {
                    break;
                }
            }
        });

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = ModEnv.saveClass(srg, writer.toByteArray());
        return bytes;
    }
}
