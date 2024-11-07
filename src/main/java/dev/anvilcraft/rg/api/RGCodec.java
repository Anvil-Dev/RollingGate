package dev.anvilcraft.rg.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.function.Function;

public class RGCodec<T> implements JsonDeserializer<T>, JsonSerializer<T> {
    private final Class<T> clazz;
    private final Function<String, T> decoder;
    private final Function<T, String> encoder;
    private final boolean isLock;
    public static final RGCodec<String> STRING = new RGCodec<>(String.class, String::toString, String::toString, true);
    public static final RGCodec<Boolean> BOOLEAN = new RGCodec<>(Boolean.class, s -> s.equals("true"), Object::toString, true);
    public static final RGCodec<Byte> BYTE = new RGCodec<>(Byte.class, Byte::parseByte, Object::toString, true);
    public static final RGCodec<Short> SHORT = new RGCodec<>(Short.class, Short::parseShort, Object::toString, true);
    public static final RGCodec<Integer> INTEGER = new RGCodec<>(Integer.class, Integer::parseInt, Object::toString, true);
    public static final RGCodec<Long> LONG = new RGCodec<>(Long.class, Long::parseLong, Object::toString, true);
    public static final RGCodec<Float> FLOAT = new RGCodec<>(Float.class, Float::parseFloat, Object::toString, true);
    public static final RGCodec<Double> DOUBLE = new RGCodec<>(Double.class, Double::parseDouble, Object::toString, true);

    private RGCodec(Class<T> clazz, Function<String, T> decoder, Function<T, String> encoder, boolean isLock) {
        this.clazz = clazz;
        this.decoder = decoder;
        this.encoder = encoder;
        this.isLock = isLock;
    }

    @SuppressWarnings("unused")
    public static <T> @NotNull RGCodec<T> of(Class<T> clazz, Function<String, T> decoder, Function<T, String> encoder) {
        return new RGCodec<>(clazz, decoder, encoder, false);
    }

    public Class<T> clazz() {
        return this.clazz;
    }

    public boolean isLock() {
        return isLock;
    }

    public T decode(String str) {
        if (str == null) return null;
        return this.decoder.apply(str);
    }

    public String encode(T value) {
        if (value == null) return null;
        return this.encoder.apply(value);
    }

    public String forceEncode(Object value) {
        if (value == null) return null;
        //noinspection unchecked
        return this.encoder.apply((T) value);
    }

    @Override
    public T deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return this.decode(json.getAsString());
    }

    @Override
    public @NotNull JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(this.encode(src));
    }
}