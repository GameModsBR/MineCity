package br.com.gamemods.minecity.reactive.script;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.net.URL;

public class ScriptEngine
{
    private final Binding binding = new Binding();
    private final GroovyScriptEngine engine;

    public ScriptEngine(URL... roots)
    {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(ReactiveScript.class.getName());

        engine = new GroovyScriptEngine(roots);
        engine.setConfig(config);
    }

    public Object load(String modId) throws ResourceException, ScriptException
    {
        modId = modId.toLowerCase();
        try
        {
            return engine.run(modId+"/"+modId+".groovy", binding);
        }
        catch(ResourceException e)
        {
            try
            {
                return engine.run(modId+".groovy", binding);
            }
            catch(Exception e2)
            {
                e.addSuppressed(e2);
                throw new ScriptException("Failed to load the protection script for "+modId);
            }
        }
    }
}
