package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Comparator;

import static org.objectweb.asm.Opcodes.*;

@MethodPatcher
public class ToolMassCannonTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ToolMassCannonTransformer()
    {
        super("appeng.items.tools.powered.ToolMassCannon");
        writerFlags = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        String rayTrace = ModEnv.rayTraceResultClass.replace('.', '/');
        String type = rayTrace + (rayTrace.contains("Moving")? "$MovingObjectType" :  "$Type");
        String appengHook = "br.com.gamemods.minecity.forge.base.protection.appeng.AppengHooks".replace('.','/');
        String iTrace = "br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult".replace('.','/');
        if(rayTrace.contains("Moving"))

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("standardAmmo") || method.name.equals("shootPaintBalls"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == GETSTATIC).map(FieldInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals(type))
                        .filter(ins-> ins.desc.equals("L"+type+";"))
                        .mapToInt(ins-> method.instructions.indexOf(ins))
                        .filter(i-> method.instructions.get(i+1).getOpcode() == IF_ACMPNE)
                        .filter(i-> method.instructions.get(i-1).getOpcode() == GETFIELD)
                        .filter(i-> method.instructions.get(i-2).getOpcode() == ALOAD)
                        .boxed().sorted(Comparator.reverseOrder()).mapToInt(Integer::valueOf)
                        .forEachOrdered(i->{
                            JumpInsnNode jump = (JumpInsnNode) method.instructions.get(i+1);
                            VarInsnNode var = (VarInsnNode) method.instructions.get(i-2);
                            FieldInsnNode fd = (FieldInsnNode) method.instructions.get(i);

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 2));
                            list.add(new VarInsnNode(ALOAD, 3));
                            list.add(new VarInsnNode(ALOAD, var.var));
                            list.add(new FieldInsnNode(fd.getOpcode(), fd.owner, fd.name, fd.desc));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    appengHook,
                                    "onMassCannonHit",
                                    "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;L"+iTrace+";Ljava/lang/Enum;)Z",
                                    false
                            ));
                            LabelNode labelNode = new LabelNode();
                            list.add(new JumpInsnNode(IFEQ, labelNode));
                            list.add(new InsnNode(RETURN));
                            list.add(labelNode);
                            method.instructions.insert(jump, list);
                        });
            }
        }
    }
}
