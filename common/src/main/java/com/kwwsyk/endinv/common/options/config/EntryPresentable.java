package com.kwwsyk.endinv.common.options.config;

/**
 * For enum class
 */
public interface EntryPresentable {

    default String key(){
        if(this instanceof Enum<?> e) return e.name();
        throw new UnsupportedOperationException("Interface EntryPresentable only for enum class default");
    }

    /**
     * Description of this enum entry
     */
    String description();

    static <E extends Enum<E> & EntryPresentable> String[] generateComments(Class<E> clazz, String basicComment){
        E[] values = clazz.getEnumConstants();
        String[] out = new String[values.length + 1];
        out[0] = basicComment + "Enum values:";
        for(int i = 1; i < values.length + 1; i++){
            out[i] = values[i].name() + ": " + values[i].description();
        }
        return out;
    }
}
