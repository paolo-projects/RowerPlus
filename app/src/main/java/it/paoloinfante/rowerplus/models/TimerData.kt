package it.paoloinfante.rowerplus.models

import android.os.Parcel
import android.os.Parcelable

data class TimerData (
    var timeElapsed: Int,
    var calories: Float,
    var distance: Float,
    var rowsCount: Int,
    var currentRPM: Float,
    var currentSecsFor500M: Float
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(timeElapsed)
        parcel.writeFloat(calories)
        parcel.writeFloat(distance)
        parcel.writeInt(rowsCount)
        parcel.writeFloat(currentRPM)
        parcel.writeFloat(currentSecsFor500M)
    }

    companion object CREATOR : Parcelable.Creator<TimerData> {
        override fun createFromParcel(parcel: Parcel): TimerData {
            return TimerData(parcel)
        }

        override fun newArray(size: Int): Array<TimerData?> {
            return arrayOfNulls(size)
        }
    }
}