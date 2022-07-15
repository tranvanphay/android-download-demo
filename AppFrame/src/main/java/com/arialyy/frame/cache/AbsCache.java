package com.arialyy.frame.cache;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import com.arialyy.frame.util.AndroidUtils;
import com.arialyy.frame.util.AppUtils;
import com.arialyy.frame.util.StreamUtil;
import com.arialyy.frame.util.StringUtil;
import com.arialyy.frame.util.show.FL;
import com.arialyy.frame.util.show.L;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Lyy on 2015/4/9.
 * Cache abstract class that encapsulates cache read and write operations
 */
public abstract class AbsCache implements CacheParam {
  private static final String TAG = "AbsCache";
  /**
   * Disk cache tool
   */
  private DiskLruCache mDiskLruCache = null;
  /**
   * memory cache tool
   */
  private LruCache<String, byte[]> mMemoryCache = null;
  /**
   * Whether to use memory cache
   */
  private boolean useMemory = false;
  private int mMaxMemory;
  private Context mContext;
  private static final Object mDiskCacheLock = new Object();

  /**
   * Use the default path by default
   *
   * @param useMemory Whether to use memory cache
   */
  protected AbsCache(Context context, boolean useMemory) {
    this.mContext = context;
    this.useMemory = useMemory;
    init(DEFAULT_DIR, 1, SMALL_DISK_CACHE_CAPACITY);
  }

  /**
   * Specify cache folder
   *
   * @param useMemory Whether to use memory cache
   * @param cacheDir cache folder
   */
  protected AbsCache(Context context, boolean useMemory, @NonNull String cacheDir) {
    this.mContext = context;
    this.useMemory = useMemory;
    init(cacheDir, 1, SMALL_DISK_CACHE_CAPACITY);
  }

  private void init(String cacheDir, int valueCount, long cacheSize) {
    initDiskCache(cacheDir, valueCount, cacheSize);
    initMemoryCache();
  }

  /**
   * Initialize disk cache
   */
  protected void initDiskCache(String cacheDir, int valueCount, long cacheSize) {
    try {
      File dir = getDiskCacheDir(mContext, cacheDir);
      if (!dir.exists()) {
        dir.mkdirs();
      }
      mDiskLruCache =
          DiskLruCache.open(dir, AppUtils.getVersionNumber(mContext), valueCount, cacheSize);
    } catch (IOException e) {
      FL.e(this, "createCacheFailed\n" + FL.getExceptionString(e));
    }
  }

  /**
   * Initialize the memory cache
   */
  protected void initMemoryCache() {
    if (!useMemory) {
      return;
    }
    // Get the maximum available memory for the application
    mMaxMemory = (int) Runtime.getRuntime().maxMemory();
    // Set the image cache size to 1/8 of the program's maximum available memory
    mMemoryCache = new LruCache<>(mMaxMemory / 8);
  }

  /**
   * Whether to use memory cache
   */
  protected void setUseMemory(boolean useMemory) {
    this.useMemory = useMemory;
    initMemoryCache();
  }

  /**
   * Set the memory cache size
   */
  protected void setMemoryCache(int size) {
    mMemoryCache.resize(size);
  }

  /**
   * Open the cache in a directory
   *
   * @param cacheDir Cache directory, just fill in the folder name, no need to write the path
   * @param valueCount Specify how many cache files the same key can correspond to, basically pass 1
   * @param cacheSize cache size
   * @see CacheParam
   */
  protected void openDiskCache(@NonNull String cacheDir, int valueCount, long cacheSize) {
    synchronized (mDiskCacheLock) {
      if (mDiskLruCache != null && mDiskLruCache.isClosed()) {
        try {
          File dir = getDiskCacheDir(mContext, cacheDir);
          if (!dir.exists()) {
            dir.mkdirs();
          }
          mDiskLruCache =
              DiskLruCache.open(dir, AppUtils.getVersionNumber(mContext), valueCount, cacheSize);
        } catch (IOException e) {
          FL.e(this, "createCacheFailed\n" + FL.getExceptionString(e));
        }
      }
    }
  }

  /**
   * write cache to disk
   *
   * @param key The cached key, through which the cache is read and written, usually a URL
   * @param data cached data
   */
  protected void writeDiskCache(@NonNull String key, @NonNull byte[] data) {
    if (TextUtils.isEmpty(key)) {
      return;
    }
    String hashKey = StringUtil.keyToHashKey(key);
    if (useMemory && mMemoryCache != null) {
      mMemoryCache.put(hashKey, data);
    }
    synchronized (mDiskCacheLock) {
      if (mDiskLruCache != null) {
        L.i(TAG, "cache data to disk[key:" + key + ",hashKey:" + hashKey + "]");
        OutputStream out = null;
        try {
          DiskLruCache.Editor editor = mDiskLruCache.edit(hashKey);
          out = editor.newOutputStream(DISK_CACHE_INDEX);
          out.write(data, 0, data.length);
          editor.commit();
          out.flush();
          out.close();
        } catch (IOException e) {
          FL.e(this,
              "writeDiskFailed[key:" + key + ",hashKey:" + hashKey + "]\n" + FL.getExceptionString(
                  e));
        } finally {
          if (out != null) {
            try {
              out.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
  }

  /**
   * read cache from disk
   *
   * @param key The cached key, usually the original url
   * @return cache data
   */
  protected byte[] readDiskCache(@NonNull String key) {
    if (TextUtils.isEmpty(key)) {
      return null;
    }
    String hashKey = StringUtil.keyToHashKey(key);
    if (useMemory && mMemoryCache != null) {
      final byte[] data = mMemoryCache.get(hashKey);
      if (data != null && data.length != 0) {
        return data;
      }
    }
    synchronized (mDiskCacheLock) {
      byte[] data = null;
      L.i(TAG, "Read disk cache data[key:" + key + ",hashKey:" + hashKey + "]");
      InputStream inputStream = null;
      try {
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKey);
        if (snapshot != null) {
          inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
          data = StreamUtil.readStream(inputStream);
          return data;
        }
      } catch (IOException e) {
        FL.e(this, "readDiskCacheFailed[key:"
            + key
            + ",hashKey:"
            + hashKey
            + "]\n"
            + FL.getExceptionString(e));
      } catch (Exception e) {
        FL.e(this, "readDiskCacheFailed[key:"
            + key
            + ",hashKey:"
            + hashKey
            + "]\n"
            + FL.getExceptionString(e));
      } finally {
        if (inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return null;
  }

  /**
   * delete a cache
   *
   * @param key the cache key
   */
  protected void removeCache(@NonNull String key) {
    String hashKey = StringUtil.keyToHashKey(key);
    if (mMemoryCache != null) {
      mMemoryCache.remove(hashKey);
    }
    synchronized (mDiskCacheLock) {
      if (mDiskLruCache != null) {
        try {
          mDiskLruCache.remove(hashKey);
        } catch (IOException e) {
          FL.e(this, "removeCacheFailed[key:"
              + key
              + ",hashKey:"
              + hashKey
              + "]\n"
              + FL.getExceptionString(e));
        }
      }
    }
  }

  /**
   * clear all cache
   */
  protected void clearCache() {
    if (mMemoryCache != null) {
      mMemoryCache.evictAll();
    }
    synchronized (mDiskCacheLock) {
      if (mDiskLruCache != null) {
        try {
          mDiskLruCache.delete();
        } catch (IOException e) {
          FL.e(this, "clearCacheFailed" + FL.getExceptionString(e));
        }
      }
    }
  }

  /**
   * Turn off disk cache, note:
   * This method is used to close DiskLruCache, which is a method corresponding to the open() method.
   * After closing it, you can no longer call any method of operating the cached data in DiskLruCache.
   * Usually you should only call the close() method in the Activity's onDestroy() method.
   */
  protected void closeDiskCache() {
    synchronized (mDiskCacheLock) {
      if (mDiskLruCache != null) {
        try {
          mDiskLruCache.close();
        } catch (IOException e) {
          FL.e(this, "closeDiskCacheFailed" + FL.getExceptionString(e));
        }
      }
    }
  }

  /**
   * Synchronous in-memory cache operations are recorded to the log file (that is, the journal file)
   * Note: Flush synchronization is required once when writing to the cache. It is not necessary to call the flush() method every time the cache is written. Frequent calls will not bring any benefits.
   * will only add additional time to sync journal files. The standard approach is to call the flush() method once in the onPause() method of the Activity.
   */
  protected void flushDiskCache() {
    synchronized (mDiskCacheLock) {
      if (mDiskLruCache != null) {
        try {
          mDiskLruCache.flush();
        } catch (IOException e) {
          FL.e(this, "flushDiskCacheFailed" + FL.getExceptionString(e));
        }
      }
    }
  }

  /**
   * get cache size
   */
  protected long getCacheSize() {
    return mDiskLruCache.size();
  }

  /**
   * Convert byte array to String
   */
  private static String bytesToHexString(byte[] bytes) {
    // http://stackoverflow.com/questions/332079
    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      String hex = Integer.toHexString(0xFF & aByte);
      if (hex.length() == 1) {
        sb.append('0');
      }
      sb.append(hex);
    }
    return sb.toString();
  }

  /**
   * Generate cache folder
   *
   * @param uniqueName cache folder name
   * @return cache folder
   */
  public static File getDiskCacheDir(Context context, String uniqueName) {
    return new File(AndroidUtils.getDiskCacheDir(context) + File.separator + uniqueName);
  }
}
