package com.kwwsyk.endinv.common.options.config;

public abstract non-sealed class ComplexConfigEntryImpl<C> extends ConfigEntryImpl<C> implements IConfigValue<C>, AutoSavable<C> {

    public ComplexConfigEntryImpl(String key, String[] comments, C defaultValue) {
        super(key, comments, defaultValue);
    }

    /**
     * The getter of a complex config entry may be null.
     * Let it return {@link #fields()}'s returns.
     * <br>
     * If C is a record, let it return a new instance of C and invoke all fields' getters.
     *
     * @return the default value of the complex config entry.
     */
    @Override
    public abstract C get() ;

    /**
     * The setter of a complex config entry may be null.
     * Let it set every field of {@link #fields()}.
     * @param c to be used to set fields.
     */
    @Override
    public abstract void set(C c);

    public void setInitialized() {
        isInitialized = true;
    }

    public abstract ConfigEntryImpl<?>[] fields();
}
