package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class BlockUtilsTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public BlockUtilsTransformer()
    {
        super("thaumcraft.common.lib.utils.BlockUtils");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        FieldNode fd = new FieldNode(ACC_PUBLIC|ACC_STATIC|ACC_VOLATILE, "mineCity$findBlockFor", "Lnet/minecraft/entity/player/EntityPlayer;", null, null);
        node.fields.add(fd);

        List<MethodNode> wrappers = new ArrayList<>(2);
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("breakFurthestBlock") && method.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;Lnet/minecraft/entity/player/EntityPlayer;ZI)Z"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 5));
                list.add(new FieldInsnNode(PUTSTATIC, name.replace('.','/'), fd.name, fd.desc));
                method.instructions.insert(list);
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals(name.replace('.','/')))
                        .filter(ins-> name.equals("findBlocks"))
                        .anyMatch(ins-> {
                            InsnList post = new InsnList();
                            post.add(new InsnNode(ACONST_NULL));
                            post.add(new FieldInsnNode(PUTSTATIC, name.replace('.','/'), fd.name, fd.desc));
                            method.instructions.insert(ins, post);
                            return true;
                        });
            }
            else if(method.name.equals("findBlocks"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(III)Lnet/minecraft/block/Block;"))
                        .filter(ins-> ins.getNext().getNext().getOpcode() == IF_ACMPNE)
                        .anyMatch(ins-> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC,"mineCity$getBlock",
                                    "(Lnet/minecraft/world/World;III)Lnet/minecraft/block/Block;",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitFieldInsn(GETSTATIC, name.replace('.','/'), fd.name, fd.desc);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                                    "onPlayerBreak",
                                    "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;III)Z",
                                    false
                            );
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitInsn(ACONST_NULL);
                            wrapper.visitInsn(ARETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(ARETURN);
                            wrapper.visitEnd();
                            wrappers.add(wrapper);

                            ins.setOpcode(INVOKESTATIC);
                            ins.owner = name.replace('.','/');
                            ins.itf = false;
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            return true;
                        });
            }
        }

        node.methods.addAll(wrappers);
    }
}
