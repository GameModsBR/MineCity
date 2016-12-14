package br.com.gamemods.minecity.reactive;

import br.com.gamemods.minecity.reactive.game.block.data.*;
import br.com.gamemods.minecity.reactive.reactor.Manipulator;
import br.com.gamemods.minecity.reactive.script.ReactiveScript;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroovyTest
{
    public static void main(String[] args) throws ResourceException, ScriptException
    {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(ReactiveScript.class.getName());

        Manipulator manipulator = mock(Manipulator.class);
        when(manipulator.getBlockManipulator()).thenReturn(new BlockManipulator()
        {
            @NotNull
            @Override
            public Optional<BlockTypeData> getBlockType(@NotNull Object block)
            {
                System.out.println("Getting: "+block);
                if(block instanceof String)
                    return Optional.of(new TestBlockTypeData(block));

                return Optional.empty();
            }

            @NotNull
            @Override
            public Optional<BlockStateData> getBlockState(@NotNull Object blockState)
            {
                return Optional.empty();
            }

            @NotNull
            @Override
            public Optional<TileEntityData> getTileEntity(@NotNull Object tileEntity)
            {
                return Optional.empty();
            }

            @NotNull
            @Override
            public Optional<BlockTraitData<?>> getBlockTrait(@NotNull Object blockTrait)
            {
                return Optional.empty();
            }
        });

        ReactiveLayer.setManipulator(manipulator);

        Binding binding = new Binding();
        GroovyScriptEngine engine = new GroovyScriptEngine(new URL[]{GroovyTest.class.getResource("/")});
        engine.setConfig(config);
        System.out.println(engine.run("scripts/minecraft/minecraft.groovy", binding));
    }
}
