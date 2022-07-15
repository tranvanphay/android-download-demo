package com.arialyy.frame.core;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.arialyy.frame.module.AbsModule;
import com.arialyy.frame.module.IOCProxy;
import com.arialyy.frame.util.StringUtil;

/**
 * Created by lyy on 2015/11/4.
 * AlertDialog base class, with 5.0 effect, needs to be used with AlertDialog.Builder
 * */
public abstract class AbsAlertDialog extends DialogFragment {
  protected String TAG = "";

  private Object mObj;    //Observed
  private IOCProxy mProxy;
  private DialogSimpleModule mSimpleModule;
  private Dialog mDialog;
  private ModuleFactory mModuleF;

  public AbsAlertDialog() {
    this(null);
  }

  /**
   * @param obj object being observed
   */
  public AbsAlertDialog(Object obj) {
    mObj = obj;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initDialog();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return mDialog;
  }

  /**
   * Create AlertDialog
   * It is recommended to use AppCompatDialog, which has the effect of 5.0
   * */
  public abstract Dialog initAlertDialog();

  private void initDialog() {
    TAG = StringUtil.getClassName(this);
    mProxy = IOCProxy.newInstance(this);
    if (mObj != null) {
      mSimpleModule = new DialogSimpleModule(getContext());
      IOCProxy.newInstance(mObj, mSimpleModule);
    }
    mModuleF = ModuleFactory.newInstance();
    mDialog = initAlertDialog();
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
   * Get a simple Moduel callback, which is generally used to call back data to the host
   * */
  protected DialogSimpleModule getSimplerModule() {
    if (mObj == null) {
      throw new NullPointerException("Host object must be set");
    }
    return mSimpleModule;
  }

  protected abstract void dataCallback(int result, Object obj);

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionHelp.getInstance().handlePermissionCallback(requestCode, permissions, grantResults);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    PermissionHelp.getInstance()
        .handleSpecialPermissionCallback(getContext(), requestCode, resultCode, data);
  }
}
