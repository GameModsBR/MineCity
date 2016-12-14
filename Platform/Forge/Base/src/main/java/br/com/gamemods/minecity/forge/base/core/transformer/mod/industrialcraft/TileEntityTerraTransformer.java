package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.objectweb.asm.Opcodes.*;

@MethodPatcher
@Referenced
public class TileEntityTerraTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public TileEntityTerraTransformer()
    {
        super("ic2.core.block.machine.tileentity.TileEntityTerra");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        AtomicReference<MethodNode> terraform = new AtomicReference<>();
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("updateEntityServer"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEINTERFACE).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.name.equals("terraform"))
                        .filter(ins-> ins.owner.equals("ic2.api.item.ITerraformingBP".replace('.','/')))
                        .anyMatch(ins->{
                            // 1.7.10
                            if(ins.desc.equals("(Lnet/minecraft/world/World;III)Z"))
                            {
                                MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$terraform",
                                        "(Lic2/api/item/ITerraformingBP;Lnet/minecraft/world/World;IIILnet/minecraft/tileentity/TileEntity;)Z",
                                        null, null
                                );
                                wrapper.visitCode();
                                wrapper.visitVarInsn(ALOAD, 5);
                                wrapper.visitVarInsn(ALOAD, 1);
                                wrapper.visitVarInsn(ILOAD, 2);
                                wrapper.visitVarInsn(ILOAD, 3);
                                wrapper.visitVarInsn(ILOAD, 4);
                                wrapper.visitMethodInsn(INVOKESTATIC, ModEnv.hookClass.replace('.','/'), "toPoint", "(III)Lbr/com/gamemods/minecity/api/shape/Point;", false);
                                wrapper.visitMethodInsn(INVOKESTATIC,
                                        "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                                        "onTerraformStart", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                        false
                                );
                                Label label = new Label();
                                wrapper.visitJumpInsn(IFEQ, label);
                                wrapper.visitInsn(ICONST_0);
                                wrapper.visitInsn(IRETURN);
                                wrapper.visitLabel(label);
                                wrapper.visitVarInsn(ALOAD, 0);
                                wrapper.visitVarInsn(ALOAD, 1);
                                wrapper.visitVarInsn(ILOAD, 2);
                                wrapper.visitVarInsn(ILOAD, 3);
                                wrapper.visitVarInsn(ILOAD, 4);
                                wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                                wrapper.visitVarInsn(ALOAD, 5);
                                wrapper.visitVarInsn(ALOAD, 1);
                                wrapper.visitMethodInsn(INVOKESTATIC,
                                        "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                                        "onTerraformEnds", "(ZLnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;)Z",
                                        false
                                );
                                wrapper.visitInsn(IRETURN);
                                wrapper.visitEnd();
                                terraform.set(wrapper);

                                method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = name.replace('.','/');
                                ins.name = wrapper.name;
                                ins.desc = wrapper.desc;
                                return true;
                            }
                            // 1.10.2
                            else
                            {
                                MethodNode wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC, "mineCity$terraform",
                                        "(Lic2/api/item/ITerraformingBP;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)Z",
                                        null, null
                                );
                                wrapper.visitCode();
                                wrapper.visitVarInsn(ALOAD, 4);
                                wrapper.visitVarInsn(ALOAD, 2);
                                wrapper.visitVarInsn(ALOAD, 3);
                                wrapper.visitMethodInsn(INVOKESTATIC, ModEnv.hookClass.replace('.','/'), "toPoint", "(Ljava/lang/Object;)Lbr/com/gamemods/minecity/api/shape/Point;", false);
                                wrapper.visitMethodInsn(INVOKESTATIC,
                                        "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                                        "onTerraformStart", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;Lbr/com/gamemods/minecity/api/shape/Point;)Z",
                                        false
                                );
                                Label label = new Label();
                                wrapper.visitJumpInsn(IFEQ, label);
                                wrapper.visitInsn(ICONST_0);
                                wrapper.visitInsn(IRETURN);
                                wrapper.visitLabel(label);
                                wrapper.visitVarInsn(ALOAD, 0);
                                wrapper.visitVarInsn(ALOAD, 1);
                                wrapper.visitVarInsn(ALOAD, 2);
                                wrapper.visitVarInsn(ALOAD, 3);
                                wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                                wrapper.visitVarInsn(ALOAD, 4);
                                wrapper.visitVarInsn(ALOAD, 2);
                                wrapper.visitMethodInsn(INVOKESTATIC,
                                        "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                                        "onTerraformEnds", "(ZLnet/minecraft/tileentity/TileEntity;Lnet/minecraft/world/World;)Z",
                                        false
                                );
                                wrapper.visitInsn(IRETURN);
                                wrapper.visitEnd();
                                terraform.set(wrapper);

                                method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                                ins.setOpcode(INVOKESTATIC);
                                ins.itf = false;
                                ins.owner = name.replace('.','/');
                                ins.name = wrapper.name;
                                ins.desc = wrapper.desc;
                                return true;
                            }
                        });
            }
            else if(method.name.equals("setBiomeAt"))
            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 0));
                list.add(new VarInsnNode(ILOAD, 1));
                list.add(new VarInsnNode(ILOAD, 2));
                list.add(new MethodInsnNode(INVOKESTATIC, "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                        "onChangeBiome", "(Lnet/minecraft/world/World;II)Z", false
                ));
                LabelNode labelNode = new LabelNode();
                list.add(new JumpInsnNode(IFEQ, labelNode));
                list.add(new InsnNode(RETURN));
                list.add(labelNode);
                method.instructions.insert(list);
            }
        }

        node.methods.add(Objects.requireNonNull(terraform.get(), "The terraform wrapper was not generated!"));
    }
}
