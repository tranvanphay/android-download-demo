package com.phaytran.android_multi_download

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.aria.core.Aria
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.security.AccessController.getContext


class ListAdapter (private val context: Context, private val FileInfoList: ArrayList<FileInfo>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val FileInfo = FileInfoList[position]
        holder.tvFileInfoName.text = FileInfo.fileName
        holder.btnDownload.setOnClickListener {
            Aria.download(getContext())
                .load(FileInfo.url)
                .setFilePath("storage/emulated/0")
                .create()
        }

    }

    override fun getItemCount(): Int {
        return FileInfoList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvFileInfoName: TextView = itemView.findViewById(R.id.fileName)
        var btnDownload: Button = itemView.findViewById(R.id.btnDownload)
        var btnPause: Button = itemView.findViewById(R.id.btnPause)
        var process: LinearProgressIndicator = itemView.findViewById(R.id.progress)
    }



}