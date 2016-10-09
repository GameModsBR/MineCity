package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.RETURN;

@Referenced
@MethodPatcher
public class BlockTaintFibresTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public BlockTaintFibresTransformer()
    {
        super("thaumcraft.common.blocks.BlockTaintFibres");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        String self = name.replace('.','/');

        MethodNode setBiomeAt = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$setBiomeAt", "(Lnet/minecraft/world/World;IILnet/minecraft/world/biome/BiomeGenBase;III)V", null, null);
        setBiomeAt.visitCode();
        setBiomeAt.visitVarInsn(ALOAD, 0);
        setBiomeAt.visitVarInsn(ILOAD, 1);
        setBiomeAt.visitVarInsn(ILOAD, 2);
        setBiomeAt.visitVarInsn(ILOAD, 4);
        setBiomeAt.visitVarInsn(ILOAD, 5);
        setBiomeAt.visitVarInsn(ILOAD, 6);
        setBiomeAt.visitMethodInsn(INVOKESTATIC,
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                "onBlockChangeBiome",
                "(Lnet/minecraft/world/World;IIIII)Z",
                false
        );
        Label elseLabel = new Label();
        setBiomeAt.visitJumpInsn(IFEQ, elseLabel);
        setBiomeAt.visitInsn(RETURN);
        setBiomeAt.visitLabel(elseLabel);
        setBiomeAt.visitVarInsn(ALOAD, 0);
        setBiomeAt.visitVarInsn(ILOAD, 1);
        setBiomeAt.visitVarInsn(ILOAD, 2);
        setBiomeAt.visitVarInsn(ALOAD, 3);
        setBiomeAt.visitMethodInsn(INVOKESTATIC,
                "thaumcraft.common.lib.utils.Utils".replace('.','/'),
                "setBiomeAt",
                "(Lnet/minecraft/world/World;IILnet/minecraft/world/biome/BiomeGenBase;)V",
                false
        );
        setBiomeAt.visitInsn(RETURN);
        setBiomeAt.visitEnd();

        for(MethodNode method : node.methods)
        {
            switch(method.name)
            {
                case "taintBiomeSpread":
                    CollectionUtil.stream(method.instructions.iterator())
                            .filter(ins -> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                            .filter(ins -> ins.owner.equals("thaumcraft/common/lib/utils/Utils"))
                            .filter(ins -> ins.desc.equals("(Lnet/minecraft/world/World;IILnet/minecraft/world/biome/BiomeGenBase;)V"))
                            .filter(ins -> ins.name.equals("setBiomeAt"))
                            .anyMatch(ins ->
                            {
                                InsnList list = new InsnList();
                                list.add(new VarInsnNode(ILOAD, 1));
                                list.add(new VarInsnNode(ILOAD, 2));
                                list.add(new VarInsnNode(ILOAD, 3));
                                method.instructions.insertBefore(ins, list);
                                ins.itf = false;
                                ins.owner = self;
                                ins.name = setBiomeAt.name;
                                ins.desc = setBiomeAt.desc;
                                return true;
                            });
                    break;
            }
        }

        node.methods.add(setBiomeAt);
    }
}
