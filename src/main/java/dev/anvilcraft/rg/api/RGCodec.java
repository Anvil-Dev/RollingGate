/**
 * Realms of the Gods (RG) 编码解码器接口
 * 提供自定义类型与JSON之间的序列化和反序列化功能
 * 
 * @param <T> 要序列化和反序列化的类型
 */
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

/**
 * RGCodec 类实现 JsonDeserializer 和 JsonSerializer 接口
 * 提供自定义类型与JSON之间的序列化和反序列化功能
 * 
 * @param <T> 要序列化和反序列化的类型
 */
public class RGCodec<T> implements JsonDeserializer<T>, JsonSerializer<T> {
    // 类型类
    private final Class<T> clazz;
    // 解码函数，将字符串转换为指定类型
    private final Function<String, T> decoder;
    // 编码函数，将指定类型转换为字符串
    private final Function<T, String> encoder;
    // 是否锁定，表示该类型是否具有固定的编解码逻辑
    private final boolean isBuiltIn;

    // 预定义的字符串类型编解码器
    public static final RGCodec<String> STRING = new RGCodec<>(String.class, String::toString, String::toString, true);
    // 预定义的布尔类型编解码器
    public static final RGCodec<Boolean> BOOLEAN = new RGCodec<>(Boolean.class, s -> s.equals("true"), Object::toString, true);
    // 预定义的字节类型编解码器
    public static final RGCodec<Byte> BYTE = new RGCodec<>(Byte.class, Byte::parseByte, Object::toString, true);
    // 预定义的短整型编解码器
    public static final RGCodec<Short> SHORT = new RGCodec<>(Short.class, Short::parseShort, Object::toString, true);
    // 预定义的整型编解码器
    public static final RGCodec<Integer> INTEGER = new RGCodec<>(Integer.class, Integer::parseInt, Object::toString, true);
    // 预定义的长整型编解码器
    public static final RGCodec<Long> LONG = new RGCodec<>(Long.class, Long::parseLong, Object::toString, true);
    // 预定义的浮点型编解码器
    public static final RGCodec<Float> FLOAT = new RGCodec<>(Float.class, Float::parseFloat, Object::toString, true);
    // 预定义的双精度浮点型编解码器
    public static final RGCodec<Double> DOUBLE = new RGCodec<>(Double.class, Double::parseDouble, Object::toString, true);

    /**
     * 构造一个 RGCodec 实例
     * 
     * @param clazz 类型类
     * @param decoder 解码函数
     * @param encoder 编码函数
     * @param isBuiltIn 是否内置
     */
    private RGCodec(Class<T> clazz, Function<String, T> decoder, Function<T, String> encoder, boolean isBuiltIn) {
        this.clazz = clazz;
        this.decoder = decoder;
        this.encoder = encoder;
        this.isBuiltIn = isBuiltIn;
    }

    /**
     * 创建一个 RGCodec 实例
     * 
     * @param clazz 类型类
     * @param decoder 解码函数
     * @param encoder 编码函数
     * @param <T> 类型
     * @return RGCodec 实例
     */
    @SuppressWarnings("unused")
    public static <T> @NotNull RGCodec<T> of(Class<T> clazz, Function<String, T> decoder, Function<T, String> encoder) {
        return new RGCodec<>(clazz, decoder, encoder, false);
    }

    /**
     * 获取类型类
     * 
     * @return 类型类
     */
    public Class<T> clazz() {
        return this.clazz;
    }

    /**
     * 检查编解码器是否内置
     * 
     * @return 是否内置
     */
    public boolean isBuiltIn() {
        return isBuiltIn;
    }

    /**
     * 解码字符串为指定类型
     * 
     * @param str 字符串
     * @return 解码后的对象
     */
    public T decode(String str) {
        if (str == null) return null;
        return this.decoder.apply(str);
    }

    /**
     * 编码对象为字符串
     * 
     * @param value 对象
     * @return 编码后的字符串
     */
    public String encode(T value) {
        if (value == null) return null;
        return this.encoder.apply(value);
    }

    /**
     * 强制编码对象为字符串，不进行类型检查
     * 
     * @param value 对象
     * @return 编码后的字符串
     */
    public String forceEncode(Object value) {
        if (value == null) return null;
        //noinspection unchecked
        return this.encoder.apply((T) value);
    }

    /**
     * 从 JsonElement 中反序列化为指定类型对象
     * 
     * @param json JSON元素
     * @param typeOfT 类型
     * @param context 反序列化上下文
     * @return 反序列化后的对象
     * @throws JsonParseException JSON解析异常
     */
    @Override
    public T deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return this.decode(json.getAsString());
    }

    /**
     * 序列化对象为 JsonElement
     * 
     * @param src 对象
     * @param typeOfSrc 类型
     * @param context 序列化上下文
     * @return 序列化后的 JSON元素
     */
    @Override
    public @NotNull JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(this.encode(src));
    }
}
