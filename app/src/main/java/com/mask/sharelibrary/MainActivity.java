package com.mask.sharelibrary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

/**
 * 官方文档：
 * 管理分区外部存储访问: https://developer.android.com/training/data-storage/files/external-scoped
 * 使用存储访问框架打开文件: https://developer.android.com/guide/topics/providers/document-provider
 * 分享简单的数据: https://developer.android.com/training/sharing
 * 分享文件: https://developer.android.com/training/secure-file-sharing
 * 共享存储空间概览: https://developer.android.com/training/data-storage/shared
 */
public class MainActivity extends AppCompatActivity {

    private Activity activity;

    private View btn_open_document;
    private View btn_share_single;
    private View btn_share_multiple;

    private File dirFile;
    private String dirName = "Mask";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
        initData();
    }

    private void initView() {
        btn_open_document = findViewById(R.id.btn_open_document);
        btn_share_single = findViewById(R.id.btn_share_single);
        btn_share_multiple = findViewById(R.id.btn_share_multiple);
    }

    private void setListener() {
        btn_open_document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDocument();
            }
        });
        btn_share_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareSingle();
            }
        });
        btn_share_multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareMultiple();
            }
        });
    }

    private void initData() {
        dirFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);

        if (dirFile != null && dirFile.isDirectory()) {
            LogUtil.i("DirFile mkdirs: " + dirFile.mkdirs());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            LogUtil.i("Environment.isExternalStorageLegacy: " + Environment.isExternalStorageLegacy());
        }

        requestPermission();

        printMediaStore();
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    /**
     * 打印 MediaStore
     */
    private void printMediaStore() {
        if (true) {
            return;
        }
        Uri contentUri = MediaStore.Files.getContentUri("external");
//        String[] projection = new String[]{MediaStore.MediaColumns._ID};
//        String selection = MediaStore.MediaColumns.DATA + "=? ";
//        String[] selectionArgs = new String[]{path};
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Uri uri = ContentUris.withAppendedId(contentUri, cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID)));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                    String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                    LogUtil.i("Uri: " + uri);
                    LogUtil.i("Name: " + name);
                    LogUtil.i("Path: " + path);
                    LogUtil.i("MimeType: " + mimeType);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    /**
     * 打开系统文件选择器
     */
    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "选择文件"), 10086);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10086 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String mimeType = FileUtils.getMimeType(activity, uri);
            LogUtil.i("onActivityResult Uri: " + uri);
            LogUtil.i("onActivityResult Path: " + FileUtils.getPath(activity, uri));
            LogUtil.i("onActivityResult Name: " + FileUtils.getName(activity, uri));
            LogUtil.i("onActivityResult MimeType: " + mimeType);

            shareSingle(uri, mimeType);
        }
    }

    /**
     * 分享单个文件
     */
    private void shareSingle() {
        File file;

        // true/false主要是判断是否可以取到ContentUri
        // 应用外图片测试
//        file = new File("/storage/emulated/0/DCIM/Camera/IMG_20200317_175127.jpg");// true
//        file = new File("/storage/emulated/0/DCIM/Screenshots/Screenshot_2020-02-18-16-08-37-324_com.raykite.mobile.jpg");// true
//        file = new File("/storage/emulated/0/DCIM/EasyPhotosDemo/IMG1594021883192.png");// true
//        file = new File("/storage/emulated/0/IMG1594021883192.png");// true
        // 应用外视频测试
//        file = new File("/storage/emulated/0/DCIM/Camera/VID_20191213_102540.mp4");// true
//        file = new File("/storage/emulated/0/DCIM/ScreenRecorder/Screenrecorder-2020-06-07-17-43-03-440.mp4");// true
//        file = new File("/storage/emulated/0/MediaRecorder_20200323_181233730.mp4");// true
        // 应用外文档测试
//        file = new File("/storage/emulated/0/Download/1594880862572.pdf");// false
//        file = new File("/storage/emulated/0/1594880862572.pdf");// false

        // 应用内图片测试
//        file = new File(dirFile, "Screenshot_2020-01-03-16-54-05-360_com.mask.chartlibrary.jpg");// false
        // 应用内文档测试
        file = new File(dirFile, "1594880862572.pdf");// false

//        MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
//            @Override
//            public void onScanCompleted(String path, Uri uri) {
//                LogUtil.i("Scan Path: " + path);
//                LogUtil.i("Scan Uri: " + uri);
//                LogUtil.i("File Path: " + FileUtils.getPath(activity, uri));
//                LogUtil.i("File Name: " + FileUtils.getName(activity, uri));
//            }
//        });

        Uri uri;

        final Uri fileUri = FileUtils.getFileUri(activity, file);
        final Uri contentUri = FileUtils.getContentUri(activity, file);
        final String mimeType = FileUtils.getMimeType(file.getName());

        LogUtil.i("File Path: " + file.getAbsolutePath());
        LogUtil.i("File Exists: " + file.exists());
        LogUtil.i("File Extension: " + FileUtils.getExtension(file.getName()));
        LogUtil.i("File MimeType: " + mimeType);
        LogUtil.i("File FileUri: " + fileUri);
        LogUtil.i("File FilePath: " + FileUtils.getPath(activity, fileUri));
        LogUtil.i("File FileName: " + FileUtils.getName(activity, fileUri));
        LogUtil.i("File FileMimeType: " + FileUtils.getMimeType(activity, fileUri));
        LogUtil.i("File ContentUri: " + contentUri);
        LogUtil.i("File ContentPath: " + FileUtils.getPath(activity, contentUri));
        LogUtil.i("File ContentName: " + FileUtils.getName(activity, contentUri));
        LogUtil.i("File ContentMimeType: " + FileUtils.getMimeType(activity, contentUri));

        if (contentUri == null) {
            final Uri copyUri = FileUtils.copyFileToExternal(activity, dirName, file);
            LogUtil.i("File CopyUri: " + copyUri);
            LogUtil.i("File CopyPath: " + FileUtils.getPath(activity, copyUri));
            LogUtil.i("File CopyName: " + FileUtils.getName(activity, copyUri));
            LogUtil.i("File CopyMimeType: " + FileUtils.getMimeType(activity, copyUri));

            uri = copyUri == null ? fileUri : copyUri;
        } else {
            uri = contentUri;
        }

        shareSingle(uri, mimeType);
    }

    /**
     * 分享多个文件
     */
    private void shareMultiple() {
        File dirFileTemp;
        String[] pathArr = new String[]{
                "Screenshot_2020-01-03-16-54-05-360_com.mask.chartlibrary.jpg",
                "Screenshot_2020-01-03-16-55-03-336_com.mask.chartlibrary.jpg",
                "Screenshot_2020-02-18-16-08-37-324_com.raykite.mobile.jpg",
                "Screenshot_2020-02-18-16-09-30-530_com.raykite.mobile.jpg",
                "Screenshot_2020-02-18-16-10-07-667_com.raykite.mobile.jpg",
        };
        // 应用外图片测试
//        dirFileTemp = new File("/storage/emulated/0/DCIM/Screenshots/");
        // 应用内图片测试
        dirFileTemp = dirFile;
        pathArr[0] = "20200720190708.png";

        ArrayList<Uri> uriList = new ArrayList<>();
        for (String path : pathArr) {
            File file = new File(dirFileTemp, path);
            final Uri fileUri = FileUtils.getFileUri(activity, file);
            final Uri contentUri = FileUtils.getContentUri(activity, file);

            LogUtil.i("Multiple Path: " + file.getAbsolutePath());
            LogUtil.i("Multiple Exists: " + file.exists());
            LogUtil.i("Multiple FileUri: " + fileUri);
            LogUtil.i("Multiple ContentUri: " + contentUri);

            Uri uri;
            if (contentUri == null) {
                final Uri copyUri = FileUtils.copyFileToExternal(activity, dirName, file);
                LogUtil.i("Multiple CopyUri: " + copyUri);

                uri = copyUri == null ? fileUri : copyUri;
            } else {
                uri = contentUri;
            }
            uriList.add(uri);
        }

        shareMultiple(uriList, "image/*");
    }

    /**
     * 分享单个文件
     *
     * @param uri      uri
     * @param mimeType mimeType
     */
    private void shareSingle(Uri uri, String mimeType) {
        // 腾讯文档多次分享同一个文件会提示"导入失败，请重新导入"，解决方案如下
        // 1. 每次分享前修改文件名；
        // 2. 每次分享前手动清空腾讯文档Cache目录；
        // 3. 更换为MediaStore Uri。
        // 猜测因为腾讯文档读取FileProvider Uri，会复制一份到自己的cache目录，相同文件会导致文件名丢失。
        // 腾讯文档 ACTION_SEND 没反应，ACTION_VIEW 才有用，其他App正常，猜测腾讯文档自身问题

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// 必须添加此flag，否则暴露的uri会无权限读写
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.setPackage("com.tencent.docs");// 指定打开APP的包名
//        intent.setPackage("cn.wps.moffice_eng");// 指定打开APP的包名
        intent.putExtra(Intent.EXTRA_STREAM, uri);  // 传输文件，采用流的方式
        intent.setDataAndType(uri, mimeType);
        startActivity(Intent.createChooser(intent, "分享单个文件"));
    }

    /**
     * 分享多个文件
     *
     * @param uriList  uriList
     * @param mimeType mimeType
     */
    private void shareMultiple(ArrayList<Uri> uriList, String mimeType) {
        // FileProvider Uri模式：
        // QQ偶尔会失败，QQ收藏必然失败，微信必然失败，微信收藏必然失败，WPS成功
        // MediaStore Uri模式都正常
        // 猜测因为部分App没有适配FileProvider Uri

        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);// 必须添加此flag，否则暴露的uri会无权限读写
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        intent.setPackage("com.tencent.docs");// 指定打开APP的包名
//        intent.setPackage("cn.wps.moffice_eng");// 指定打开APP的包名
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);  // 传输文件，采用流的方式
        intent.setType(mimeType);
        startActivity(Intent.createChooser(intent, "分享多个文件"));
    }

}