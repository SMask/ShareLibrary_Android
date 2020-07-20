package com.mask.sharelibrary;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        if (file == null) {
            return null;
        }

        Uri uri = null;
        String path = file.getAbsolutePath();
        String mimeType = getMimeType(file.getName());

        ContentResolver contentResolver = context.getContentResolver();
        Uri contentUri = getContentUri(mimeType);
        String[] projection = new String[]{MediaStore.MediaColumns._ID};
        String selection = MediaStore.MediaColumns.DATA + "=?";
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
    }

    /**
     * 获取 重复文件Uri
     *
     * @param context context
     * @param file    file
     * @return Uri
     */
    public static Uri getDuplicateFileUri(Context context, File file) {
        if (file == null) {
            return null;
        }

        Uri uri = null;
        String name = file.getName();
        long size = file.length();
        String mimeType = getMimeType(name);

        ContentResolver contentResolver = context.getContentResolver();
        Uri contentUri = getContentUri(mimeType);
        String[] projection = new String[]{MediaStore.MediaColumns._ID};
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=?" + " AND " + MediaStore.MediaColumns.SIZE + "=?";
        String[] selectionArgs = new String[]{name, String.valueOf(size)};
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

    /**
     * 获取 Uri(根据mimeType)
     *
     * @param mimeType mimeType
     * @return Uri
     */
    public static Uri getContentUri(String mimeType) {
        Uri contentUri;
        if (mimeType.startsWith("image")) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("video")) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("audio")) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        return contentUri;
    }

    /**
     * 获取 文件夹名称(根据mimeType)
     *
     * @param mimeType mimeType
     * @return String dirName
     */
    public static String getDirName(String mimeType) {
        String dirName;
        if (mimeType.startsWith("image")) {
            dirName = Environment.DIRECTORY_PICTURES;
        } else if (mimeType.startsWith("video")) {
            dirName = Environment.DIRECTORY_PICTURES;
        } else if (mimeType.startsWith("audio")) {
            dirName = Environment.DIRECTORY_MUSIC;
        } else {
            dirName = Environment.DIRECTORY_DOCUMENTS;
        }
        return dirName;
    }

    /**
     * 复制文件到外部
     * 详细查看官方文档
     * 访问共享存储空间中的媒体文件：https://developer.android.com/training/data-storage/shared/media#add-item
     *
     * @param context context
     * @param dirName 目录名(例如："/Pictures/WeChat"中的"WeChat")
     * @param file    file
     * @return Uri
     */
    public static Uri copyFileToExternal(Context context, String dirName, File file) {
        if (file == null) {
            return null;
        }

        // 获取是否有重复的文件，避免重复复制
        Uri uri = getDuplicateFileUri(context, file);
        if (uri != null) {
            return uri;
        }

        String name = file.getName();
        String mimeType = getMimeType(name);

        ContentResolver contentResolver = context.getContentResolver();
        Uri contentUri = getContentUri(mimeType);

        // 插入参数
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);// 文件名
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);// mimeType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String dirPath = getDirName(mimeType);
            if (!TextUtils.isEmpty(dirName)) {
                dirPath += File.separatorChar + dirName;
            }
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, dirPath);// 相对路径
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);// 文件的处理状态(防止写入过程中被其他App查询到，写入完成后记得修改回来)
        }

        // 获取插入的Uri
        uri = contentResolver.insert(contentUri, values);
        if (uri == null) {
            return null;
        }

        // 复制文件
        boolean copySuccess = false;
        try {
            OutputStream outputStream = contentResolver.openOutputStream(uri);
            if (outputStream == null) {
                return null;
            }
            InputStream inputStream = new FileInputStream(file);
            copySuccess = copy(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 复制失败则删除
        if (!copySuccess) {
            contentResolver.delete(uri, null, null);
            return null;
        }

        // 更新文件的处理状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);// 文件的处理状态(防止写入过程中被其他App查询到，写入完成后记得修改回来)

            contentResolver.update(uri, values, null, null);
        }

        return uri;
    }

    /**
     * 复制
     * {@link android.os.FileUtils#copy(InputStream, OutputStream)}
     *
     * @param inputStream  inputStream
     * @param outputStream outputStream
     * @return boolean
     */
    public static boolean copy(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] buffer = new byte[8192];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, byteRead);
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
