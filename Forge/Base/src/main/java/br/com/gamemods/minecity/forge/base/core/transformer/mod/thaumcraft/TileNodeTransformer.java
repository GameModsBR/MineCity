package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
        AtomicReference<MethodNode> wrapperNode = new AtomicReference<>();
        MethodNode setBiomeAt = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$setBiomeAt", "(Lnet/minecraft/world/World;IILnet/minecraft/world/biome/BiomeGenBase;L"+name.replace('.','/')+";)V", null, null);
        setBiomeAt.visitCode();
        setBiomeAt.visitVarInsn(ALOAD, 4);
        setBiomeAt.visitVarInsn(ALOAD, 0);
        setBiomeAt.visitVarInsn(ILOAD, 1);
        setBiomeAt.visitVarInsn(ILOAD, 2);
        setBiomeAt.visitMethodInsn(INVOKESTATIC,
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                "onTileChangeBiome",
                "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;II)Z",
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

        MethodNode spreadFibres = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$spreadFibres", "(Lnet/minecraft/world/World;IIIL"+name.replace('.','/')+";)Z", null, null);
        spreadFibres.visitCode();
        spreadFibres.visitVarInsn(ALOAD, 4);
        spreadFibres.visitVarInsn(ALOAD, 0);
        spreadFibres.visitVarInsn(ILOAD, 1);
        spreadFibres.visitVarInsn(ILOAD, 2);
        spreadFibres.visitVarInsn(ILOAD, 3);
        spreadFibres.visitMethodInsn(INVOKESTATIC,
                "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                "onTileSpreadFibres",
                "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;III)Z",
                false
        );
        elseLabel = new Label();
        spreadFibres.visitJumpInsn(IFEQ, elseLabel);
        spreadFibres.visitInsn(ICONST_0);
        spreadFibres.visitInsn(IRETURN);
        spreadFibres.visitLabel(elseLabel);
        spreadFibres.visitVarInsn(ALOAD, 0);
        spreadFibres.visitVarInsn(ILOAD, 1);
        spreadFibres.visitVarInsn(ILOAD, 2);
        spreadFibres.visitVarInsn(ILOAD, 3);
        spreadFibres.visitMethodInsn(INVOKESTATIC,
                "thaumcraft.common.blocks.BlockTaintFibres".replace('.','/'),
                "spreadFibres",
                "(Lnet/minecraft/world/World;III)Z",
                false
        );
        spreadFibres.visitInsn(IRETURN);
        spreadFibres.visitEnd();

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
                                    "mineCity$onNodeBreak",
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
                            wrapperNode.set(wrapper);

                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = name.replace('.','/');
                            ins.name = wrapper.name;
                            ins.desc = wrapper.desc;
                            return true;
                        });
            }
            else if(method.name.equals("handleHungryNodeFirst"))
            {
                String aabb = ModEnv.aabbClass.replace('.','/');
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Ljava/lang/Class;L"+aabb+";)Ljava/util/List;"))
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                                    "onTileDamageEntities",
                                    "(Ljava/util/List;Lbr.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;)Ljava/util/List;"
                                        .replace('.','/'),
                                    false
                            ));
                            method.instructions.insert(ins, list);
                            return true;
                        });
            }
            else if(method.name.equals("handleTaintNode"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("thaumcraft/common/blocks/BlockTaintFibres"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/world/World;III)Z"))
                        .filter(ins-> ins.name.equals("spreadFibres"))
                        .anyMatch(ins-> {
                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.itf = false;
                            ins.owner = name.replace('.','/');
                            ins.name = spreadFibres.name;
                            ins.desc = spreadFibres.desc;
                            return true;
                        });
            }

            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKESTATIC).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("thaumcraft/common/lib/utils/Utils"))
                    .filter(ins-> ins.name.equals("setBiomeAt"))
                    .filter(ins-> ins.desc.equals("(Lnet/minecraft/world/World;IILnet/minecraft/world/biome/BiomeGenBase;)V"))
                    .map(ins-> method.instructions.indexOf(ins)).sorted(Comparator.reverseOrder())
                    .map(i-> (MethodInsnNode) method.instructions.get(i))
                    .forEachOrdered(ins-> {
                        method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                        ins.itf = false;
                        ins.owner = name.replace('.','/');
                        ins.name = setBiomeAt.name;
                        ins.desc = setBiomeAt.desc;
                    });
        }

        node.methods.add(Objects.requireNonNull(wrapperNode.get(), "mineCity$onNodeBreak was not generated"));
        node.methods.add(setBiomeAt);
        node.methods.add(spreadFibres);
    }
}
