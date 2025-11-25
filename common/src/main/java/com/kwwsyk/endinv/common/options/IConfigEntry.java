package com.kwwsyk.endinv.common.options;

/**
 * Config Entry Interface suiting Forge's config api.
 * Which has key, comments, getter and setter
 * @param <T>
 */
public interface IConfigEntry<T> extends IConfigValue<T>, AutoSavable<T>
{

    String key();

    String[] comments();

    T defaultValue();

    boolean isInitialized();

    /**
     * Config Entry with fields.
     * Fields will automatically be computed.
     */
    interface Complex<C> extends IConfigEntry<C>{

        ConfigEntryImpl<?>[] fields();

        default boolean isInitialized(){
            for(IConfigEntry<?> field : fields()) if(!field.isInitialized()) return false;
            return true;
        }
    }
}
