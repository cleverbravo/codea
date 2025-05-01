package com.codea.domain.TerminalManager

import android.os.Parcel
import android.os.Parcelable

data class CommandInfo(val command: String) : Parcelable {
    private constructor(inParcel: Parcel) : this(command = inParcel.readString() ?: "") {
        exitCode = inParcel.readValue(kotlin.Int::class.java.classLoader) as? Int
        stdout = inParcel.readString()
        stderr = inParcel.readString()
        errorMessage = inParcel.readString()
        errorCode = inParcel.readValue(kotlin.Int::class.java.classLoader) as? Int
    }

    var state: CommandState = CommandState.CREATED
    var exitCode: Int? = null
    var stdout: String? = null
    var stderr: String? = null
    var errorMessage: String? = null
    var errorCode: Int? = null
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(command)
        dest.writeString(state.name)
        dest.writeValue(exitCode)
        dest.writeString(stdout)
        dest.writeString(stderr)
        dest.writeString(errorMessage)
        dest.writeValue(errorCode)
    }

    companion object CREATOR : Parcelable.Creator<CommandInfo> {
        override fun createFromParcel(parcel: Parcel): CommandInfo {
            return CommandInfo(parcel)
        }

        override fun newArray(size: Int): Array<CommandInfo?> {
            return arrayOfNulls(size)
        }
    }
}

enum class CommandState { CREATED, RUNNING, STOPPED, FINISHED }
