package dev.anvilcraft.rg.tools;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.ChatFormatting;

import java.lang.reflect.Type;

public class ChatFormattingSerializer implements JsonSerializer<ChatFormatting>, JsonDeserializer<ChatFormatting> {
    @Override
    public ChatFormatting deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return ChatFormatting.getByName(json.getAsString());
    }

    @Override
    public JsonElement serialize(ChatFormatting src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }
}
