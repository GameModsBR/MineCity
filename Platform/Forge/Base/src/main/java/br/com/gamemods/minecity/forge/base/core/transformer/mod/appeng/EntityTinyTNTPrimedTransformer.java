package br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class EntityTinyTNTPrimedTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public EntityTinyTNTPrimedTransformer()
    {
        super("appeng.entity.EntityTinyTNTPrimed");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("explode"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"))
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.appeng.AppengHooks".replace('.','/'),
                                    "onTinyTntDamage",
                                    "(Ljava/util/List;Lbr.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityTNTPrimed;)Ljava/util/List;"
                                        .replace('.','/'),
                                    false
                            ));
                            method.instructions.insert(ins, list);
                            return true;
                        });

                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(III)Lnet/minecraft/block/Block;"))
                        .anyMatch(ins-> {
                            method.instructions.insertBefore(ins, new VarInsnNode(ALOAD, 0));
                            ins.setOpcode(INVOKESTATIC);
                            ins.itf = false;
                            ins.owner = "br.com.gamemods.minecity.forge.base.protection.appeng.AppengHooks".replace('.','/');
                            ins.name = "onTinyTntBreak";
                            ins.desc = "(Lnet/minecraft/world/World;IIILbr.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityTNTPrimed;)Lnet/minecraft/block/Block;".replace('.','/');
                            return true;
                        });
                break;
            }
        }
    }
}
