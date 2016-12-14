package br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbe;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Comparator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class JammerPartTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if(!transformedName.equals("codechicken.wirelessredstone.logic.JammerPart"))
            return basicClass;

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        String jammerPart = "br/com/gamemods/minecity/forge/base/protection/wrcbe/IJammerPart";
        String bolt = "br/com/gamemods/minecity/forge/base/protection/wrcbe/IWirelessBolt";
        node.interfaces.add(jammerPart);

        for(MethodNode method : node.methods)
        {
            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKESPECIAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.name.equals("<init>"))
                    .filter(ins-> ins.owner.equals("codechicken/wirelessredstone/core/WirelessBolt"))
                    .map(ins-> method.instructions.indexOf(ins))
                    .sorted(Comparator.reverseOrder()).map(Integer::intValue)
                    .forEachOrdered(index -> {
                        InsnList list = new InsnList();
                        list = new InsnList();
                        list.add(new VarInsnNode(ALOAD, 0));
                        list.add(new MethodInsnNode(INVOKEINTERFACE,
                                bolt, "createdFromPart", "(L"+jammerPart+";)L"+bolt+";",
                                true
                        ));
                        list.add(new TypeInsnNode(CHECKCAST, "codechicken/wirelessredstone/core/WirelessBolt"));
                        method.instructions.insert(method.instructions.get(index), list);
                    });

            if(method.name.equals("jamTile"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        "br/com/gamemods/minecity/forge/base/protection/wrcbe/WRCBEHooks", "onJammerJamTile",
                        "(Lbr/com/gamemods/minecity/forge/base/protection/wrcbe/IJammerPart;Lbr/com/gamemods/minecity/forge/base/accessors/block/ITileEntity;)Z",
                        false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(RETURN));
                list.add(labelNode);
                method.instructions.insert(list);
            }
            else if(method.name.equals("jamEntity"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(INVOKESTATIC,
                        "br/com/gamemods/minecity/forge/base/protection/wrcbe/WRCBEHooks", "onJammerJamEntity",
                        "(Lbr/com/gamemods/minecity/forge/base/protection/wrcbe/IJammerPart;Lbr/com/gamemods/minecity/forge/base/accessors/entity/base/IEntity;)Z",
                        false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(RETURN));
                list.add(labelNode);
                method.instructions.insert(list);
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
