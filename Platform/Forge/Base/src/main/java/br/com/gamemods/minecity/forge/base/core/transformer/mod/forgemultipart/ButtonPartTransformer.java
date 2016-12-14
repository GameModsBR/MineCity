package br.com.gamemods.minecity.forge.base.core.transformer.mod.forgemultipart;

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
public class ButtonPartTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("codechicken.multipart.minecraft.ButtonPart"))
            return bytes;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        node.methods.stream()
                .filter(method -> method.name.equals("updateState") && method.desc.equals("()V"))
                .forEachOrdered(method ->
                    CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                        .filter(ins -> ins.name.equals("isEmpty"))
                        .filter(ins -> ins.owner.equals("java/util/List"))
                        .filter(ins -> ins.desc.equals("()Z"))
                        .anyMatch(ins -> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new TypeInsnNode(CHECKCAST, "br/com/gamemods/minecity/forge/base/protection/forgemultipart/IButtonPart"));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/forgemultipart/MultiPartHooks",
                                    "onWoodButtonFindEntity", "(Ljava/util/List;Lbr/com/gamemods/minecity/forge/base/protection/forgemultipart/IButtonPart;)Ljava/util/List;",
                                    false
                            ));
                            method.instructions.insertBefore(ins, list);
                            return true;
                        })
                );

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
