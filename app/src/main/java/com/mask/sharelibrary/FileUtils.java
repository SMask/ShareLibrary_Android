package com.mask.sharelibrary;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;

/**
 * FileUtils
 * Created by lishilin on 2020/07/14
 */
public class FileUtils {

    /**
     * 获取 Authority(7.0Uri适配)
     *
     * @param context context
     * @return String Authority
     */
    public static String getAuthority(Context context) {
        return context.getPackageName() + ".FileProvider";
    }

    /**
     * 获取 FileUri
     *
     * @param context context
     * @param file    file
     * @return Uri
     */
    public static Uri getFileUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, getAuthority(context), file);
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * 获取 ContentUri
     *
     * @param context context
     * @param file    file(特定的文件才能查询到)
     * @return Uri
     */
    public static Uri getContentUri(Context context, File file) {
        Uri uri = null;
        String path = file.getAbsolutePath();
        ContentResolver contentResolver = context.getContentResolver();
        Uri contentUri = MediaStore.Files.getContentUri("external");
        String[] projection = new String[]{MediaStore.MediaColumns._ID};
        String selection = MediaStore.MediaColumns.DATA + "=? ";
        String[] selectionArgs = new String[]{path};
        Cursor cursor = contentResolver.query(contentUri, projection, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                if (idCol >= 0) {
                    uri = ContentUris.withAppendedId(contentUri, cursor.getLong(idCol));
                }
            }
            cursor.close();
        }
        return uri;
        //        if (mimeType.startsWith("image")) {
//            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        } else if (mimeType.startsWith("video")) {
//            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        } else if (mimeType.startsWith("audio")) {
//            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        } else {
//            contentUri = MediaStore.Files.getContentUri("external");
//        }
    }

    /**
     * 获取 Path(只作为Debug使用，实际开发不建议使用，毕竟获取path没有什么意义，用Uri来操作更好)
     *
     * @param context context
     * @param uri     uri(特定的Uri才能查询到，比如系统媒体库的Uri)
     * @return String Path
     */
    public static String getPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String path = null;
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[]{MediaStore.MediaColumns.DATA};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int pathCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (pathCol >= 0) {
                    path = cursor.getString(pathCol);
                }
            }
            cursor.close();
        }
        return path;
    }

    /**
     * 获取 Name
     *
     * @param context context
     * @param uri     uri
     * @return String Name
     */
    public static String getName(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
        if (documentFile != null) {
            return documentFile.getName();
        }
        return null;

//        String name = null;
//        ContentResolver contentResolver = context.getContentResolver();
//        String[] projection = new String[]{MediaStore.MediaColumns.DISPLAY_NAME};
//        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                int nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
//                if (nameCol >= 0) {
//                    name = cursor.getString(nameCol);
//                }
//            }
//            cursor.close();
//        }
//        return name;
    }

    /**
     * 获取 文件扩展名
     *
     * @param name name
     * @return String Extension
     */
    public static String getExtension(String name) {
        int index = name.lastIndexOf(".");
        if (index > 0) {
            return name.substring(index + 1);
        }
        return "";
    }

    /**
     * 获取 文件MimeType
     *
     * @param name name
     * @return String MimeType
     */
    public static String getMimeType(String name) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(name));
    }

    /**
     * 获取 文件MimeType
     *
     * @param context context
     * @param uri     uri
     * @return String MimeType
     */
    public static String getMimeType(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

//        DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
//        if (documentFile != null) {
//            return documentFile.getType();
//        }
//        return null;

        return context.getContentResolver().getType(uri);
    }

}
