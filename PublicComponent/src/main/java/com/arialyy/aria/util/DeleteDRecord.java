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
package com.arialyy.aria.util;

import android.text.TextUtils;
import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.ThreadRecord;
import com.arialyy.aria.core.common.AbsEntity;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import com.arialyy.aria.orm.DbEntity;
import java.io.File;

/**
 * 删除下载记录
 */
public class DeleteDRecord implements IDeleteRecord {
  private String TAG = CommonUtil.getClassName(this);
  private static volatile DeleteDRecord INSTANCE = null;

  private DeleteDRecord() {

  }

  public static DeleteDRecord getInstance() {
    if (INSTANCE == null) {
      synchronized (DeleteDRecord.class) {
        if (INSTANCE == null) {
          INSTANCE = new DeleteDRecord();
        }
      }
    }
    return INSTANCE;
  }

  /**
   * @param filePath 文件保存路径
   * @param removeTarget true，无论下载成功以否，都将删除下载下来的文件。false，只会删除下载任务没有完成的文件
   * @param needRemoveEntity 是否需要删除实体
   */
  @Override public void deleteRecord(String filePath, boolean removeTarget,
      boolean needRemoveEntity) {
    if (TextUtils.isEmpty(filePath)) {
      throw new NullPointerException("Failed to delete record, file path is empty");
    }
    if (!filePath.startsWith("/")) {
      throw new IllegalArgumentException(String.format("file path error，filePath：%s", filePath));
    }
    DownloadEntity entity = DbEntity.findFirst(DownloadEntity.class, "downloadPath=?", filePath);
    if (entity == null) {
      ALog.e(TAG, "Failed to delete the download record, the corresponding entity file was not found in the database，filePath：" + filePath);
      return;
    }
    deleteRecord(entity, removeTarget, needRemoveEntity);
  }

  /**
   * @param absEntity record associated entity
   * @param needRemoveFile true，Whether the download is successful or not, the downloaded files will be deleted. false, only the files that the download task has not completed will be deleted
   * @param needRemoveEntity Whether the entity needs to be deleted
   */
  @Override
  public void deleteRecord(AbsEntity absEntity, boolean needRemoveFile, boolean needRemoveEntity) {
    if (absEntity == null) {
      ALog.e(TAG, "Failed to delete download record, entity is empty");
      return;
    }
    DownloadEntity entity = (DownloadEntity) absEntity;
    final String filePath = entity.getFilePath();
    File targetFile = new File(filePath);

    // Compatible with previous versions
    if (entity.getTaskType() == ITaskWrapper.M3U8_VOD
        || entity.getTaskType() == ITaskWrapper.M3U8_LIVE) {
      DeleteM3u8Record.getInstance().deleteRecord(entity, needRemoveFile, needRemoveEntity);
      return;
    }

    TaskRecord record = DbDataHelper.getTaskRecord(entity.getFilePath(), entity.getTaskType());
    if (record == null) {
      ALog.e(TAG, "Failed to delete the download record, the record is empty, the entity record will be deleted，filePath：" + entity.getFilePath());
      FileUtil.deleteFile(targetFile);
      deleteEntity(needRemoveEntity, filePath);
      return;
    }

    // Delete downloaded thread records and task records
    DbEntity.deleteData(ThreadRecord.class, "taskKey=? AND threadType=?", filePath,
        String.valueOf(entity.getTaskType()));
    DbEntity.deleteData(TaskRecord.class, "filePath=? AND taskType=?", filePath,
        String.valueOf(entity.getTaskType()));

    if (needRemoveFile || !entity.isComplete()) {
      FileUtil.deleteFile(targetFile);
      if (record.isBlock) {
        removeBlockFile(record);
      }
    }

    deleteEntity(needRemoveEntity, filePath);
  }

  private void deleteEntity(boolean needRemoveEntity, String filePath){
    if (needRemoveEntity) {
      DbEntity.deleteData(DownloadEntity.class, "downloadPath=?", filePath);
    }
  }

  /**
   * Delete chunked files for multithreaded chunked downloads
   */
  private void removeBlockFile(TaskRecord record) {
    for (int i = 0, len = record.threadNum; i < len; i++) {
      FileUtil.deleteFile(String.format(IRecordHandler.SUB_PATH, record.filePath, i));
    }
  }
}
