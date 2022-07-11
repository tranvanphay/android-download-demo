package com.phaytran.android_multi_download

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arialyy.annotations.Download
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.task.DownloadTask

class MainActivity : AppCompatActivity() {
    private val mData: ArrayList<FileInfo> = arrayListOf()
    private var mAdapter: ListAdapter? = null
    lateinit var mList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Aria.download(this).register()
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_link)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        registerDialogView(dialog)
        setList()
        permission()

    }



    fun registerDialogView(dialog:Dialog){
        val editText = dialog.findViewById<EditText>(R.id.link)
        val btnOk = dialog.findViewById<Button>(R.id.ok)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAddLink)
        btnOk.setOnClickListener { dialog.dismiss() }
        btnAdd.setOnClickListener {
            if(editText.text.isNotEmpty()){
                val text = editText.text
                mData.add(FileInfo(mData.size+1,"file"+mData.size+text.substring(text.length-4),text.toString(),"",0,0,0,false))
                mAdapter?.notifyDataSetChanged()
                editText.text.clear()
            }
        }
    }

    fun permission(){
        val permissions = arrayOf(
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.CAMERA"
        )
        val requestCode = 200
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
        requestPermission()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1
            )
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
    }

    fun setList(){
        mAdapter = ListAdapter(this, mData)
        mList = findViewById(R.id.view_download)
        mList.layoutManager = LinearLayoutManager(this)
        mList.adapter = mAdapter
    }

    @Download.onTaskStart fun taskStart (task: DownloadTask){
        Log.e("Task start main::: ","===> " + task.downloadEntity.fileName)
    }

    @Download.onWait
    fun taskWait(task: DownloadTask) {
        Log.e("Task waiting main::: ", " ==> " + task.downloadEntity.fileName)
    }

    @Download.onTaskResume
    fun taskResume(task: DownloadTask) {
        Log.e("Task resume main::: ", " ==> " + task.downloadEntity.fileName)
    }

    @Download.onTaskStop
    fun taskStop(task: DownloadTask) {
        Log.e("Task stop main::: ", " ==> " + task.downloadEntity.fileName)
    }

    @Download.onTaskCancel
    fun taskCancel(task: DownloadTask) {
        Log.e("Task cancel main::: ", " ==> " + task.downloadEntity.fileName)
    }

    @Download.onTaskFail
    fun taskFail(task: DownloadTask?) {
        Log.e("Task failed main::: ", " ==> " + task?.downloadEntity?.fileName)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskComplete
    fun taskComplete(task: DownloadTask) {
        Log.e("Task complted main::: ", " ==> " + task.downloadEntity.fileName)
        mAdapter?.update(task)
        Aria.download(this).load(task.downloadEntity.id).removeRecord()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    @Download.onTaskRunning
    fun taskRunning(task: DownloadTask) {
        Log.e("Task running main::: ", task.downloadEntity.fileName+" ===> " + task.downloadEntity.currentProgress)
        mAdapter?.update(task)

    }

}