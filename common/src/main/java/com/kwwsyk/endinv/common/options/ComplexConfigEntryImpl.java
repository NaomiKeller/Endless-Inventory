package com.kwwsyk.endinv.common.options;

public abstract non-sealed class ComplexConfigEntryImpl<C> extends ConfigEntryImpl<C> implements IConfigEntry.Complex<C>{

    public ComplexConfigEntryImpl(String key, String[] comments, C defaultValue) {
        super(key, comments, defaultValue);
    }

    /**
     * The getter of a complex config entry may be null.
     * Let it return {@link #fields()}'s returns.
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
}
