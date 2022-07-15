package com.arialyy.frame.base.net;

/**
 * Created by “Aria.Lao” on 2016/10/25.
 * Network response interface, all network callbacks must inherit this interface
 *
 * @param <T> 数据实体结构
 */
public interface INetResponse<T> {

  /**
   * The network request was successful
   */
  public void onResponse(T response);

  /**
   * Request failed
   */
  public void onFailure(Throwable e);
}
