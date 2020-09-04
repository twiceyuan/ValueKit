package com.twiceyuan.valuekit

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import java.lang.UnsupportedOperationException

class Initializer : ContentProvider() {

    companion object {
        internal lateinit var context: Context
    }

    override fun onCreate(): Boolean {
        val ctx = context ?: return true
        Initializer.context = ctx
        return true
    }

    val notSupport = {
        throw UnsupportedOperationException("Not support in ${this::class.java.simpleName}")
    }

    override fun getType(p0: Uri): String? = notSupport()
    override fun insert(p0: Uri, p1: ContentValues?): Uri? = notSupport()
    override fun delete(p0: Uri, p1: String?, p2: Array<String>?): Int = notSupport()
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<String>?): Int =
            notSupport()

    override fun query(p0: Uri, p1: Array<String>?, p2: String?, p3: Array<String>?, p4: String?): Cursor? =
            notSupport()
}