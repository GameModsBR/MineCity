package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

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
public class EntityTravelingTrunkTransformer extends BasicTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public EntityTravelingTrunkTransformer()
    {
        super("thaumcraft.common.entities.golems.EntityTravelingTrunk");
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        for(MethodNode method : node.methods)
        {
            if(method.name.equals("pullItems"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Ljava/lang/Class;L"+ ModEnv.aabbClass.replace('.','/')+";)Ljava/util/List;"))
                        .map(ins-> method.instructions.indexOf(ins)).sorted(Comparator.reverseOrder())
                        .map(i-> (MethodInsnNode) method.instructions.get(i))
                        .forEachOrdered(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/'),
                                    "onEntityPull","(Ljava/util/List;Lbr/com/gamemods/minecity/forge/base/accessors/entity/base/IEntity;)Ljava/util/List;",
                                    false
                            ));
                            method.instructions.insert(ins, list);
                        });
                break;
            }
        }
    }
}
