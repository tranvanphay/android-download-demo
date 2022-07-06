package com.phaytran.android_multi_download

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadTask
import com.arialyy.frame.util.FileUtil
import java.io.File

class MainActivity : AppCompatActivity() {
    private val mData: ArrayList<FileInfo> = arrayListOf(
        FileInfo(Int.MIN_VALUE,"FALLEN.mp4","http://download028.fshare.vn/dl/4bKsOcwlXC7MYWlr-b6ce0vREy3AWFUYSvQQbktUnLy+8wPs5X1KTnNbKdn0B6-oVa5aXzyu1wKQk+yO/%28Hard%20Sub%20Vi%E1%BB%87t%29%20-%20LONDON%20HAS%20FALLEN.mp4","path1",0,0,0,false),
        FileInfo(Int.MIN_VALUE,"file2.mkv","http://download005.fshare.vn/dl/Alr85hQwF6VKOUfYUywp5FC6m4gIhhPMJ2ZEnaSkAtpUJPAiolMzhnyYDrpr8qMaYX8-NQgF3Dex-lgh/12.Days.of.Christmas.2020.1080p.WEB-DL.DD5.1.H.264-EVO%5BEtHD%5D.mkv","path2",0,0,0,false),
        FileInfo(Int.MIN_VALUE,"file3.mkv","http://download034.fshare.vn/dl/0k2BmIOy7ZtYOCNd1kQnkY+ZmEKRXy3JC9KmNshSxvD+L3swpX+O25S0zktTy5ehoqpu2P9CJhRny2A4/12.Hour.Shift.2020.1080p.WEB-DL.DD5.1.H264-FGT.mkv","path3",0,0,0,false),
        FileInfo(Int.MIN_VALUE,"file4.mkv","http://download024.fshare.vn/dl/YHeQKV0iAJ6R5PCg20Kui02VvY4UiUmB3tWKLw3p-nt0XHKz+Gph4o7tNTJTkiXNPgqLYmWUqSwE2z8P/1917.2019.1080p.HDRip.DD2.1.x264-FoE.mkv","path4",0,0,0,false),
        FileInfo(Int.MIN_VALUE,"file5.mp4","http://download046.fshare.vn/dl/Rs24UoK2P8E2eq6DkRpqc1EC9r9+PnNNoTX0Scc82dsHYDD+d6o8RKO22fAq5wPUnkJZbzqaZKIYgOKJ/1996%20Police%20Story%20IV%20First%20Strike%20-%20Cau%20Chuyen%20Canh%20Sat%204%20%28Thanh%20Long%29%20%28SUBVIET%29.mp4","path5",0,0,0,false),

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

    @Download.onTaskStart fun taskStart (task: DownloadTask){
        Log.e("Task start::: ","===> " + task.downloadEntity.fileName)
    }

    @Download.onWait
    fun taskWait(task: DownloadTask) {
        Log.e("Task waiting ::: ", " ==> " + task.downloadEntity.fileName)
    }

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
        Log.e("Task complted ::: ", " ==> " + task.downloadEntity.fileName)
        mAdapter?.update(task)
        Aria.download(this).load(task.downloadEntity.id).removeRecord()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        Log.e("Task running ::: ", task.downloadEntity.fileName+" ===> " + task.downloadEntity.currentProgress)
        mAdapter?.update(task)

    }

}