package com.arialyy.aria.http.download;

import com.arialyy.aria.core.TaskRecord;
import com.arialyy.aria.core.common.SubThreadConfig;
import com.arialyy.aria.core.loader.AbsNormalTTBuilderAdapter;
import com.arialyy.aria.core.loader.IRecordHandler;
import com.arialyy.aria.core.task.IThreadTaskAdapter;
import com.arialyy.aria.util.ALog;
import com.arialyy.aria.util.BufferedRandomAccessFile;
import com.arialyy.aria.util.FileUtil;
import java.io.File;
import java.io.IOException;

final class HttpDTTBuilderAdapter extends AbsNormalTTBuilderAdapter {

  @Override public IThreadTaskAdapter getAdapter(SubThreadConfig config) {
    return new HttpDThreadTaskAdapter(config);
  }

  @Override public boolean handleNewTask(TaskRecord record, int totalThreadNum) {
    if (!record.isBlock) {
      if (getTempFile().exists()) {
        FileUtil.deleteFile(getTempFile());
      }
    } else {
      for (int i = 0; i < totalThreadNum; i++) {
        File blockFile =
            new File(String.format(IRecordHandler.SUB_PATH, getTempFile().getPath(), i));
        if (blockFile.exists()) {
          ALog.d(TAG, String.format("The block [%s] already exists, the block will be deleted", i));
          FileUtil.deleteFile(blockFile);
        }
      }
    }
    BufferedRandomAccessFile file = null;
    try {
      if (totalThreadNum > 1 && !record.isBlock) {
        file = new BufferedRandomAccessFile(getTempFile().getPath(), "rwd", 8192);
        //设置文件长度
        file.setLength(getEntity().getFileSize());
      }
      if (getTempFile().exists()) {
        FileUtil.deleteFile(getTempFile());
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      ALog.e(TAG, String.format("download failed，filePath: %s, url: %s", getEntity().getFilePath(),
          getEntity().getUrl()));
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return false;
  }
}