package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

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
public class InventoryWorldControlMk2DClassTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("li.cil.oc.server.component.traits.InventoryWorldControlMk2$class"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("withInventory"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.name.equals("inventoryAt"))
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "world", "()Lscala/Option;", false));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "x", "()I", false));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "y", "()I", false));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "li/cil/oc/util/BlockPosition", "z", "()I", false));
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Enum", "ordinal", "()I", false));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks", "onInventoryControllerAccess",
                                    "(Lscala/Option;Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IInventoryWorldControlMk2;Lscala/Option;IIII)Lscala/Option;",
                                    false
                            ));
                            method.instructions.insert(ins, list);
                            return true;
                        });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
