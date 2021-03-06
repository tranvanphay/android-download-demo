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

package com.arialyy.aria.core.command;

import com.arialyy.aria.core.download.DGTaskWrapper;
import com.arialyy.aria.core.download.DTaskWrapper;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.DownloadGroupEntity;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.manager.TaskWrapperManager;
import com.arialyy.aria.core.queue.DGroupTaskQueue;
import com.arialyy.aria.core.queue.DTaskQueue;
import com.arialyy.aria.core.queue.UTaskQueue;
import com.arialyy.aria.core.upload.UTaskWrapper;
import com.arialyy.aria.core.upload.UploadEntity;
import com.arialyy.aria.orm.DbEntity;
import com.arialyy.aria.util.ALog;
import java.util.List;

/**
 * Created by AriaL on 2017/6/27.
 * delete all tasks and delete all
 */
final public class CancelAllCmd<T extends AbsTaskWrapper> extends AbsNormalCmd<T> {
  /**
   * removeFile {@code true} Delete the tasks that have been downloaded, not only delete the download record, but also delete the files that have been downloaded，{@code false}
   * If the file has already been downloaded, only delete the download record
   */
  public boolean removeFile = false;

  CancelAllCmd(T entity, int taskType) {
    super(entity, taskType);
  }

  @Override public void executeCmd() {
    if (!canExeCmd) return;
    if (isDownloadCmd) {
      removeAllDTask();
      removeAllDGTask();
    } else {
      removeUTask();
    }
  }

  /**
   * Remove all normal download tasks
   */
  private void removeAllDTask() {
    List<DownloadEntity> entities =
        DbEntity.findDatas(DownloadEntity.class, "isGroupChild=?", "false");
    if (entities != null && !entities.isEmpty()) {
      for (DownloadEntity entity : entities) {
        remove(TaskWrapperManager.getInstance()
            .getNormalTaskWrapper(DTaskWrapper.class, entity.getId()));
      }
    }
  }

  /**
   * Delete all download task group tasks
   */
  private void removeAllDGTask() {
    List<DownloadGroupEntity> entities =
        DbEntity.findDatas(DownloadGroupEntity.class, "state!=?", "-1");
    if (entities != null && !entities.isEmpty()) {
      for (DownloadGroupEntity entity : entities) {
        remove(TaskWrapperManager.getInstance()
            .getGroupWrapper(DGTaskWrapper.class, entity.getId()));
      }
    }
  }

  /**
   * Delete all normal upload tasks
   */
  private void removeUTask() {
    List<UploadEntity> entities =
        DbEntity.findDatas(UploadEntity.class, "isGroupChild=?", "false");
    if (entities != null && !entities.isEmpty()) {
      for (UploadEntity entity : entities) {
        remove(TaskWrapperManager.getInstance()
            .getNormalTaskWrapper(UTaskWrapper.class, entity.getId()));
      }
    }
  }

  private void remove(AbsTaskWrapper te) {
    if (te == null) {
      ALog.w(TAG, "Cancel task failed, task is empty");
      return;
    }
    if (te instanceof DTaskWrapper) {
      mQueue = DTaskQueue.getInstance();
    } else if (te instanceof UTaskWrapper) {
      mQueue = UTaskQueue.getInstance();
    } else if (te instanceof DGTaskWrapper) {
      mQueue = DGroupTaskQueue.getInstance();
    }
    te.setRemoveFile(removeFile);
    removeTask(te);
  }
}
