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
package com.arialyy.aria.m3u8.live;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.common.CompleteInfo;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.inf.IThreadStateManager;
import com.arialyy.aria.core.loader.IInfoTask;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.loader.IThreadTaskBuilder;
import com.arialyy.aria.core.manager.ThreadTaskManager;
import com.arialyy.aria.core.processor.ILiveTsUrlConverter;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.core.task.ThreadTask;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.exception.AriaException;
import com.arialyy.aria.exception.AriaM3U8Exception;
import com.arialyy.aria.m3u8.BaseM3U8Loader;
import com.arialyy.aria.m3u8.IdGenerator;
import com.arialyy.aria.m3u8.M3U8InfoTask;
import com.arialyy.aria.m3u8.M3U8Listener;
import com.arialyy.aria.m3u8.M3U8TaskOption;
import com.arialyy.aria.m3u8.M3U8ThreadTaskAdapter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * M3U8?????????????????????
 */
final class M3U8LiveLoader extends BaseM3U8Loader {
  /**
   * ???????????????
   */
  private static int EXEC_MAX_NUM = 4;
  private Handler mStateHandler;
  private ArrayBlockingQueue<Long> mFlagQueue = new ArrayBlockingQueue<>(EXEC_MAX_NUM);
  private ReentrantLock LOCK = new ReentrantLock();
  private Condition mCondition = LOCK.newCondition();
  private LinkedBlockingQueue<ExtInfo> mPeerQueue = new LinkedBlockingQueue<>();
  private ExtInfo mCurExtInfo;
  private M3U8InfoTask mInfoTask;
  private ScheduledThreadPoolExecutor mTimer;
  private List<String> mPeerUrls = new ArrayList<>();

  M3U8LiveLoader(DTaskWrapper wrapper, M3U8Listener listener) {
    super(wrapper, listener);
    if (((M3U8TaskOption) wrapper.getM3u8Option()).isGenerateIndexFile()) {
      ALog.i(TAG, "???????????????????????????????????????????????????????????????????????????????????????");
      EXEC_MAX_NUM = 1;
    }
  }

  ExtInfo getCurExtInfo() {
    return mCurExtInfo;
  }

  private void offerPeer(ExtInfo extInfo) {
    mPeerQueue.offer(extInfo);
  }

  @Override protected void handleTask(Looper looper) {
    if (isBreak()) {
      return;
    }

    // ????????????
    getRecordHandler().setOption(mM3U8Option);
    mRecord = getRecordHandler().getRecord(0);

    // ????????????????????????
    getStateManager().setLooper(mRecord, looper);
    getStateManager().setLoader(this);
    mStateHandler = new Handler(looper, getStateManager().getHandlerCallback());

    // ??????????????????????????????
    startLoaderLiveInfo();

    // ???????????????
    startTimer();

    new Thread(new Runnable() {
      @Override public void run() {
        String cacheDir = getCacheDir();
        int index = 0;
        while (!isBreak()) {
          try {
            LOCK.lock();
            while (mFlagQueue.size() < EXEC_MAX_NUM) {
              ExtInfo extInfo = mPeerQueue.poll();
              if (extInfo == null) {
                break;
              }
              mCurExtInfo = extInfo;
              ThreadTask task = createThreadTask(cacheDir, index, extInfo.url);
              getTaskList().add(task);
              mFlagQueue.offer(startThreadTask(task, task.getConfig().peerIndex));
              index++;
            }
            if (mFlagQueue.size() > 0) {
              mCondition.await();
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          } finally {
            LOCK.unlock();
          }
        }
      }
    }).start();
  }

  @Override protected LiveStateManager getStateManager() {
    return (LiveStateManager) super.getStateManager();
  }

  private LiveRecordHandler getRecordHandler() {
    return (LiveRecordHandler) mRecordHandler;
  }

  @Override public long getFileSize() {
    return mTempFile.length();
  }

  void notifyLock(boolean success, int peerId) {
    try {
      LOCK.lock();
      long id = mFlagQueue.take();
      if (success) {
        ALog.d(TAG, String.format("?????????%s???????????????", peerId));
      } else {
        ALog.e(TAG, String.format("?????????%s???????????????", peerId));
      }
      mCondition.signalAll();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      LOCK.unlock();
    }
  }

  /**
   * ??????????????????
   *
   * @return ????????????id??????
   */
  private long startThreadTask(ThreadTask task, int indexId) {
    ThreadTaskManager.getInstance().startThread(mTaskWrapper.getKey(), task);
    ((M3U8Listener) getListener()).onPeerStart(mTaskWrapper.getKey(),
        task.getConfig().tempFile.getPath(),
        indexId);
    return IdGenerator.getInstance().nextId();
  }

  /**
   * ??????config
   */
  private ThreadTask createThreadTask(String cacheDir, int indexId, String tsUrl) {
    ThreadRecord tr = getRecordHandler().createThreadRecord(mRecord, tsUrl, indexId);

    SubThreadConfig config = new SubThreadConfig();
    config.url = tsUrl;
    config.tempFile = new File(getTsFilePath(cacheDir, indexId));
    config.isBlock = mRecord.isBlock;
    config.taskWrapper = mTaskWrapper;
    config.record = tr;
    config.stateHandler = mStateHandler;
    config.peerIndex = indexId;
    config.threadType = SubThreadConfig.getThreadType(ITaskWrapper.M3U8_LIVE);
    config.updateInterval = SubThreadConfig.getUpdateInterval(ITaskWrapper.M3U8_LIVE);
    config.ignoreFailure = mM3U8Option.isIgnoreFailureTs();
    if (!config.tempFile.exists()) {
      FileUtil.createFile(config.tempFile);
    }
    ThreadTask threadTask = new ThreadTask(config);
    M3U8ThreadTaskAdapter adapter = new M3U8ThreadTaskAdapter(config);
    threadTask.setAdapter(adapter);
    return threadTask;
  }

  /**
   * ????????????
   *
   * @return {@code true} ???????????????{@code false}????????????
   */
  private boolean mergeFile() {
    ITsMergeHandler mergeHandler = mM3U8Option.getMergeHandler();
    String cacheDir = getCacheDir();
    List<String> partPath = new ArrayList<>();
    String[] tsNames = new File(cacheDir).list(new FilenameFilter() {
      @Override public boolean accept(File dir, String name) {
        return name.endsWith(".ts");
      }
    });
    for (String tsName : tsNames) {
      partPath.add(cacheDir + "/" + tsName);
    }

    boolean isSuccess;
    if (mergeHandler != null) {
      isSuccess = mergeHandler.merge(getEntity().getM3U8Entity(), partPath);
    } else {
      isSuccess = FileUtil.mergeFile(getEntity().getFilePath(), partPath);
    }
    if (isSuccess) {
      // ?????????????????????????????????
      for (String pp : partPath) {
        FileUtil.deleteFile(pp);
      }
      File cDir = new File(cacheDir);
      FileUtil.deleteDir(cDir);
      return true;
    } else {
      ALog.e(TAG, "????????????");
      return false;
    }
  }

  @Override public void addComponent(IRecordHandler recordHandler) {
    mRecordHandler = recordHandler;
  }

  @Override public void addComponent(IInfoTask infoTask) {
    mInfoTask = (M3U8InfoTask) infoTask;
    mInfoTask.setCallback(new IInfoTask.Callback() {
      @Override public void onSucceed(String key, CompleteInfo info) {
        ALog.d(TAG, "???????????????m3u8??????");
      }

      @Override public void onFail(AbsEntity entity, AriaException e, boolean needRetry) {

      }
    });

    mInfoTask.setOnGetPeerCallback(new M3U8InfoTask.OnGetLivePeerCallback() {
      @Override public void onGetPeer(String url, String extInf) {
        if (mPeerUrls.contains(url)) {
          return;
        }
        mPeerUrls.add(url);
        ILiveTsUrlConverter converter = mM3U8Option.isUseDefConvert() ?
            new LiveTsDefConverter() :
            mM3U8Option.getLiveTsUrlConverter();
        if (converter != null) {
          if (TextUtils.isEmpty(mM3U8Option.getBandWidthUrl())) {
            url = converter.convert(getEntity().getUrl(), url);
          } else {
            url = converter.convert(mM3U8Option.getBandWidthUrl(), url);
          }
        }
        if (TextUtils.isEmpty(url) || !url.startsWith("http")) {
          fail(new AriaM3U8Exception(String.format("ts???????????????url???%s", url)), false);
          return;
        }
        offerPeer(new M3U8LiveLoader.ExtInfo(url, extInf));
      }
    });
  }

  private void fail(AriaM3U8Exception e, boolean needRetry) {
    getListener().onFail(needRetry, e);
    handleComplete();
  }

  private void handleComplete() {
    if (mInfoTask != null) {
      mInfoTask.setStop(true);
      closeInfoTimer();
      if (mM3U8Option.isGenerateIndexFile()) {
        if (generateIndexFile(true)) {
          getListener().onComplete();
        } else {
          getListener().onFail(false, new AriaM3U8Exception("????????????????????????"));
        }
      } else if (mM3U8Option.isMergeFile()) {
        if (mergeFile()) {
          getListener().onComplete();
        } else {
          getListener().onFail(false, new AriaM3U8Exception("??????????????????"));
        }
      } else {
        getListener().onComplete();
      }
    }
  }

  /**
   * ??????????????????m3u8??????
   */
  private void startLoaderLiveInfo() {
    mTimer = new ScheduledThreadPoolExecutor(1);
    mTimer.scheduleWithFixedDelay(new Runnable() {
      @Override public void run() {
        mInfoTask.run();
      }
    }, 0, mM3U8Option.getLiveUpdateInterval(), TimeUnit.MILLISECONDS);
  }

  private void closeInfoTimer() {
    if (mTimer != null && !mTimer.isShutdown()) {
      mTimer.shutdown();
    }
  }

  /**
   * ?????????{@link #addComponent(IRecordHandler)} ?????????
   */
  @Override public void addComponent(IThreadStateManager threadState) {
    mStateManager = threadState;
  }

  /**
   * @deprecated m3u8 ?????????????????????
   */
  @Deprecated
  @Override public void addComponent(IThreadTaskBuilder builder) {

  }

  @Override protected void checkComponent() {
    if (mRecordHandler == null) {
      throw new NullPointerException("????????????????????????");
    }
    if (mInfoTask == null) {
      throw new NullPointerException(("????????????????????????"));
    }
    if (mStateManager == null) {
      throw new NullPointerException("??????????????????????????????");
    }
  }

  static class ExtInfo {
    String url;
    String extInf;

    ExtInfo(String url, String extInf) {
      this.url = url;
      this.extInf = extInf;
    }
  }
}
