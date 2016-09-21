package br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.util.Comparator;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class ExplosionIC2Transformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public ExplosionIC2Transformer()
    {
        super("ic2.core.ExplosionIC2");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        node.interfaces.add("br.com.gamemods.minecity.forge.base.protection.industrialcraft.IExplosionIC2".replace('.','/'));

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("doExplosion"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;")
                                    ||ins.desc.equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;")
                                    ||ins.desc.equals("(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;")
                                    ||ins.desc.equals("(Ljava/lang/Class;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"))
                        .map(ins-> method.instructions.indexOf(ins))
                        .sorted(Comparator.reverseOrder()).mapToInt(Integer::intValue)
                        .mapToObj(index-> method.instructions.get(index)).map(MethodInsnNode.class::cast)
                        .forEachOrdered(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.industrialcraft.ICHooks".replace('.','/'),
                                    "onExplosionDamage", "(Ljava/util/List;Lbr.com.gamemods.minecity.forge.base.protection.industrialcraft.IExplosionIC2;)Ljava/util/List;".replace('.','/'),
                                    false
                            ));
                            method.instructions.insert(ins, list);
                        });
                break;
            }
        }

        MethodNode method = new MethodNode(ACC_PUBLIC, "getWorld", "()Lbr.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;".replace('.','/'), null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name.replace('.','/'), "worldObj", "Lnet/minecraft/world/World;");
        method.visitTypeInsn(CHECKCAST, "br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer".replace('.','/'));
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        method = new MethodNode(ACC_PUBLIC, "getIgniter", "()Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;".replace('.','/'), null, null);
        method.visitCode();
        method.visitVarInsn(ALOAD, 0);
        method.visitFieldInsn(GETFIELD, name.replace('.','/'), "igniter", "Lnet/minecraft/entity/EntityLivingBase;");
        method.visitInsn(ARETURN);
        method.visitEnd();
        node.methods.add(method);

        if(!ModEnv.seven)
        {
            method = new MethodNode(ACC_PUBLIC, "getExplosionX", "()D", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, name.replace('.','/'), "explosionX", "D");
            method.visitInsn(DRETURN);
            method.visitEnd();
            node.methods.add(method);

            method = new MethodNode(ACC_PUBLIC, "getExplosionY", "()D", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, name.replace('.','/'), "explosionY", "D");
            method.visitInsn(DRETURN);
            method.visitEnd();
            node.methods.add(method);

            method = new MethodNode(ACC_PUBLIC, "getExplosionZ", "()D", null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, name.replace('.','/'), "explosionZ", "D");
            method.visitInsn(DRETURN);
            method.visitEnd();
            node.methods.add(method);

            method = new MethodNode(ACC_PUBLIC, "getExploder", "()Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;".replace('.','/'), null, null);
            method.visitCode();
            method.visitVarInsn(ALOAD, 0);
            method.visitFieldInsn(GETFIELD, name.replace('.','/'), "exploder", "Lnet/minecraft/entity/Entity;");
            method.visitInsn(ARETURN);
            method.visitEnd();
            node.methods.add(method);
        }
    }
}
