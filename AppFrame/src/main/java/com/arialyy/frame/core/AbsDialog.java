package com.arialyy.frame.core;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import androidx.annotation.NonNull;
import com.arialyy.frame.module.AbsModule;
import com.arialyy.frame.module.IOCProxy;
import com.arialyy.frame.util.StringUtil;

/**
 * Created by lyy on 2015/11/4.
 * Inherit Dialog
 */
public abstract class AbsDialog extends Dialog {
  protected String TAG = "";
  private Object mObj;    //Observed
  private IOCProxy mProxy;
  private DialogSimpleModule mSimpleModule;
  private ModuleFactory mModuleF;

  public AbsDialog(Context context) {
    this(context, null);
  }

  /**
   * @param obj Dialog's host   */
  public AbsDialog(Context context, Object obj) {
    super(context);
    mObj = obj;
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(setLayoutId());
    initDialog();
  }

  private void initDialog() {
    TAG = StringUtil.getClassName(this);
    mProxy = IOCProxy.newInstance(this);
    mModuleF = ModuleFactory.newInstance();
    if (mObj != null) {
      mSimpleModule = new DialogSimpleModule(getContext());
      IOCProxy.newInstance(mObj, mSimpleModule);
    }
  }

  /**
   * Get a simple Moduel callback, which is generally used to call back data to the host
   * */
  protected DialogSimpleModule getSimplerModule() {
    if (mObj == null) {
      throw new NullPointerException("Host object must be set");
    }
    return mSimpleModule;
  }

  /**
   * Get Module
   *
   * @param clazz {@link AbsModule}
   */
  protected <M extends AbsModule> M getModule(Class<M> clazz) {
    M module = mModuleF.getModule(getContext(), clazz);
    mProxy.changeModule(module);
    return module;
  }

  /**
   * Get Module
   *
   * @param clazz Module class0
   * @param callback Module callback function
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
   * Set resource layout
   */
  protected abstract int setLayoutId();

  /**
   * data callback
   */
  protected abstract void dataCallback(int result, Object obj);
}
