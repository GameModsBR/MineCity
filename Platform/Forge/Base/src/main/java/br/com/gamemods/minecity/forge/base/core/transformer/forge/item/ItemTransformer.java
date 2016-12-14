package br.com.gamemods.minecity.forge.base.core.transformer.forge.item;

import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
public class ItemTransformer extends BasicTransformer
{
    public ItemTransformer()
    {
        super("net.minecraft.item.Item");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Z)L"+ModEnv.rayTraceResultClass.replace('.','/')+";"))
            {
                MethodNode wrapper = new MethodNode(ACC_PUBLIC,
                        "rayTrace",
                        "(Lbr.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;Z)Lbr.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;"
                            .replace('.','/')
                        ,
                        null, null
                );
                wrapper.visitCode();
                wrapper.visitVarInsn(ALOAD, 0);
                wrapper.visitVarInsn(ALOAD, 1);
                wrapper.visitTypeInsn(CHECKCAST, "net/minecraft/world/World");
                wrapper.visitVarInsn(ALOAD, 2);
                wrapper.visitTypeInsn(CHECKCAST, "net/minecraft/entity/player/EntityPlayer");
                wrapper.visitVarInsn(ILOAD, 3);
                wrapper.visitMethodInsn(INVOKEVIRTUAL, name.replace('.','/'), method.name, method.desc, false);
                wrapper.visitInsn(ARETURN);
                wrapper.visitEnd();
                node.methods.add(wrapper);
                break;
            }
        }
    }
}
