package com.example.instapic.model

import android.os.Parcel
import android.os.Parcelable

// Data class to represent a post in the Instagram-like app
data class PostModel(
    var postId: String = "",
    var userId: String = "",
    var imageUrl: String = "",
    var caption: String = "",
    var timestamp: Long = 0L,
    var likesCount: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(postId)
        parcel.writeString(userId)
        parcel.writeString(imageUrl)
        parcel.writeString(caption)
        parcel.writeLong(timestamp)
        parcel.writeInt(likesCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostModel> {
        override fun createFromParcel(parcel: Parcel): PostModel {
            return PostModel(parcel)
        }

        override fun newArray(size: Int): Array<PostModel?> {
            return arrayOfNulls(size)
        }
    }
}
