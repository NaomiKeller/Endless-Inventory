package com.kwwsyk.endinv.common.options.config.command;

import com.kwwsyk.endinv.common.options.config.ComplexConfigEntryImpl;
import com.kwwsyk.endinv.common.options.config.ConfigEntryImpl;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public final class CommandBuilder {

    public static void buildCommandTree(LiteralArgumentBuilder<CommandSourceStack> configCommandRoot, List<ConfigEntryImpl<?>> configs){
        for(var entry : configs){
            buildCommandTree(configCommandRoot, entry);
        }
    }

    public static void buildCommandTree(LiteralArgumentBuilder<CommandSourceStack> configCommandRoot, ConfigEntryImpl<?> configEntry){
        configCommandRoot.then(buildNode(configEntry));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> buildNode(ConfigEntryImpl<?> configEntry){
        return switch (configEntry) {
            case ConfigEntryImpl.BooleanEntry booleanEntry ->
                    leafBoolean(booleanEntry);
            case ConfigEntryImpl.IntEntry intEntry ->
                    leafInt(intEntry);
            case ConfigEntryImpl.LongEntry longEntry ->
                    leafLong(longEntry);
            case ConfigEntryImpl.FloatEntry floatEntry ->
                    leafFloat(floatEntry);
            case ConfigEntryImpl.DoubleEntry doubleEntry ->
                    leafDouble(doubleEntry);
            case ConfigEntryImpl.StringEntry stringEntry ->
                    leafString(stringEntry);
            case ConfigEntryImpl.EnumEntry<?> enumEntry ->
                    leafEnum(enumEntry);
            case ConfigEntryImpl.ListEntry<?> listEntry -> listNode(listEntry);
            case ComplexConfigEntryImpl<?> complex -> {
                LiteralArgumentBuilder<CommandSourceStack> parent = Commands.literal(complex.key())
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(String.join("\n", complex.print())), false);
                            return 1;
                        });
                for (ConfigEntryImpl<?> field : complex.fields()) {
                    parent.then(buildNode(field));
                }
                //complex.setInitialized();
                yield parent;
            }
        };
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leafBoolean(ConfigEntryImpl.BooleanEntry entry){
        return Commands.literal(entry.key())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(entry.key() + " = " + entry.get()), false);
                    return 1;
                })
                .then(Commands.argument(entry.key(), BoolArgumentType.bool())
                        .executes(ctx -> {
                            boolean v = BoolArgumentType.getBool(ctx, entry.key());
                            entry.set(v);
                            ctx.getSource().sendSuccess(() -> Component.literal("Set " + entry.key() + " = " + v), true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leafInt(ConfigEntryImpl.IntEntry entry){
        return Commands.literal(entry.key())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(entry.key() + " = " + entry.get()), false);
                    return 1;
                })
                .then(Commands.argument(entry.key(), IntegerArgumentType.integer(entry.getMin(), entry.getMax()))
                        .executes(ctx -> {
                            int v = IntegerArgumentType.getInteger(ctx, entry.key());
                            entry.set(v);
                            ctx.getSource().sendSuccess(() -> Component.literal("Set " + entry.key() + " = " + v), true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leafLong(ConfigEntryImpl.LongEntry entry){
        return Commands.literal(entry.key())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(entry.key() + " = " + entry.get()), false);
                    return 1;
                })
                .then(Commands.argument(entry.key(), LongArgumentType.longArg(entry.getMin(), entry.getMax()))
                        .executes(ctx -> {
                            long v = LongArgumentType.getLong(ctx, entry.key());
                            entry.set(v);
                            ctx.getSource().sendSuccess(() -> Component.literal("Set " + entry.key() + " = " + v), true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leafFloat(ConfigEntryImpl.FloatEntry entry){
        return Commands.literal(entry.key())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(entry.key() + " = " + entry.get()), false);
                    return 1;
                })
                .then(Commands.argument(entry.key(), FloatArgumentType.floatArg(entry.getMin(), entry.getMax()))
                        .executes(ctx -> {
                            float v = FloatArgumentType.getFloat(ctx, entry.key());
                            entry.set(v);
                            ctx.getSource().sendSuccess(() -> Component.literal("Set " + entry.key() + " = " + v), true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leafDouble(ConfigEntryImpl.DoubleEntry entry){
        return Commands.literal(entry.key())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(entry.key() + " = " + entry.get()), false);
                    return 1;
                })
                .then(Commands.argument(entry.key(), DoubleArgumentType.doubleArg(entry.getMin(), entry.getMax()))
                        .executes(ctx -> {
                            double v = DoubleArgumentType.getDouble(ctx, entry.key());
                            entry.set(v);
                            ctx.getSource().sendSuccess(() -> Component.literal("Set " + entry.key() + " = " + v), true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leafString(ConfigEntryImpl.StringEntry entry){
        return Commands.literal(entry.key())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(entry.key() + " = " + entry.get()), false);
                    return 1;
                })
                .then(Commands.argument(entry.key(), StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String v = StringArgumentType.getString(ctx, entry.key());
                            entry.set(v);
                            ctx.getSource().sendSuccess(() -> Component.literal("Set " + entry.key() + " = " + v), true);
                            return 1;
                        }));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static LiteralArgumentBuilder<CommandSourceStack> leafEnum(ConfigEntryImpl.EnumEntry<?> entry){
        Class enumClass = entry.defaultValue().getClass();
        return Commands.literal(entry.key())
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(entry.key() + " = " + entry.get()), false);
                    return 1;
                })
                .then(Commands.argument(entry.key(), StringArgumentType.word())
                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(
                                Arrays.stream(((Enum[]) enumClass.getEnumConstants())).map(Enum::name), b))
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, entry.key());
                            Enum<?> value;
                            try {
                                value = Enum.valueOf(enumClass, name);
                            } catch (IllegalArgumentException ex) {
                                // try uppercase fallback
                                try {
                                    value = Enum.valueOf(enumClass, name.toUpperCase());
                                } catch (Exception ignored) {
                                    ctx.getSource().sendFailure(Component.literal("Invalid enum value: " + name));
                                    return 0;
                                }
                            }
                            ((ConfigEntryImpl.EnumEntry) entry).set(value);
                            Enum<?> finalValue = value;
                            ctx.getSource().sendSuccess(() -> Component.literal("Set " + entry.key() + " = " + finalValue.name()), true);
                            return 1;
                        }));
    }

    // List entry support: show/add/set/remove
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static LiteralArgumentBuilder<CommandSourceStack> listNode(ConfigEntryImpl.ListEntry<?> entry){
        String key = entry.key();
        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(key)
                .executes(ctx -> {
                    List<?> list = entry.get();
                    StringBuilder sb = new StringBuilder();
                    sb.append(key).append(" (size ").append(list.size()).append("):\n");
                    for (int i = 0; i < list.size(); i++) {
                        sb.append("[").append(i).append("] ").append(list.get(i)).append("\n");
                    }
                    ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                    return 1;
                });

        // add <value>
        node.then(Commands.literal("add")
                .then(Commands.argument("value", StringArgumentType.greedyString())
                        .suggests(listValueSuggestions(entry))
                        .executes(ctx -> {
                            String raw = StringArgumentType.getString(ctx, "value");
                            Class<?> elemClass = inferElemClass(entry);
                            Object parsed = parseValue(raw, elemClass);
                            if (!entry.getNewValPredicate().test(parsed)) {
                                ctx.getSource().sendFailure(Component.literal("Invalid value for list: " + raw));
                                return 0;
                            }
                            List cur = entry.get();
                            if (cur.size() + 1 > entry.getMaxLen()) {
                                ctx.getSource().sendFailure(Component.literal("List size exceeds maxLen " + entry.getMaxLen()));
                                return 0;
                            }
                            var copy = new java.util.ArrayList(cur);
                            copy.add(parsed);
                            entry.set(copy);
                            ctx.getSource().sendSuccess(() -> Component.literal("Added to " + key + ": " + raw), true);
                            return 1;
                        })));

        // set <index> <value>
        node.then(Commands.literal("set")
                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                .suggests(listValueSuggestions(entry))
                                .executes(ctx -> {
                                    int idx = IntegerArgumentType.getInteger(ctx, "index");
                                    var src = ctx.getSource();
                                    List cur = entry.get();
                                    if (idx < 0 || idx >= cur.size()) {
                                        src.sendFailure(Component.literal("Index out of bounds: " + idx));
                                        return 0;
                                    }
                                    String raw = StringArgumentType.getString(ctx, "value");
                                    Class<?> elemClass = inferElemClass(entry);
                                    Object parsed = parseValue(raw, elemClass);
                                    if (!entry.getNewValPredicate().test(parsed)) {
                                        src.sendFailure(Component.literal("Invalid value: " + raw));
                                        return 0;
                                    }
                                    var copy = new java.util.ArrayList(cur);
                                    copy.set(idx, parsed);
                                    ((ConfigEntryImpl.ListEntry) entry).set(copy);
                                    src.sendSuccess(() -> Component.literal("Set " + key + "[" + idx + "] = " + raw), true);
                                    return 1;
                                }))));

        // remove <index>
        node.then(Commands.literal("remove")
                .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            int idx = IntegerArgumentType.getInteger(ctx, "index");
                            var src = ctx.getSource();
                            List cur = entry.get();
                            if (idx < 0 || idx >= cur.size()) {
                                src.sendFailure(Component.literal("Index out of bounds: " + idx));
                                return 0;
                            }
                            if (cur.size() - 1 < entry.getMinLen()) {
                                src.sendFailure(Component.literal("List size below minLen " + entry.getMinLen()));
                                return 0;
                            }
                            var copy = new java.util.ArrayList(cur);
                            Object removed = copy.remove(idx);
                            ((ConfigEntryImpl.ListEntry) entry).set(copy);
                            src.sendSuccess(() -> Component.literal("Removed " + key + "[" + idx + "] = " + String.valueOf(removed)), true);
                            return 1;
                        })));

        return node;
    }

    private static SuggestionProvider<CommandSourceStack> listValueSuggestions(ConfigEntryImpl.ListEntry<?> entry){
        Class<?> cls = inferElemClass(entry);
        if (cls != null && cls.isEnum()) {
            Object[] vals = cls.getEnumConstants();
            return (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(vals).map(o -> ((Enum<?>) o).name()), builder);
        }
        return (ctx, builder) -> builder.buildFuture();
    }

    private static Class<?> inferElemClass(ConfigEntryImpl.ListEntry<?> entry){
        List<?> def = entry.defaultValue();
        if (def != null && !def.isEmpty() && def.getFirst() != null) return def.getFirst().getClass();
        return String.class;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object parseValue(String raw, Class<?> cls){
        try {
            if (cls == Integer.class) return Integer.parseInt(raw);
            if (cls == Long.class) return Long.parseLong(raw);
            if (cls == Float.class) return Float.parseFloat(raw);
            if (cls == Double.class) return Double.parseDouble(raw);
            if (cls == Boolean.class) return Boolean.parseBoolean(raw);
            if (cls != null && Enum.class.isAssignableFrom(cls)) {
                try { return Enum.valueOf((Class) cls, raw); }
                catch (IllegalArgumentException ex) { return Enum.valueOf((Class) cls, raw.toUpperCase()); }
            }
        } catch (Exception ignored) {}
        return raw;
    }
}
