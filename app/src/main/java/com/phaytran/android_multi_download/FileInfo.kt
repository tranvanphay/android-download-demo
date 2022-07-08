package com.phaytran.android_multi_download

import android.os.Parcel
import android.os.Parcelable

data class FileInfo(
    var taskID:Int? = Int.MIN_VALUE,
    var fileName: String?, var url:String?,
    var storagePath:String?, var percent: Int, var totalSize:Long,
    var downloaded:Long, var isCompleted:Boolean): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0?.writeValue(taskID)
        p0?.writeString(fileName)
        p0?.writeString(url)
        p0?.writeString(storagePath)
        p0?.writeInt(percent)
        p0?.writeLong(totalSize)
        p0?.writeLong(downloaded)
        p0?.writeByte(if (isCompleted) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<FileInfo> {
        override fun createFromParcel(parcel: Parcel): FileInfo {
            return FileInfo(parcel)
        }

        override fun newArray(size: Int): Array<FileInfo?> {
            return arrayOfNulls(size)
        }
    }
}