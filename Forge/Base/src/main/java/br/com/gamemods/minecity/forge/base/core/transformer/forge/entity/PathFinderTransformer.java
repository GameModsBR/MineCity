package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.objectweb.asm.Opcodes.*;

@Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
@Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
@MethodPatcher
public class PathFinderTransformer implements IClassTransformer
{
    @Override
    public byte[] transform(String s, String srg, byte[] bytes)
    {
        if(!srg.equals("net.minecraft.pathfinding.PathFinder"))
            return bytes;

        String hookClass = ModEnv.hookClass.replace('.', '/');

        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);

        Optional<FieldNode> processorField = node.fields.stream()
                .filter(field -> field.desc.equals("Lnet/minecraft/pathfinding/NodeProcessor;"))
                .findFirst();

        Optional<FieldNode> accessField = node.fields.stream()
                .filter(field -> field.desc.equals("Lnet/minecraft/world/IBlockAccess;"))
                .findFirst();

        boolean seven = accessField.isPresent();

        for(MethodNode method : node.methods)
        {
            if(method.desc.equals("(Lnet/minecraft/pathfinding/PathPoint;Lnet/minecraft/pathfinding/PathPoint;F)Lnet/minecraft/pathfinding/Path;")
            || method.desc.equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/pathfinding/PathPoint;Lnet/minecraft/pathfinding/PathPoint;Lnet/minecraft/pathfinding/PathPoint;F)Lnet/minecraft/pathfinding/PathEntity;"))
            {
                AtomicBoolean noSkip = new AtomicBoolean(seven);
                CollectionUtil.stream(method.instructions.iterator())
                        .filter(ins -> ins.getOpcode() == GETFIELD).map(FieldInsnNode.class::cast)
                        .filter(ins -> ins.desc.equals("[Lnet/minecraft/pathfinding/PathPoint;"))
                        .filter(ins -> ins.owner.equals("net/minecraft/pathfinding/PathFinder"))
                        .filter(ins -> noSkip.getAndSet(true))
                        .anyMatch(ins -> {
                            int index = method.instructions.indexOf(ins);
                            ListIterator<AbstractInsnNode> iter = method.instructions.iterator(index);
                            LabelNode breakLabel = null;
                            LabelNode compareLabel = null;
                            VarInsnNode k = null;
                            while(iter.hasPrevious())
                            {
                                AbstractInsnNode previous = iter.previous();
                                if(breakLabel == null)
                                {
                                    if(previous instanceof JumpInsnNode)
                                        breakLabel = ((JumpInsnNode) previous).label;
                                }
                                else
                                {
                                    if(previous instanceof LabelNode)
                                    {
                                        compareLabel = (LabelNode) previous;
                                        break;
                                    }
                                    else if(previous instanceof VarInsnNode)
                                        k = (VarInsnNode) previous;
                                }
                            }

                            Objects.requireNonNull(compareLabel, "Failed to find the jump point to continue the for loop");
                            Objects.requireNonNull(k, "Failed to find the k variable");

                            VarInsnNode var = (VarInsnNode) method.instructions.get(index + 3);

                            InsnList list = new InsnList();
                            list.add(new VarInsnNode(ALOAD, 0));
                            list.add(new VarInsnNode(ALOAD, var.var));
                            if(seven)
                            {
                                FieldNode access = accessField.orElseThrow(() -> new NoSuchElementException("Failed to find the worldAccess field"));
                                list.add(new VarInsnNode(ALOAD, 0));
                                list.add(new FieldInsnNode(GETFIELD, "net/minecraft/pathfinding/PathFinder", access.name, access.desc));
                                list.add(new VarInsnNode(ALOAD, 1));
                                list.add(new TypeInsnNode(CHECKCAST, "net/minecraft/entity/EntityLiving"));
                            }
                            else
                            {
                                FieldNode processor = processorField.orElseThrow(() -> new NoSuchElementException("Failed to find the nodeProcessor field"));
                                list.add(new VarInsnNode(ALOAD, 0));
                                list.add(new FieldInsnNode(GETFIELD, "net/minecraft/pathfinding/PathFinder", processor.name, processor.desc));
                                list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/pathfinding/NodeProcessor", "getBlockAccess$MC", "()Lnet/minecraft/world/IBlockAccess;", false));
                                list.add(new VarInsnNode(ALOAD, 0));
                                list.add(new FieldInsnNode(GETFIELD, "net/minecraft/pathfinding/PathFinder", processor.name, processor.desc));
                                list.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/pathfinding/NodeProcessor", "getEntity$MC", "()Lnet/minecraft/entity/EntityLiving;", false));
                            }
                            list.add(new MethodInsnNode(INVOKESTATIC,
                                    hookClass, "onPathFind",
                                    "(Lnet/minecraft/pathfinding/PathFinder;Lnet/minecraft/pathfinding/PathPoint;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/entity/EntityLiving;)Z",
                                    false
                            ));
                            LabelNode elseLabel = new LabelNode();
                            list.add(new JumpInsnNode(IFEQ, elseLabel));
                            list.add(new IincInsnNode(k.var, 1));
                            list.add(new JumpInsnNode(GOTO, compareLabel));
                            list.add(elseLabel);
                            method.instructions.insert(var, list);
                            return true;
                        });
                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        bytes = writer.toByteArray();
        ModEnv.saveClass(srg, bytes);
        return bytes;
    }
}
