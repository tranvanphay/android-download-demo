/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.http.download;

import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.ILoaderVisitor;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.exception.AriaHTTPException;
import com.arialyy.aria.http.HttpTaskOption;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.CommonUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Combined task file information, used to obtain the length of the combined task when the length is unknown
 */
public final class HttpDGInfoTask implements IInfoTask {
  private String TAG = CommonUtil.getClassName(this);
  private Callback callback;
  private DGTaskWrapper wrapper;
  private final Object LOCK = new Object();
  private ExecutorService mPool = null;
  private boolean getLenComplete = false;
  private AtomicInteger count = new AtomicInteger();
  private AtomicInteger failCount = new AtomicInteger();
  private boolean isStop = false, isCancel = false;

  public interface DGInfoCallback extends Callback {

    /**
     * subtask failed
     */
    void onSubFail(DownloadEntity subEntity, AriaHTTPException e, boolean needRetry);

    /**
     * Combination task stops
     */
    void onStop(long len);
  }

  /**
   * Subtask callback
   */
  private Callback subCallback = new Callback() {
    @Override public void onSucceed(String url, CompleteInfo info) {
      count.getAndIncrement();
      checkGetSizeComplete(count.get(), failCount.get());
      ALog.d(TAG, "Get subtask information completed");
    }

    @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {
      ALog.e(TAG, String.format("Failed to get file information, url: %s", ((DownloadEntity) entity).getUrl()));
      count.getAndIncrement();
      failCount.getAndIncrement();
      ((DGInfoCallback) callback).onSubFail((DownloadEntity) entity, new AriaHTTPException(
          String.format("Subtask failed to get file length, url: %s", ((DownloadEntity) entity).getUrl())), needRetry);
      checkGetSizeComplete(count.get(), failCount.get());
    }
  };

  HttpDGInfoTask(DGTaskWrapper wrapper) {
    this.wrapper = wrapper;
  }

  /**
   * stop
   */
  @Override
  public void stop() {
    isStop = true;
    if (mPool != null) {
      mPool.shutdown();
    }
  }

  @Override public void cancel() {
    isCancel = true;
    if (mPool != null) {
      mPool.shutdown();
    }
  }

  @Override public void run() {
    // If it is the isUnknownSize() flag and the size acquisition is not completed, onStop will be called directly
    if (mPool != null && !getLenComplete) {
      ALog.d(TAG, "Stop combining tasks when getting length is not completed");
      mPool.shutdown();
      ((DGInfoCallback)callback).onStop(0);
      return;
    }
    // Handling the case where the combined task size is unknown
    if (wrapper.isUnknownSize()) {
      mPool = Executors.newCachedThreadPool();
      getGroupSize();
      try {
        synchronized (LOCK) {
          LOCK.wait();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (!mPool.isShutdown()) {
        mPool.shutdown();
      }
    } else {
      for (DTaskWrapper wrapper : wrapper.getSubTaskWrapper()) {
        cloneHeader(wrapper);
      }
      callback.onSucceed(wrapper.getKey(), new CompleteInfo());
    }
  }

  /*
   *Get the combined task size, use the combined task size obtained in this way, and subtasks do not need to re-obtain the file size
   */
  private void getGroupSize() {
    new Thread(new Runnable() {
      @Override public void run() {
        for (DTaskWrapper dTaskWrapper : wrapper.getSubTaskWrapper()) {
          DownloadEntity subEntity = dTaskWrapper.getEntity();
          if (subEntity.getFileSize() > 0) {
            count.getAndIncrement();
            if (subEntity.getCurrentProgress() < subEntity.getFileSize()) {
              // If it is not completed, a copy of the data is required
              cloneHeader(dTaskWrapper);
            }
            checkGetSizeComplete(count.get(), failCount.get());
            continue;
          }
          cloneHeader(dTaskWrapper);
          HttpDFileInfoTask infoTask = new HttpDFileInfoTask(dTaskWrapper);
          infoTask.setCallback(subCallback);
          mPool.execute(infoTask);
        }
      }
    }).start();
  }

  /**
   * Check whether the combined task size is completed, unblock after the acquisition is complete, and set the combined task size
   */
  private void checkGetSizeComplete(int count, int failCount) {
    if (isStop || isCancel) {
      ALog.w(TAG, "Task stopped or canceled，isStop = " + isStop + ", isCancel = " + isCancel);
      notifyLock();
      return;
    }
    if (failCount == wrapper.getSubTaskWrapper().size()) {
      callback.onFail(wrapper.getEntity(), new AriaHTTPException("Failed to get subtask length"), false);
      notifyLock();
      return;
    }
    if (count == wrapper.getSubTaskWrapper().size()) {
      long size = 0;
      for (DTaskWrapper wrapper : wrapper.getSubTaskWrapper()) {
        size += wrapper.getEntity().getFileSize();
      }
      wrapper.getEntity().setConvertFileSize(CommonUtil.formatFileSize(size));
      wrapper.getEntity().setFileSize(size);
      wrapper.getEntity().update();
      getLenComplete = true;
      ALog.d(TAG, String.format("Get the length of the combined task completed, the total length of the combined task: %s, the number of failed subtasks: %s", size, failCount));
      callback.onSucceed(wrapper.getKey(), new CompleteInfo());
      notifyLock();
    }
  }

  private void notifyLock() {
    synchronized (LOCK) {
      LOCK.notifyAll();
    }
  }

  /**
   * Subtasks use properties of parent wrapper
   */
  private void cloneHeader(DTaskWrapper taskWrapper) {
    HttpTaskOption groupOption = (HttpTaskOption) wrapper.getTaskOption();
    HttpTaskOption subOption = new HttpTaskOption();

    // set properties
    subOption.setFileLenAdapter(groupOption.getFileLenAdapter());
    subOption.setFileNameAdapter(groupOption.getFileNameAdapter());
    subOption.setUseServerFileName(groupOption.isUseServerFileName());

    subOption.setFileNameAdapter(groupOption.getFileNameAdapter());
    subOption.setRequestEnum(groupOption.getRequestEnum());
    subOption.setHeaders(groupOption.getHeaders());
    subOption.setProxy(groupOption.getProxy());
    subOption.setParams(groupOption.getParams());
    taskWrapper.setTaskOption(subOption);
  }

  @Override public void setCallback(Callback callback) {
    this.callback = callback;
  }

  @Override public void accept(ILoaderVisitor visitor) {
    visitor.addComponent(this);
  }
}
