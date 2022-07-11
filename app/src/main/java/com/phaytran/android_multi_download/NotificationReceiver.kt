package com.phaytran.android_multi_download

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat.stopForeground
import com.arialyy.aria.core.Aria
import java.security.AccessController.getContext

class NotificationReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("NotificationReceiver:::","On Receiver")
        val action = intent.action
        val taskId:Long = intent.getLongExtra("taskID",0L)
        if(DownloadService.CANCEL_DOWNLOAD==action){
            Log.e("Phaydev::: ",""+taskId)
            Aria.download(getContext()).load(taskId).cancel(true)
//            shouldStopService(context)
        }else if(DownloadService.PAUSE_DOWNLOAD==action){
            Log.e("NotificationReceiver:::","Pause download")
            Aria.download(getContext()).load(taskId).stop()
//            Aria.download(getContext()).load(taskId).stop()
        }else if(DownloadService.RESUME_DOWNLOAD==action){
            Log.e("NotificationReceiver:::","Resume download")
                Aria.download(getContext()).load(taskId).resume(false)
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun shouldStopService(context: Context){
        if(Aria.download(getContext()).dRunningTask==null){
            context.stopService(Intent(context,DownloadService::class.java))
            Log.e("Phaydev::","Stop service")
        }
    }
}