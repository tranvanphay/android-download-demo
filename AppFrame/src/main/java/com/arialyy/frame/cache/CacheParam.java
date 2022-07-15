package com.arialyy.frame.cache;

/**
 * Created by Lyy on 2015/4/9.
 * cache parameter
 */
public interface CacheParam {

  /**
   * disk cache
   */
  public static final int DISK_CACHE = 0;
  /**
   * Default cache directory folder name
   */
  public static final String DEFAULT_DIR = "defaultDir";
  /**
   * memory cache
   */
  public static final int MEMORY_CACHE_SIZE = 1;
  /**
   * Small-capacity disk cache
   */
  public static final long SMALL_DISK_CACHE_CAPACITY = 4 * 1024 * 1024;
  /**
   * Medium capacity disk cache
   */
  public static final long NORMAL_DISK_CACHE_CAPACITY = 10 * 1024 * 1024;
  /**
   * Large capacity disk cache
   */
  public static final long LARGER_DISKCACHE_CAPACITY = 20 * 1024 * 1024;
  /**
   * cache index
   */
  public static final int DISK_CACHE_INDEX = 0;
}
