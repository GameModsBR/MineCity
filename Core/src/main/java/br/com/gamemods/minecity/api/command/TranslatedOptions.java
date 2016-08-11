package br.com.gamemods.minecity.api.command;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

public class TranslatedOptions implements Arg
{
    @NotNull
    private final Arg arg;

    @NotNull
    private String[] translations;

    public TranslatedOptions(@NotNull Arg arg, @NotNull String[] translations)
    {
        this.arg = arg;
        this.translations = translations;
    }

    public String[] originalOptions()
    {
        return arg.options();
    }

    @Override
    public String name()
    {
        return arg.name();
    }

    @Override
    public Type type()
    {
        return arg.type();
    }

    @Override
    public boolean sticky()
    {
        return arg.sticky();
    }

    @Override
    public boolean optional()
    {
        return arg.optional();
    }

    @Override
    public String[] options()
    {
        return translations.clone();
    }

    @Override
    public String relative()
    {
        return arg.relative();
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
        return arg.annotationType();
    }
}
