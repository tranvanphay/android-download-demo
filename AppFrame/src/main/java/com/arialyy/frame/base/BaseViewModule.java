package com.arialyy.frame.base;

import androidx.lifecycle.ViewModel;
import com.arialyy.frame.base.net.NetManager;
import com.arialyy.frame.util.StringUtil;

/**
 * Created by AriaL on 2017/11/26.
 * ViewModule can only be public
 */

public class BaseViewModule extends ViewModel {
  protected NetManager mNetManager;
  protected String TAG = "";

  public BaseViewModule() {
    mNetManager = NetManager.getInstance();
    TAG = StringUtil.getClassName(this);
  }
}
