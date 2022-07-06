package com.phaytran.android_multi_download

data class FileInfo (var taskID:Int? = Int.MIN_VALUE,var fileName: String, var url:String,var storagePath:String, var percent: Int, var totalSize:Long,var downloaded:Long, var isCompleted:Boolean){
}