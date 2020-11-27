package com.mehmetakiftutuncu.muezzin.repositories

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mehmetakiftutuncu.muezzin.database.Database

abstract class Repository {
    protected fun <R> read(ctx: Context, action: SQLiteDatabase.() -> R): R =
        Database(ctx).readableDatabase.use(action)

    protected fun <R> write(ctx: Context, action: SQLiteDatabase.() -> R): R =
        Database(ctx).writableDatabase.use(action)

    protected fun <R> list(ctx: Context,
                           sql: String,
                           construct: (Cursor) -> R?): List<R> {
        val items: MutableList<R> = mutableListOf()

        read(ctx) {
            rawQuery(sql, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast) {
                        construct(cursor)?.also { item ->
                            items.add(item)
                        }

                        cursor.moveToNext()
                    }
                }
            }
        }

        return items
    }

    protected fun <R> first(ctx: Context,
                            sql: String,
                            construct: (Cursor) -> R?): R? =
        read(ctx) {
            rawQuery(sql, null)?.use { cursor ->
                if (!cursor.moveToFirst()) null else construct(cursor)
            }
        }
}