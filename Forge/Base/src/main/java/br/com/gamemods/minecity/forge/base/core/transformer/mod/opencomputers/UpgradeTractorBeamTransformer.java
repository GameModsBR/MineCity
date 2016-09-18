package br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@MethodPatcher
public class UpgradeTractorBeamTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        switch(transformedName)
        {
            case "li.cil.oc.server.component.UpgradeTractorBeam$Common":
                return common(transformedName, basicClass);
            case "li.cil.oc.server.component.UpgradeTractorBeam$Drone":
                return drone(transformedName, basicClass);
            case "li.cil.oc.server.component.UpgradeTractorBeam$Player":
                return player(transformedName, basicClass);
            default:
                return basicClass;
        }
    }

    public byte[] drone(String transformedName, byte[] basicClass)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        MethodNode owner = new MethodNode(ACC_PUBLIC, "host", "()Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IEnvironmentHost;", null, null);
        owner.visitCode();
        owner.visitVarInsn(ALOAD, 0);
        owner.visitMethodInsn(INVOKEVIRTUAL, transformedName.replace('.','/'), "owner", "()Lli/cil/oc/api/internal/Agent;", false);
        owner.visitInsn(ARETURN);
        owner.visitEnd();
        node.methods.add(owner);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }

    public byte[] player(String transformedName, byte[] basicClass)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        MethodNode owner = new MethodNode(ACC_PUBLIC, "host", "()Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IEnvironmentHost;", null, null);
        owner.visitCode();
        owner.visitVarInsn(ALOAD, 0);
        owner.visitMethodInsn(INVOKEVIRTUAL, transformedName.replace('.','/'), "owner", "()Lli/cil/oc/api/network/EnvironmentHost;", false);
        owner.visitInsn(ARETURN);
        owner.visitEnd();
        node.methods.add(owner);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }

    public byte[] common(String transformedName, byte[] basicClass)
    {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);

        node.interfaces.add("br/com/gamemods/minecity/forge/base/protection/opencomputers/IUpgradeTractorBeam");

        for(MethodNode method : node.methods)
        {
            if(method.name.equals("suck"))
            {
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                        .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                        .filter(ins-> ins.desc.equals("(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;")
                                    ||ins.desc.equals("(Ljava/lang/Class;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"))
                        .anyMatch(ins-> {
                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    "br/com/gamemods/minecity/forge/base/protection/opencomputers/OCHooks",
                                    "onSuck",
                                    "(Ljava/util/List;Lbr/com/gamemods/minecity/forge/base/protection/opencomputers/IUpgradeTractorBeam;)Ljava/util/List;",
                                    false
                            ));
                            method.instructions.insert(ins, list);
                            return true;
                        });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        basicClass = ModEnv.saveClass(transformedName, writer.toByteArray());
        return basicClass;
    }
}
