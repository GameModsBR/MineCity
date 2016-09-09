package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @deprecated Used to fire a custom event but there's a forge event available for the same thing.
 */
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@Deprecated
@MethodPatcher
public class EntityArmorStandTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.item.EntityArmorStand"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.','/');

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;"))
            {
                InsnList add = new InsnList();
                add.add(new VarInsnNode(ALOAD, 0));
                add.add(new VarInsnNode(ALOAD, 1));
                add.add(new VarInsnNode(ALOAD, 2));
                add.add(new VarInsnNode(ALOAD, 3));
                add.add(new VarInsnNode(ALOAD, 4));
                add.add(new MethodInsnNode(INVOKESTATIC,
                        hookClass, "onPrecisePlayerInteraction",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/EnumActionResult;",
                        false
                ));
                add.add(new InsnNode(DUP));
                LabelNode elseNode = new LabelNode(new Label());
                add.add(new JumpInsnNode(IFNULL, elseNode));
                add.add(new InsnNode(ARETURN));
                add.add(elseNode);
                add.add(new InsnNode(POP));
                method.instructions.insertBefore(method.instructions.get(0), add);
                break;
            }
        }

        node.interfaces.add("br/com/gamemods/minecity/forge/base/accessors/entity/item/IEntityArmorStand");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
