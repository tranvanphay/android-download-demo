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
import com.arialyy.aria.core.task.DownloadTask
import com.phaytran.android_multi_download.App.Companion.CHANNEL_ID
import java.io.File
import java.security.AccessController.getContext


class DownloadService : Service() {

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
        val notiId = intent.getIntExtra("fileID",0)
        listFileInfo = intent.extras!!.getParcelableArrayList("listTask")!!
        listFileInfo.forEach {
            Log.e("Task",""+it.fileName)
        }
        Log.e("NotiID",""+notiId)

        Aria.download(getContext())
                .load(url)
                .ignoreFilePathOccupy()
                .setFilePath("$path/Download/$fileName")
                .create()

        val notificationBuilder = startNotification(fileName,notiId,"Pending...",0)
        if(!isCommandStart){
            startForeground(notiId, notificationBuilder.build())
            isCommandStart = true
            Log.e("DownloadService ===> ","Start Foreground")
        }
        return START_NOT_STICKY
    }

    private fun startNotification(fileName:String?,notificationID:Int,status:String,progress:Int):NotificationCompat.Builder{
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this,CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download) // notification icon
            .setContentTitle(fileName) // title for notification
            .setContentText(status) // mess
            .setProgress(100,progress,false)// age for notification
            .setAutoCancel(false) // clear notification after click
            .setOngoing(true)
        val notifyIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        mBuilder.setContentIntent(pi)
        if(isCommandStart){
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(notificationID, mBuilder.build())
        }
        return mBuilder;
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Example Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskStart fun taskStart (task: DownloadTask){
        Log.e("Task start::: ","===> " + task.downloadEntity.fileName)
        updateNoti(task,"Starting...",false)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onWait
    fun taskWait(task: DownloadTask) {
        Log.e("Task waiting ::: ", " ==> " + task.downloadEntity.fileName)
        updateNoti(task,"Waiting...",false)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        Log.e("Task resume ::: ", " ==> " + task.downloadEntity.fileName)

    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        Log.e("Task stop ::: ", " ==> " + task.downloadEntity.fileName)
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        Log.e("Task cancel ::: ", " ==> " + task.downloadEntity.fileName)
    }

    @Download.onTaskFail
    fun taskFail(task: DownloadTask?) {
        Log.e("Task failed ::: ", " ==> " + task?.downloadEntity?.fileName)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        Log.e("Task completed ::: ", " ==> " + task.downloadEntity.fileName)
        Aria.download(this).load(task.downloadEntity.id).removeRecord()
        updateNoti(task,"Completed...",true)

    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        Log.e("Task running ::: ", task.downloadEntity.fileName+" ===> " + task.downloadEntity.currentProgress)
        updateNoti(task,"Downloading...",false)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("DownloadService ===> ","On destroyed")
    }

    fun updateNoti(task:DownloadTask,status:String,isCompleted:Boolean) {
        val size: Long = task.fileSize
        val progress: Long = task.currentProgress
        val current = if (size == 0L) 0 else (progress * 100 / size).toInt()
        val item =   listFileInfo.find {
            it.url == task.downloadEntity.url
        }
        Log.e("NotificationCheck","Object:: ${item?.percent} ::::: $current")
        if(item?.percent!=current){
            item?.percent = current;
            val notificationBuilder = startNotification(item?.fileName,item?.taskID!!,status,current)
            if(isCompleted){
                notificationBuilder.setAutoCancel(true)
                notificationBuilder.setOngoing(false)
                notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
            }
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(item.taskID!!, notificationBuilder.build())
        }



    }

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        Log.e("DownloadService ===> ","On bind")
        return null
    }
}