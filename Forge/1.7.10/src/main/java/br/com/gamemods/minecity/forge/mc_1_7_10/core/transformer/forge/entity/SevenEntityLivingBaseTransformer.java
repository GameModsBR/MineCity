package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.MethodPatcher;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.NoSuchElementException;

import static br.com.gamemods.minecity.api.CollectionUtil.stream;
import static org.objectweb.asm.Opcodes.*;

@Referenced(at = MineCitySevenCoreMod.class)
@MethodPatcher
public class SevenEntityLivingBaseTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.entity.EntityLivingBase"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        MethodNode getExp = node.methods.stream()
                .filter(m -> m.access == ACC_PROTECTED)
                .filter(m -> m.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)I"))
                .findFirst().orElseThrow(()-> new NoSuchElementException("getExperiencePoints(EntityPlayer player) was not found!"));

        node.methods.stream()
                .filter(m -> m.access == ACC_PROTECTED)
                .filter(m -> m.desc.equals("()V"))
                .anyMatch(method ->
                    stream(method.instructions.iterator())
                            .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                            .filter(ins-> ins.owner.equals("net/minecraft/entity/EntityLivingBase"))
                            .filter(ins-> ins.desc.equals(getExp.desc))
                            .filter(ins-> ins.name.equals(getExp.name))
                            .anyMatch(ins-> {
                                int i = method.instructions.indexOf(ins);
                                FieldInsnNode field = (FieldInsnNode) method.instructions.get(i-1);

                                InsnList list = new InsnList();
                                list.add(new VarInsnNode(ALOAD, 0));
                                list.add(new VarInsnNode(ALOAD, 0));
                                list.add(new FieldInsnNode(field.getOpcode(), field.owner, field.name, field.desc));
                                list.add(new MethodInsnNode(INVOKESTATIC,
                                        "br/com/gamemods/minecity/forge/mc_1_7_10/protection/MineCitySevenHooks",
                                        "getExperienceDrop", "(ILnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/player/EntityPlayer;)I",
                                        false
                                ));
                                method.instructions.insert(ins, list);
                                return true;
                            })
                );

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
