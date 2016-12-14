package br.com.gamemods.minecity.forge.base.core.transformer.mod.wrcbe;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertSetterGetterTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.util.Comparator;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class EntityREPTransformer extends InsertSetterGetterTransformer
{
    public EntityREPTransformer()
    {
        super("codechicken.wirelessredstone.addons.EntityREP",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter",
                "mineCityShooter",
                "br/com/gamemods/minecity/forge/base/protection/wrcbe/IEntityREP",
                "setMineCityShooter", "getMineCityShooter"
        );
    }

    @Override
    protected void patch(String transformedName, ClassNode node, ClassReader reader)
    {
        super.patch(transformedName, node, reader);

        String bolt = "br/com/gamemods/minecity/forge/base/protection/wrcbe/IWirelessBolt";

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("detonate"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKESPECIAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.name.equals("<init>"))
                        .filter(ins-> ins.owner.equals("codechicken/wirelessredstone/core/WirelessBolt"))
                        .map(ins-> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).mapToInt(Integer::intValue)
                        .forEachOrdered(index -> {
                            InsnList list = new InsnList();
                            list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKEINTERFACE,
                                    bolt, "createdFromProjectile", "(Lbr/com/gamemods/minecity/forge/base/accessors/entity/projectile/EntityProjectile;)L"+bolt+";",
                                    true
                            ));
                            list.add(new TypeInsnNode(CHECKCAST, "codechicken/wirelessredstone/core/WirelessBolt"));
                            method.instructions.insert(method.instructions.get(index), list);
                        });
                break;
            }
        }

        MethodNode method = new MethodNode(ACC_PUBLIC, "getShooterEntity", "()Lbr/com/gamemods/minecity/forge/base/accessors/entity/base/IEntityLivingBase;", null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, transformedName.replace('.','/'), "shootingEntity", "Lnet/minecraft/entity/EntityLivingBase;");
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);
    }
}
