package br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.forge.base.core.MethodPatcher;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.BasicTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static org.objectweb.asm.Opcodes.*;

@Referenced
@MethodPatcher
public class GolemHelperAndAITransformer extends BasicTransformer
{
    private Pattern pattern = Pattern.compile("^thaumcraft\\.common\\.entities\\.ai\\.[^.]+\\.AI.+$");

    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public GolemHelperAndAITransformer()
    {
        super("thaumcraft.common.entities.golems.GolemHelper");
    }

    @Override
    public boolean accept(String name)
    {
        return name.equals("thaumcraft.common.entities.golems.GolemHelper") || pattern.matcher(name).matches();
    }

    @Override
    protected void patch(String name, ClassNode node, ClassReader reader)
    {
        String thaum = "br.com.gamemods.minecity.forge.base.protection.thaumcraft.ThaumHooks".replace('.','/');
        String golem = "Lthaumcraft.common.entities.golems.EntityGolemBase;".replace('.','/');
        String golemItf = "Lbr.com.gamemods.minecity.forge.base.protection.thaumcraft.IEntityGolemBase;".replace('.','/');
        String golemAi = "br.com.gamemods.minecity.forge.base.protection.thaumcraft.GolemAI".replace('.','/');
        String ai = "Lbr.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityAIBase;".replace('.','/');
        String self = name.replace('.','/');

        boolean modified = false;
        final Supplier<AbstractInsnNode> loadAi;
        final Supplier<InsnList> loadGolem;
        if(!name.endsWith("GolemHelper"))
        {
            loadAi = () -> new VarInsnNode(ALOAD, 0);
            Optional<FieldNode> opt = node.fields.stream().filter(fd -> fd.desc.equals(golem)).findFirst();
            if(opt.isPresent())
            {
                FieldNode theGolem = opt.get();
                modified = true;
                node.interfaces.add(golemAi);

                MethodNode method = new MethodNode(ACC_PUBLIC, "getTheGolem", "()"+golemItf, null, null);
                method.visitCode();
                method.visitVarInsn(ALOAD, 0);
                method.visitFieldInsn(GETFIELD, self, theGolem.name, theGolem.desc);
                method.visitInsn(ARETURN);
                method.visitEnd();
                node.methods.add(method);

                loadGolem = ()-> {
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new FieldInsnNode(GETFIELD, self, theGolem.name, theGolem.desc));
                    return list;
                };
            }
            else
            {
                loadGolem = ()-> {
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new TypeInsnNode(CHECKCAST, golemAi));
                    LabelNode labelNode = new LabelNode();
                    list.add(new JumpInsnNode(IFEQ, labelNode));
                    list.add(new VarInsnNode(ALOAD, 0));
                    list.add(new MethodInsnNode(INVOKEINTERFACE, golemAi, "getTheGolem", "()"+golemItf, true));
                    list.add(new TypeInsnNode(CHECKCAST, golem.substring(1, golem.length()-1)));
                    return list;
                };
            }
        }
        else
        {
            loadAi = () -> new InsnNode(ACONST_NULL);
            loadGolem = null;
        }


        AtomicReference<MethodNode> getTileWrapper = new AtomicReference<>();
        for(MethodNode method : node.methods)
        {
            final Supplier<InsnList> golemLoader;
            if(loadGolem != null)
                golemLoader = loadGolem;
            else
            {
                int var = varIndex(is(method.access, ACC_STATIC), method.desc, golem);
                if(var == -1)
                    continue;

                golemLoader = () ->
                {
                    InsnList list = new InsnList();
                    list.add(new VarInsnNode(ALOAD, var));
                    return list;
                };
            }


            CollectionUtil.stream(method.instructions.iterator())
                    .filter(ins-> ins.getOpcode() == INVOKEVIRTUAL).map(MethodInsnNode.class::cast)
                    .filter(ins-> ins.owner.equals("net/minecraft/world/World"))
                    .filter(ins-> ins.desc.equals("(III)Lnet/minecraft/tileentity/TileEntity;"))
                    .map(ins-> method.instructions.indexOf(ins)).sorted(Comparator.reverseOrder())
                    .map(i-> (MethodInsnNode) method.instructions.get(i))
                    .forEachOrdered(ins-> {
                        MethodNode wrapper = getTileWrapper.get();
                        if(wrapper == null)
                        {
                            wrapper = new MethodNode(ACC_PUBLIC|ACC_STATIC,"mineCity$getTileEntity",
                                    "(Lnet/minecraft/world/World;III"+golem+ai+")Lnet/minecraft/tileentity/TileEntity;",
                                    null, null
                            );
                            getTileWrapper.set(wrapper);
                            wrapper.visitCode();
                            wrapper.visitVarInsn(ALOAD, 4);
                            wrapper.visitVarInsn(ALOAD, 5);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(INVOKESTATIC,
                                    thaum, "onGolemAiScanTile", "("+golemItf+ai+"Lnet/minecraft/world/World;III)Z", false
                            );
                            Label label = new Label();
                            wrapper.visitJumpInsn(IFEQ, label);
                            wrapper.visitInsn(ACONST_NULL);
                            wrapper.visitInsn(ARETURN);
                            wrapper.visitLabel(label);
                            wrapper.visitVarInsn(ALOAD, 0);
                            wrapper.visitVarInsn(ILOAD, 1);
                            wrapper.visitVarInsn(ILOAD, 2);
                            wrapper.visitVarInsn(ILOAD, 3);
                            wrapper.visitMethodInsn(ins.getOpcode(), ins.owner, ins.name, ins.desc, ins.itf);
                            wrapper.visitInsn(ARETURN);
                            wrapper.visitEnd();
                        }

                        InsnList list = new InsnList();
                        list.add(golemLoader.get());
                        list.add(loadAi.get());

                        method.instructions.insertBefore(ins, list);
                        ins.setOpcode(INVOKESTATIC);
                        ins.itf = false;
                        ins.owner = self;
                        ins.name = wrapper.name;
                        ins.desc = wrapper.desc;
                    });
        }

        MethodNode wrapper = getTileWrapper.get();
        if(wrapper == null)
            abort = !modified;
        else
            node.methods.add(wrapper);
    }
}
