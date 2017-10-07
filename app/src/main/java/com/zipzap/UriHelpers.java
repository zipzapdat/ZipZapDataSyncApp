package com.zipzap;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;

/**
 *  Utility functions to support Uri conversion and processing.
 */
public final class UriHelpers {

    private UriHelpers() {}

    /**
     * Get the file path for a uri. This is a convoluted way to get the path for an Uri created using the
     * StorageAccessFramework. This in no way is the official way to do this but there does not seem to be a better
     * way to do this at this point. It is taken from https://github.com/iPaulPro/aFileChooser.
     *
     * @param context The context of the application
     * @param uri The uri of the saved file
     * @return The file with path pointing to the saved file. It can return null if we can't resolve the uri properly.
     */
    public static File getFileForUri(final Context context, final Uri uri) {
        String path = null;
        // DocumentProvider
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            // MediaStore (and general)
            path = getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // File
            path = uri.getPath();
        }

        if (path != null) {
            return new File(path);
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
