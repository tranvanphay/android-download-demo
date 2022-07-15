package com.arialyy.frame.base.net;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 * Custom Gson description
 * Created by “Aria.Lao” on 2016/10/26.
 *
 * @param <T> server data entity
 */
public class BasicDeserializer<T> implements JsonDeserializer<T> {
  @Override
  public T deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject root = element.getAsJsonObject();
    if (JsonCodeAnalysisUtil.isSuccess(root)) {
      return new Gson().fromJson(root.get("object"), typeOfT);
    } else {
      throw new IllegalStateException(root.get("rltmsg").getAsString());
    }
  }
}