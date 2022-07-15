package com.phaytran.android_multi_download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.arialyy.aria.core.Aria
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.AccessController.getContext
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future


class NotificationReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId:Long = intent.getLongExtra("taskID",0L)
        Log.e("NotificationReceiver:::","On Receiver $taskId")

        if(DownloadService.CANCEL_DOWNLOAD==action){
            Log.e("Phaydev::: ",""+taskId)
            Aria.download(getContext()).load(taskId).cancel(true)
        }else if(DownloadService.PAUSE_DOWNLOAD==action){
            Log.e("NotificationReceiver:::","Pause download $taskId")
            Aria.download(getContext()).load(taskId).stop()
        }else if(DownloadService.RESUME_DOWNLOAD==action){
            Log.e("NotificationReceiver:::","Resume download $taskId")
                Aria.download(getContext()).load(taskId).resume()
        }

        Log.e("IP:::",getPublicIPAddress()+"")
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun shouldStopService(context: Context){
        if(Aria.download(getContext()).dRunningTask==null){
            context.stopService(Intent(context,DownloadService::class.java))
            Log.e("Phaydev::","Stop service")
        }
    }

    fun getPublicIPAddress(): String? {
        var value: String? = null
        val es: ExecutorService = Executors.newSingleThreadExecutor()
        val result: Future<String?> = es.submit(object : Callable<String?> {
            @Throws(java.lang.Exception::class)
            override fun call(): String? {
                try {
                    val url = URL("http://whatismyip.akamai.com/")
                    val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    return try {
                        val `in`: InputStream = BufferedInputStream(urlConnection.getInputStream())
                        val r = BufferedReader(InputStreamReader(`in`))
                        val total = StringBuilder()
                        var line: String?
                        while (r.readLine().also { line = it } != null) {
                            total.append(line).append('\n')
                        }
                        urlConnection.disconnect()
                        total.toString()
                    } finally {
                        urlConnection.disconnect()
                    }
                } catch (e: IOException) {
                    Log.e("Public IP: ", e.message+"")
                }
                return null
            }
        })
        try {
            value = result.get()
        } catch (e: java.lang.Exception) {
            // failed
        }
        es.shutdown()
        return value
    }
}