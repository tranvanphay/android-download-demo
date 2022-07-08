package com.phaytran.android_multi_download

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.util.CommonUtil
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.security.AccessController.getContext


class ListAdapter (private val context: Context, private val FileInfoList: ArrayList<FileInfo>) : RecyclerView.Adapter<ListAdapter.ViewHolder>(), View.OnClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = Environment.getExternalStorageDirectory().path
        Log.e("Path::: ",path)
        val FileInfo = FileInfoList[position]
        holder.tvFileInfoName.text = FileInfo.fileName
        holder.btnDownload.setOnClickListener {
//            Aria.download(getContext())
//                .load(FileInfo.url)
//                .ignoreFilePathOccupy()
//                .setFilePath("$path/Download/${FileInfo.fileName}")
//                .create()

            val url: String? = FileInfo.url
            val fileName: String? = FileInfo.fileName
            val fileId:Int? = FileInfo.taskID

            val serviceIntent = Intent(context, DownloadService::class.java)
            serviceIntent.putExtra("url", url)
            serviceIntent.putExtra("fileName", fileName)
            serviceIntent.putExtra("fileID", fileId)
            serviceIntent.putExtra("path", path)
            val bundle = Bundle()
            bundle.putParcelableArrayList("listTask", FileInfoList)
            serviceIntent.putExtras(bundle)
            ContextCompat.startForegroundService(context, serviceIntent)
        }

        holder.btnPause.setOnClickListener {
            val downloadingEntity = Aria.download(getContext()).load(FileInfo.url).entity
            if(Aria.download(getContext()).load(downloadingEntity.id).isRunning){
                Aria.download(getContext()).load(downloadingEntity.id).stop()
                holder.btnPause.text = "Resume"
            }else {
                Aria.download(getContext()).load(downloadingEntity.id).resume()
                holder.btnPause.text = "Pause"
            }
        }

        holder.process.progress = FileInfo.percent
        holder.processDownload.text = "${FileInfo.percent}%"

        if(FileInfo.downloaded==0L){
            holder.downloaded.text  = "Pending"
        }else if(FileInfo.isCompleted||FileInfo.downloaded==FileInfo.totalSize){
            holder.downloaded.text  = "Downloaded"
        }else {
            holder.downloaded.text  = covertCurrentSize(FileInfo.downloaded)+"/" + CommonUtil.formatFileSize(FileInfo.totalSize.toDouble())
        }
    }

    override fun getItemCount(): Int {
        return FileInfoList.size
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun update(task:DownloadTask) {
        val size: Long = task.fileSize
        val progress: Long = task.currentProgress
        val current = if (size == 0L) 0 else (progress * 100 / size).toInt()
        Log.e("TaskID","===> ${task.downloadEntity.id}}")

        val item =   FileInfoList.find {
            it.url == task.downloadEntity.url
        }
        item?.totalSize = size
        item?.percent = current
        item?.downloaded = progress
        item?.isCompleted = task.isComplete
        this.notifyDataSetChanged()

    }

    private fun covertCurrentSize(currentSize: Long): String? {
        return if (currentSize < 0) "0" else CommonUtil.formatFileSize(currentSize.toDouble())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFileInfoName: TextView = itemView.findViewById(R.id.fileName)
        var btnDownload: Button = itemView.findViewById(R.id.btnDownload)
        var btnPause: Button = itemView.findViewById(R.id.btnPause)
        var process: LinearProgressIndicator = itemView.findViewById(R.id.progress)
        var downloaded :TextView = itemView.findViewById(R.id.downloaded)
        var processDownload :TextView = itemView.findViewById(R.id.process)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btnDownload -> {

            }
        }
    }


}