package com.phaytran.android_multi_download

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.getBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.annotations.Download
import com.arialyy.annotations.DownloadGroup
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadGroupTask
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.aria.util.ALog
import com.arialyy.frame.util.FileUtil
import java.io.File

class MainActivity : AppCompatActivity() {
    var mData: ArrayList<FileInfo> = arrayListOf(
        FileInfo("file1.mp4","link1","path1"),
        FileInfo("file2.mp4","link2","path2"),
        FileInfo("file3.mp4","link3","path3"),
        FileInfo("file4.mp4","link4","path4"),
        FileInfo("file5.mp4","link5","path5"),

    )
    private var mAdapter: ListAdapter? = null
    lateinit var mList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Aria.download(this).register()
        mAdapter = ListAdapter(this, mData)
        mList = findViewById(R.id.view_download)
        mList.layoutManager = LinearLayoutManager(this)
        mList.adapter = mAdapter

    }

}