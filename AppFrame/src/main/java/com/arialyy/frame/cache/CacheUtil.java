package com.arialyy.frame.cache;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import com.arialyy.frame.util.DrawableUtil;
import com.arialyy.frame.util.show.L;
import com.google.gson.Gson;
import java.io.UnsupportedEncodingException;

/**
 * Created by AriaLyy on 2015/4/9.
 * cache tool
 */
public class CacheUtil extends AbsCache {
  private static final String TAG = "CacheUtil";

  /**
   * Use the default path by default
   *
   * @param useMemory Whether to use memory cache
   */
  public CacheUtil(Context context, boolean useMemory) {
    super(context, useMemory);
  }

  /**
   * Specify cache folder
   *
   * @param useMemory Whether to use memory cache
   * @param cacheDir cache folder
   */
  public CacheUtil(Context context, boolean useMemory, @NonNull String cacheDir) {
    super(context, useMemory, cacheDir);
  }

  /**
   * Set whether to use memory cache
   */
  public void setUseMemoryCache(boolean useMemoryCache) {
    setUseMemory(useMemoryCache);
  }

  /**
   * Open the cache in a directory
   *
   * @param cacheDir Cache directory, just fill in the folder name, no need to write the path
   * @param valueCount Specify how many cache files the same key can correspond to, basically pass 1
   * @param cacheSize cache size
   * @see CacheParam
   */
  public void openCache(String cacheDir, int valueCount, long cacheSize) {
    openDiskCache(cacheDir, valueCount, cacheSize);
  }

  /**
   * Write to Bitmap type cache, note: you need to flush at a specific time, generally write in onPause(), onDestroy()   *
   * @param key key value, usually url
   * @param bitmap data to be written
   */
  public void putBitmapCache(String key, Bitmap bitmap) {
    byte[] data = DrawableUtil.getBitmapByte(bitmap);
    putByteCache(key, data);
  }

  /**
   * Get the bitmap in the cache
   */
  public Bitmap getBitmapCache(String key) {
    byte[] data = getByteCache(key);
    return DrawableUtil.getBitmapFromByte(data);
  }

  /**
   * Write to String type cache, note: it needs to be flushed at a specific time, usually written in onPause(), onDestroy()   *
   * @param key key value, usually url
   * @param data data to be written
   */
  public void putStringCache(String key, String data) {
    try {
      putByteCache(key, data.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  /**
   * read string cache
   */
  public String getStringCache(String key) {
    byte[] data = getByteCache(key);
    String str = "";
    if (data != null) {
      try {
        str = new String(data, "utf-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
    return str;
  }

  /**
   * Write byte type cache, note: you need to flush at a specific time, generally write in onPause(), onDestroy()   *
   * @param key key value, usually url
   * @param data data to be written
   */
  public void putByteCache(String key, byte[] data) {
    writeDiskCache(key, data);
  }

  /**
   * Read byte type cache
   *
   * @param key cache key
   */
  public byte[] getByteCache(String key) {
    return readDiskCache(key);
  }

  /**
   * After writing to the object cache, note: you need to flush at a specific time, usually in onPause(), onDestroy()   *
   * @param clazz object type
   * @param key cache key value
   * @param object object
   */
  public void putObjectCache(Class<?> clazz, String key, Object object) {
    String json = new Gson().toJson(object, clazz);
    try {
      writeDiskCache(key, json.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      L.e(TAG, "code conversion error", e);
    }
  }

  /**
   * read object cache
   *
   * @param clazz object type
   * @param key cache key value
   */
  public <T> T getObjectCache(Class<T> clazz, String key) {
    T object = null;
    try {
      byte[] data = readDiskCache(key);
      if (data == null) {
        return null;
      }
      object = new Gson().fromJson(new String(data, "utf-8"), clazz);
    } catch (UnsupportedEncodingException e) {
      L.e(TAG, "code conversion error", e);
    }
    return object;
  }

  /**
   * Synchronized records, if they are not synchronized, the cache cannot be extracted
   * */
  public void flush() {
    flushDiskCache();
  }

  /**
   * delete a cache
   *
   * @param key cache key value
   */
  public void remove(String key) {
    readDiskCache(key);
  }

  /**
   * delete all cache
   */
  public void removeAll() {
    clearCache();
  }

  /**
   * Turn off disk caching
   */
  public void close() {
    closeDiskCache();
  }

  /**
   * get cache size
   */
  public long getCacheSize() {
    return super.getCacheSize();
  }
}
