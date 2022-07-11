package com.phaytran.android_multi_download

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.listener.ISchedulers
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.util.CommonUtil
import com.phaytran.android_multi_download.App.Companion.CHANNEL_ID
import java.security.AccessController.getContext


class DownloadService : Service() {
    companion object {
        val CANCEL_DOWNLOAD: String = "CANCEL_DOWNLOAD"
        val PAUSE_DOWNLOAD: String = "PAUSE_DOWNLOAD"
        val RESUME_DOWNLOAD: String = "RESUME_DOWNLOAD"
    }

    var isCommandStart: Boolean = false
    var listFileInfo: ArrayList<FileInfo> = arrayListOf()
    override fun onCreate() {
        super.onCreate()
        Aria.download(this).register()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val url = intent.getStringExtra("url")
        val fileName = intent.getStringExtra("fileName")
        val path = intent.getStringExtra("path")
        val notiId = intent.getIntExtra("fileID", 0)
        listFileInfo = intent.extras!!.getParcelableArrayList("listTask")!!
        listFileInfo.forEach {
            Log.e("Task", "" + it.fileName)
        }
        Log.e("NotiID", "" + notiId)

        Aria.download(getContext())
            .load(url)
            .ignoreFilePathOccupy()
            .setFilePath("$path/Download/$fileName")
            .create()

        val notificationBuilder =
            startNotification(fileName, notiId, ISchedulers.PRE, 0, null, "", null)
        if (!isCommandStart) {
            startForeground(notiId, notificationBuilder.build())
            isCommandStart = true
            Log.e("DownloadService ===> ", "Start Foreground")
        }
        return START_NOT_STICKY
    }

    private fun getStringStatus(status: Int): String {
        when (status) {
            ISchedulers.PRE -> return getString(R.string.sts_pending)
            ISchedulers.START -> return getString(R.string.sts_start)
            ISchedulers.WAIT -> return getString(R.string.sts_wait)
            ISchedulers.RESUME -> return getString(R.string.sts_resume)
            ISchedulers.STOP -> return getString(R.string.sts_stop)
            ISchedulers.CANCEL -> return getString(R.string.sts_cancel)
            ISchedulers.FAIL -> return getString(R.string.sts_faild)
            ISchedulers.COMPLETE -> return getString(R.string.sts_complete)
            ISchedulers.RUNNING -> return getString(R.string.sts_running)

        }
        return "Exception"
    }

    private fun startNotification(
        fileName: String?,
        notificationID: Int,
        status: Int,
        progress: Int,
        speed: String?,
        currentDownloaded: String,
        taskId: Long?
    ): NotificationCompat.Builder {
        speed.let { if (it == null) "" }
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download) // notification icon
            .setContentTitle(fileName) // title for notification
            .setContentText(getStringStatus(status) + status.let {
                if (it == ISchedulers.COMPLETE || it == ISchedulers.FAIL) {
                    ""
                } else {
                    " | " + currentDownloaded
                }
            }) // mess
            .setProgress(100, progress, false)// age for notification
            .setSubText(speed)
            .setVibrate(longArrayOf(0L))
            .setAutoCancel(false) // clear notification after click
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, status.let {
                if (it == ISchedulers.STOP) {
                    getString(R.string.btn_resume)
                } else {
                    getString(R.string.btn_pause)
                }
            }, buildNotificationActionButton(
                status.let {
                    if (it == ISchedulers.STOP) {
                        RESUME_DOWNLOAD
                    } else {
                        PAUSE_DOWNLOAD
                    }
                }, taskId
            )
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.btn_cancel),
                buildNotificationActionButton(
                    CANCEL_DOWNLOAD, taskId
                )
            )

        val notifyIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            this,
            0,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        mBuilder.setContentIntent(pi)
        if (isCommandStart) {
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(notificationID, mBuilder.build())
        }
        return mBuilder
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "DownloadService",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskStart
    fun taskStart(task: DownloadTask) {
        Log.e("Task start::: ", "===> " + task.downloadEntity.fileName)
        updateNoti(task, ISchedulers.START, null)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onWait
    fun taskWait(task: DownloadTask) {
        Log.e("Task waiting ::: ", " ==> " + task.downloadEntity.fileName)
        updateNoti(task, ISchedulers.WAIT, null)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        Log.e("Task resume ::: ", " ==> " + task.downloadEntity.fileName)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        Log.e("Task stop ::: ", " ==> " + task.downloadEntity.fileName)
        updateNoti(task, ISchedulers.STOP, null)
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        Log.e("Task cancel ::: ", " ==> " + task.downloadEntity.fileName)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskFail
    fun taskFail(task: DownloadTask) {
        Log.e("Task failed ::: ", " ==> " + task.downloadEntity?.fileName)
        updateNoti(task, ISchedulers.FAIL, null)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        Log.e("Task completed ::: ", " ==> " + task.downloadEntity.fileName)
        Aria.download(getContext()).load(task.downloadEntity.id).removeRecord()
        updateNoti(task, ISchedulers.COMPLETE, null)

    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        Log.e("Task running ::: ", task.downloadEntity.fileName + " ===> " + task.downloadEntity.id)
        updateNoti(
            task,
            ISchedulers.RUNNING,
            CommonUtil.formatFileSize(task.downloadEntity.speed.toDouble()) + "/s"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("DownloadService ===> ", "On destroyed")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateNoti(task: DownloadTask, status: Int, speed: String?) {
        val size: Long = task.fileSize
        val progress: Long = task.currentProgress
        val current = if (size == 0L) 0 else (progress * 100 / size).toInt()
        val item = listFileInfo.find {
            it.url == task.downloadEntity.url
        }
        Log.e("NotificationCheck", "Object:: ${item?.percent} ::::: $current")
        if (item?.percent != current) {
            item?.percent = current
            val notificationBuilder = startNotification(
                item?.fileName,
                item?.rowId!!,
                status,
                current,
                speed,
                CommonUtil.formatFileSize(progress.toDouble()) + "/" + CommonUtil.formatFileSize(
                    size.toDouble()
                ),
                task.downloadEntity.id
            )
            if (status == ISchedulers.COMPLETE) {
                stopNotificationAction(notificationBuilder, false)
                if (Aria.download(getContext()).dRunningTask == null) {
                    stopForeground(Service.STOP_FOREGROUND_DETACH)
                    stopSelf()
                    Log.e("Phaydev::", "Stop service")
                }
            }
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(item.rowId!!, notificationBuilder.build())
        }
        if (status == ISchedulers.FAIL ||status==ISchedulers.STOP) {
            val notificationBuilder = startNotification(
                item.fileName,
                item.rowId!!,
                status,
                current,
                speed,
                CommonUtil.formatFileSize(progress.toDouble()) + "/" + CommonUtil.formatFileSize(
                    size.toDouble()
                ),
                task.downloadEntity.id
            )
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            stopNotificationAction(notificationBuilder, true)
            mNotificationManager.notify(item.rowId!!, notificationBuilder.build())
            if(status==ISchedulers.FAIL){
                if (Aria.download(getContext()).dRunningTask == null) {
                    stopForeground(Service.STOP_FOREGROUND_DETACH)
                    stopSelf()
                    Log.e("Phaydev::", "Stop service")
                }
            }
            Aria.download(getContext()).load(task.downloadEntity.id).cancel(true)
        }

    }

    private fun buildNotificationActionButton(action: String, taskId: Long?): PendingIntent {
        val receiver = Intent()
        receiver.action = action
        receiver.putExtra("taskID", taskId)
        return PendingIntent.getBroadcast(this, 2425, receiver, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun stopNotificationAction(
        notificationBuilder: NotificationCompat.Builder,
        isFailed: Boolean
    ) {
        notificationBuilder.setProgress(0, 0, false)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setOngoing(false)
        isFailed.let {
            if (it) {
                notificationBuilder.setSmallIcon(android.R.drawable.stat_notify_error)
            } else {
                notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
            }
        }
    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        Log.e("DownloadService ===> ", "On bind")
        return null
    }
}