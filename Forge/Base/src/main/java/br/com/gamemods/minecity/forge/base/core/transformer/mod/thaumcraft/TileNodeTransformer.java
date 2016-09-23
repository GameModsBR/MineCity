package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class TileNodeTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public TileNodeTransformer()
    {
        super("thaumcraft.common.tiles.TileNode");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("handleHungryNodeSecond"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(IIIZ)Z"))
                        .anyMatch(ins-> {
                            MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC,
                                    "mineCity$wrapper",
                                    "(Lnet/minecraft/world/World;IIIZL"+name.replace('.','/')+";)Z",
                                    null, null
                            );
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 5);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                                    "onNodeBreak", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;III)Z",
                                    false
                            );
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitInsn(ICONST_0);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitVarInsn(ILOAD, 4);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(IRETURN);
                            wrapper.visitEnd();
                            node.methods.add(wrapper);

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = name.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            return true;
                        });
                break;
            }
        }
    }
}
