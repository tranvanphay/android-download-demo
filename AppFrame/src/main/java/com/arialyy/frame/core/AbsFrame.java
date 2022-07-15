package com.arialyy.frame.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.arialyy.frame.base.BaseApp;
import com.arialyy.frame.util.show.FL;

import java.util.Stack;

/**
 * Created by lyy on 2015/11/4.
 * APP life cycle management class management
 */
public class AbsFrame {
  private static final String TAG = "AbsFrame";
  private static final Object LOCK = new Object();
  private volatile static AbsFrame mManager = null;
  private Context mContext;
  private Stack<AbsActivity> mActivityStack = new Stack<>();

  private AbsFrame() {

  }

  private AbsFrame(Application application) {
    mContext = application.getApplicationContext();
    BaseApp.context = mContext;
    BaseApp.app = application;
  }

  /**
   * Initialize the frame
   */
  public static AbsFrame init(Application app) {
    if (mManager == null) {
      synchronized (LOCK) {
        if (mManager == null) {
          mManager = new AbsFrame(app);
        }
      }
    }
    return mManager;
  }

  /**
   * Get AppManager pipe process instance
   */
  public static AbsFrame getInstance() {
    if (mManager == null) {
      throw new NullPointerException("Please use the MVVMFrame.init() method in the application's onCreate method to initialize");
    }
    return mManager;
  }

  /**
   * Get the Activity stack
   */
  public Stack<AbsActivity> getActivityStack() {
    return mActivityStack;
  }

  /**
   * Enable exception catch
   * The log file is located at /data/data/Package Name/cache//crash/AbsExceptionFile.crash
   * */
  public void openCrashHandler() {
    openCrashHandler("", "");
  }

  /**
   * Enable exception catch
   * Need network permissions, get requests, exception parameters, the following two network permissions are required
   * * android:name="android.permission.INTERNET"
   * android:name="android.permission.ACCESS_NETWORK_STATE"
   *
   * @param serverHost server address
   * @param key data transfer key
   */
  public AbsFrame openCrashHandler(String serverHost, String key) {
    CrashHandler handler = CrashHandler.getInstance(mContext);
    handler.setServerHost(serverHost, key);
    Thread.setDefaultUncaughtExceptionHandler(handler);
    return this;
  }

  /**
   * stack size
   */
  public int getActivitySize() {
    return mActivityStack.size();
  }

  /**
   * Get the specified Activity
   */
  public AbsActivity getActivity(int location) {
    return mActivityStack.get(location);
  }

  /**
   * Add Activity to stack
   */
  public void addActivity(AbsActivity activity) {
    if (mActivityStack == null) {
      mActivityStack = new Stack<>();
    }
    mActivityStack.add(activity);
  }

  /**
   * Get the current Activity (the last one pushed in the stack)
   */
  public AbsActivity getCurrentActivity() {
    return mActivityStack.lastElement();
  }

  /**
   * End the current Activity (last pushed in the stack)
   */
  public void finishActivity() {
    finishActivity(mActivityStack.lastElement());
  }

  /**
   * End the specified Activity
   */
  public void finishActivity(AbsActivity activity) {
    if (activity != null) {
      mActivityStack.remove(activity);
      activity.finish();
    }
  }

  /**
   * Remove the specified Activity
   */
  public void removeActivity(AbsActivity activity) {
    if (activity != null) {
      mActivityStack.remove(activity);
    }
  }

  /**
   * End the Activity with the specified class name
   */
  public void finishActivity(Class<?> cls) {
    for (AbsActivity activity : mActivityStack) {
      if (activity.getClass().equals(cls)) {
        finishActivity(activity);
      }
    }
  }

  /**
   * end all activities
   */
  public void finishAllActivity() {
    for (int i = 0, size = mActivityStack.size(); i < size; i++) {
      if (mActivityStack.get(i) != null && mActivityStack.size() > 0) {
        mActivityStack.get(i).finish();
      }
    }
    mActivityStack.clear();
  }

  /**
   * Exit the application
   *
   * @param isBackground Whether to enable background operation
   */
  public void exitApp(Boolean isBackground) {
    try {
      finishAllActivity();
      ActivityManager activityMgr =
          (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
      activityMgr.restartPackage(mContext.getPackageName());
    } catch (Exception e) {
      FL.e(TAG, FL.getExceptionString(e));
    } finally {
// Note, don't support this sentence if you have a background program running
      if (!isBackground) {
        System.exit(0);
      }
    }
  }
}
