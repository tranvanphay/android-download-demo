package com.arialyy.frame.cache;

import android.os.Environment;

/**
 * Created by AriaL on 2017/11/26.
 */

public class PathConstaant {
  private static final String WP_DIR = "windPath";

  /**
   * Get APK upgrade path
   */
  public static String getWpPath() {
    return Environment.getExternalStorageDirectory().getPath()
        + "/"
        + WP_DIR
        + "/update/windPath.apk";
  }
}
