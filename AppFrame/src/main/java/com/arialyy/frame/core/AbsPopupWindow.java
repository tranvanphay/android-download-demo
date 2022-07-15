package com.arialyy.frame.core;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import androidx.annotation.NonNull;
import com.arialyy.frame.module.AbsModule;
import com.arialyy.frame.module.IOCProxy;
import com.arialyy.frame.util.StringUtil;

/**
 * Created by lyy on 2015/12/3.
 * Abstract Popupwindow floating frame
 */
public abstract class AbsPopupWindow extends PopupWindow {

  protected String TAG;
  private Context mContext;
  private Drawable mBackground;
  protected View mView;
  private Object mObj;
  protected IOCProxy mProxy;
  protected DialogSimpleModule mSimpleModule;
  private ModuleFactory mModuleF;

  public AbsPopupWindow(Context context) {
    this(context, null);
  }

  public AbsPopupWindow(Context context, Drawable background) {
    this(context, background, null);
  }

  public AbsPopupWindow(Context context, Drawable background, Object obj) {
    mContext = context;
    mBackground = background;
    initPopupWindow();
    mProxy = IOCProxy.newInstance(this);
    if (obj != null) {
      mObj = obj;
      mSimpleModule = new DialogSimpleModule(getContext());
      IOCProxy.newInstance(mObj, mSimpleModule);
    }
    mModuleF = ModuleFactory.newInstance();
    init();
  }

  protected void init() {

  }

  private void initPopupWindow() {
    mView = LayoutInflater.from(mContext).inflate(setLayoutId(), null);
    setContentView(mView);
    TAG = StringUtil.getClassName(this);
    // Set the width of the SelectPicPopupWindow popup form
    setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    //// Set the height of the SelectPicPopupWindow popup form
    setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    setFocusable(true);
    // Set the animation effect of the SelectPicPopupWindow pop-up form
    //        setAnimationStyle(R.style.wisdom_anim_style);
    // Instantiate a ColorDrawable color for translucent
    if (mBackground == null) {
      mBackground = new ColorDrawable(Color.parseColor("#4f000000"));
    }
    // Set the background of the SelectPicPopupWindow pop-up form
    setBackgroundDrawable(mBackground);
  }

  protected <T extends View> T getViewWithTag(Object tag) {
    T result = (T) mView.findViewWithTag(tag);
    if (result == null) throw new NullPointerException("没有找到tag为【" + tag + "】的控件");
    return result;
  }

  /**
   * 获取Module
   *
   * @param clazz {@link AbsModule}
   */
  protected <M extends AbsModule> M getModule(Class<M> clazz) {
    M module = mModuleF.getModule(getContext(), clazz);
    mProxy.changeModule(module);
    return module;
  }

  /**
   * 获取Module
   *
   * @param clazz Module class0
   * @param callback Module回调函数
   * @param <M> {@link AbsModule}
   */
  protected <M extends AbsModule> M getModule(@NonNull Class<M> clazz,
      @NonNull AbsModule.OnCallback callback) {
    M module = mModuleF.getModule(getContext(), clazz);
    module.setCallback(callback);
    mProxy.changeModule(module);
    return module;
  }

  /**
   * Get a simple call to the Moduel callback, which is generally used to call back data to the host
   */
  protected DialogSimpleModule getSimplerModule() {
    if (mObj == null) {
      throw new NullPointerException("Host object must be set");
    }
    return mSimpleModule;
  }

  public Context getContext() {
    return mContext;
  }

  /**
   * Set resource layout
   */
  protected abstract int setLayoutId();

  /**
   * data callback
   */
  protected abstract void dataCallback(int result, Object obj);
}
