package org.betterx.bclib.commands.arguments;

import de.ambertation.wunderlib.math.Float3;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Float3ArgumentType implements ArgumentType<Float3> {
    private static final Collection<String> EXAMPLES = List.of("0 0 0");

    private final double minimum;
    private final double maximum;
    private final boolean asInt;

    Float3ArgumentType(int minimum, int maximum) {
        this(true, minimum, maximum);
    }

    Float3ArgumentType(double minimum, double maximum) {
        this(false, minimum, maximum);
    }

    Float3ArgumentType(boolean asInt, double minimum, double maximum) {
        this.asInt = asInt;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public boolean isInt() {
        return asInt;
    }

    @Override
    public Float3 parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        double x = asInt ? parseInt(reader) : parseDouble(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
        }
        reader.skip();
        double y = asInt ? parseInt(reader) : parseDouble(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(i);
            throw Vec3Argument.ERROR_NOT_COMPLETE.createWithContext(reader);
        }
        reader.skip();
        double z = asInt ? parseInt(reader) : parseDouble(reader);
        return Float3.of(x, y, z);
    }

    private double parseDouble(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(reader);
        } else {
            final int start = reader.getCursor();
            double result = reader.canRead() && reader.peek() != ' ' ? reader.readDouble() : 0.0;

            if (result < minimum) {
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow()
                                                                .createWithContext(reader, result, minimum);
            }
            if (result > maximum) {
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh()
                                                                .createWithContext(reader, result, maximum);
            }

            return result;
        }
    }

    private double parseInt(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            throw WorldCoordinate.ERROR_EXPECTED_DOUBLE.createWithContext(reader);
        } else {
            final int start = reader.getCursor();
            int result = reader.canRead() && reader.peek() != ' ' ? reader.readInt() : 0;

            if (result < minimum) {
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow()
                                                                .createWithContext(reader, result, minimum);
            }
            if (result > maximum) {
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh()
                                                                .createWithContext(reader, result, maximum);
            }

            return result;
        }
    }

    public static Float3ArgumentType int3() {
        return new Float3ArgumentType(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static Float3ArgumentType int3(int min) {
        return new Float3ArgumentType(min, Double.MAX_VALUE);
    }

    public static Float3ArgumentType int3(int min, int max) {
        return new Float3ArgumentType(min, max);
    }

    public static Float3ArgumentType float3() {
        return new Float3ArgumentType(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static Float3ArgumentType float3(double min) {
        return new Float3ArgumentType(min, Double.MAX_VALUE);
    }

    public static Float3ArgumentType float3(double min, double max) {
        return new Float3ArgumentType(min, max);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining();
        return SharedSuggestionProvider.suggestCoordinates(
                remaining,
                List.of(new SharedSuggestionProvider.TextCoordinates("8", "8", "8")),
                builder,
                Commands.createValidator(this::parse)
        );
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static Float3 getFloat3(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, Float3.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Float3ArgumentType)) return false;
        Float3ArgumentType that = (Float3ArgumentType) o;
        return Double.compare(that.minimum, minimum) == 0 && Double.compare(
                that.maximum,
                maximum
        ) == 0 && asInt == that.asInt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minimum, maximum, asInt);
    }

    @Override
    public String toString() {
        if (asInt) {
            if (minimum == -Double.MAX_VALUE && maximum == Double.MAX_VALUE) {
                return "int3()";
            } else if (maximum == Double.MAX_VALUE) {
                return "int3(" + (int) minimum + ")";
            } else {
                return "int3(" + (int) minimum + ", " + (int) maximum + ")";
            }
        } else {
            if (minimum == -Double.MAX_VALUE && maximum == Double.MAX_VALUE) {
                return "float3()";
            } else if (maximum == Double.MAX_VALUE) {
                return "float3(" + (int) minimum + ")";
            } else {
                return "float3(" + (int) minimum + ", " + (int) maximum + ")";
            }
        }
    }
}
