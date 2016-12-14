package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class ServerTickEventsFMLTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public ServerTickEventsFMLTransformer()
    {
        super("thaumcraft.common.lib.events.ServerTickEventsFML");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("tickBlockSwap"))
            {
                String vs = "thaumcraft/common/lib/events/ServerTickEventsFML$VirtualSwapper";
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("java/util/concurrent/LinkedBlockingQueue"))
                        .filter(ins-> ins.name.equals("poll"))
                        .filter(ins-> ins.desc.equals("()Ljava/lang/Object;"))
                        .map(AbstractInsnNode::getNext)
                        .filter(ins-> ins.getOpcode() == CHECKCAST).map(TypeInsnNode.class::cast)
                        .filter(ins-> ins.desc.equals("thaumcraft/common/lib/events/ServerTickEventsFML$VirtualSwapper"))
                        .map(AbstractInsnNode::getNext)
                        .filter(ins-> ins.getOpcode() == ASTORE).map(VarInsnNode.class::cast)
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, ins.var));
                            LabelNode labelNode = new LabelNode();
                            list.add(new JumpInsnNode(IFNULL, labelNode));
                            list.add(new VarInsnNode(ALOAD, ins.var));
                            list.add(new FieldInsnNode(GETFIELD, vs, "player", "Lnet/minecraft/entity/player/EntityPlayer;"));
                            list.add(new VarInsnNode(ALOAD, ins.var));
                            list.add(new FieldInsnNode(GETFIELD, vs, "target", "Lnet/minecraft/item/ItemStack;"));
                            list.add(new VarInsnNode(ALOAD, 1));
                            list.add(new VarInsnNode(ALOAD, ins.var));
                            list.add(new FieldInsnNode(GETFIELD, vs, "x", "I"));
                            list.add(new VarInsnNode(ALOAD, ins.var));
                            list.add(new FieldInsnNode(GETFIELD, vs, "y", "I"));
                            list.add(new VarInsnNode(ALOAD, ins.var));
                            list.add(new FieldInsnNode(GETFIELD, vs, "z", "I"));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                                    "onSwapperSwap", "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;III)Z",
                                    false
                            ));
                            list.add(new JumpInsnNode(IFEQ, labelNode));
                            list.add(new InsnNode(ACONST_NULL));
                            list.add(new VarInsnNode(ASTORE, ins.var));
                            list.add(labelNode);
                            method.instructions.insert(ins, list);
                            return true;
                        });

                break;
            }
        }
    }
}
