package org.betterx.bclib.commands.arguments;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonObject;

public class Float3ArgumentInfo implements ArgumentTypeInfo<Float3ArgumentType, Float3ArgumentInfo.Template> {
    private static byte MIN_FLAG = 1 << 1;
    private static byte MAX_FLAG = 1 << 2;
    private static byte IS_INT_FLAG = 1 << 0;

    private static byte createNumberFlags(boolean isInt, boolean hasMin, boolean hasMax) {
        byte flagByte = 0;
        if (isInt) flagByte |= IS_INT_FLAG;
        if (hasMin) flagByte |= MIN_FLAG;
        if (hasMax) flagByte |= MAX_FLAG;

        return flagByte;
    }

    private static boolean numberHasMin(byte flag) {
        return (flag & MIN_FLAG) != 0;
    }

    private static boolean numberHasMax(byte flag) {
        return (flag & MAX_FLAG) != 0;
    }

    private static boolean numberIsInt(byte flag) {
        return (flag & IS_INT_FLAG) != 0;
    }

    public void serializeToNetwork(
            Float3ArgumentInfo.Template template,
            FriendlyByteBuf friendlyByteBuf
    ) {
        final boolean hasMin = template.min != -Double.MAX_VALUE;
        final boolean hasMax = template.max != Double.MAX_VALUE;
        friendlyByteBuf.writeByte(createNumberFlags(template.asInt, hasMin, hasMax));
        if (hasMin) friendlyByteBuf.writeDouble(template.min);
        if (hasMax) friendlyByteBuf.writeDouble(template.max);
    }

    public Float3ArgumentInfo.Template deserializeFromNetwork(
            FriendlyByteBuf friendlyByteBuf
    ) {
        byte flag = friendlyByteBuf.readByte();
        boolean asInt = numberIsInt(flag);
        double min = numberHasMin(flag) ? friendlyByteBuf.readDouble() : -Double.MAX_VALUE;
        double max = numberHasMax(flag) ? friendlyByteBuf.readDouble() : Double.MAX_VALUE;
        return new Float3ArgumentInfo.Template(asInt, min, max);
    }

    public void serializeToJson(Float3ArgumentInfo.Template template, JsonObject jsonObject) {
        if (!template.asInt) {
            jsonObject.addProperty("asInt", template.asInt);
        }
        if (template.min != -Double.MAX_VALUE) {
            jsonObject.addProperty("min", template.min);
        }
        if (template.max != Double.MAX_VALUE) {
            jsonObject.addProperty("max", template.max);
        }

    }

    public Float3ArgumentInfo.Template unpack(Float3ArgumentType type) {
        return new Float3ArgumentInfo.Template(type.isInt(), type.getMinimum(), type.getMaximum());
    }

    public final class Template implements ArgumentTypeInfo.Template<Float3ArgumentType> {
        final double min;
        final double max;
        final boolean asInt;

        Template(boolean asInt, double min, double max) {
            this.min = min;
            this.max = max;
            this.asInt = asInt;

        }

        public Float3ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.asInt
                    ? Float3ArgumentType.int3((int) this.min, (int) this.max)
                    : Float3ArgumentType.float3(this.min, this.max);
        }

        public ArgumentTypeInfo<Float3ArgumentType, ?> type() {
            return Float3ArgumentInfo.this;
        }
    }
}
