package com.jjh.filemanager.Util;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.os.storage.StorageManager;

import com.jjh.filemanager.ClassifyFileActivity;
import com.jjh.filemanager.bean.EncryptedItem;
import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.bean.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.support.v4.content.FileProvider;

import org.litepal.crud.DataSupport;

/**
 * Created by jjh on 2017.
 */

public class FileUtil {
    private static List<FileBean> photos = new ArrayList<>();
    private static List<FileBean> musics = new ArrayList<>();
    private static List<FileBean> videos = new ArrayList<>();
    private static List<FileBean> texts = new ArrayList<>();
    private static List<FileBean> zips = new ArrayList<>();
    private static List<FileBean> apks = new ArrayList<>();

    public static void  updateExternalDB(String filename, Context mContext)//filename是我们的文件全名，包括后缀和路径
    {
        MediaScannerConnection.scanFile(mContext,
                new String[] { filename }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
//                        Log.i("ExternalStorage", "Scanned " + path + ":");
//                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }



    //删除文件方法
    public static boolean deleteFile(FileBean fileBean, Context context){
        try {
            String path = fileBean.getPath();
            new File(path).delete();//因为文件信息是数据库读取的，实际删除文件后还需要去更新数据库
            FileUtil.updateExternalDB(fileBean.getPath(), context);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * RandomAccessFile ：https://www.cnblogs.com/qjlbky/p/5925464.html
     * 加解密 将文件开头读取的REVERSE_LENGTH位进行异或，异或两次即解密。
     * 随机读写并不是说把数据写入任意一个随机的文件中，而是在指定的文件中通过文件指针实现在该文件指定位置的读取和写入。
     * RandomAccessFile使用随机访问（即可以定位读取）的方式，而FileInputStream及FileOutputStream使用的是流式访问的方式。
     * @param strFile
     *            源文件绝对路径
     * @return
     */
    public static boolean encrypt(String strFile) {
        final int REVERSE_LENGTH = 32 * 1024;
        int len = REVERSE_LENGTH;
        try {
            File f = new File(strFile);
            RandomAccessFile raf = new RandomAccessFile(f, "rw");
            long totalLen = raf.length();
            if (totalLen < REVERSE_LENGTH)
                len = (int) totalLen;
//            Log.e(TAG, "encrypt: 1122233|" + raf.getFilePointer() + "|");
            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(
                    FileChannel.MapMode.READ_WRITE, 0, len);
            byte tmp;
            for (int i = 0; i < len; i++) {
                byte rawByte = buffer.get(i);
                tmp = (byte) (rawByte ^ i);
                buffer.put(i, tmp);
            }
//            Log.e(TAG, "encrypt: 1122233后|" + raf.getFilePointer() + "|");
            buffer.force();
            buffer.clear();
            channel.close();
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private static List<FileBean> getAllPhoto(Context mContext) {

        ContentResolver mContentResolver = mContext.getContentResolver();
        photos.clear();
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns.DATA,};
        //MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DISPLAY_NAME

        // asc 按升序排列
        // desc 按降序排列
        //projection 是定义返回的数据，selection 通常的sql 语句，例如
        // selection=MediaStore.Images.ImageColumns.MIME_TYPE+"=? " 那么 selectionArgs=new String[]{"jpg"};
        Cursor cursor = mContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Images.ImageColumns.DATE_MODIFIED + "  desc");

//        String imageId ;
//        String fileName;

        while (cursor.moveToNext()) {

//            imageId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
//            fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));

            File f = new File(filePath);
            if (f.isHidden()) continue;
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileType.image);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize(f.length());
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            photos.add(fileBean);

//            Log.e("phone_photo",  " -- " + filePath);
        }
        cursor.close();

//        cursor = null;

        return photos;

    }

    public static int getAllPhotoNumber(Context mContext) {
        return getAllPhoto(mContext).size();
    }

    private static List<FileBean> getAllMusic(Context mContext) {

        ContentResolver mContentResolver = mContext.getContentResolver();
        musics.clear();

        String[] projection = new String[]{MediaStore.Audio.AudioColumns.DATA};


        Cursor cursor = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Audio.AudioColumns.DATE_MODIFIED + " desc");


        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));

            File f = new File(filePath);
            if (f.isHidden()) continue;
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileType.music);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize(f.length());
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            musics.add(fileBean);


        }

        cursor.close();

        return musics;

    }

    public static int getAllMusicNumber(Context mContext) {
        return getAllMusic(mContext).size();
    }

    private static List<FileBean> getAllVideo(Context mContext) {

        ContentResolver mContentResolver = mContext.getContentResolver();
        videos.clear();

        String[] projection = new String[]{MediaStore.Video.VideoColumns.DATA};


        Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Video.VideoColumns.DATE_MODIFIED + " desc");

        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));


            File f = new File(filePath);
            if (f.isHidden()) continue;
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileType.video);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize(f.length());
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            videos.add(fileBean);

        }

        cursor.close();
        cursor = null;

        return videos;

    }

    public static int getAllVideoNumber(Context mContext) {
        return getAllVideo(mContext).size();
    }

    private static List<FileBean> getAllText(Context mContext) {

        ContentResolver mContentResolver = mContext.getContentResolver();
        texts.clear();

        String[] projection = new String[]{FileColumns.DATA};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";

        String[] selectionArgs = new String[]{"text/plain", "application/msword",
                "application/pdf", "application/vnd.ms-powerpoint", "application/vnd.ms-excel"};

        Cursor cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"),
                projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");


        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

            File f = new File(filePath);
            if (f.isHidden()) continue;
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileUtil.getFileType(f));
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize(f.length());
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            texts.add(fileBean);

        }


        cursor.close();
        cursor = null;


        return texts;

    }

    public static int getAllTextNumber(Context mContext) {
        return getAllText(mContext).size();
    }

    private static List<FileBean> getAllZip(Context mContext) {

        ContentResolver mContentResolver = mContext.getContentResolver();
        zips.clear();

        String[] projection = new String[]{FileColumns.DATA};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";

        String[] selectionArgs = new String[]{"application/zip"};

        Cursor cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");


        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

            File f = new File(filePath);
            if (f.isHidden()) continue;
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileType.zip);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize(f.length());
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            zips.add(fileBean);


        }


        return zips;

    }

    public static int getAllZipNumber(Context mContext) {
        return getAllZip(mContext).size();
    }

    private static List<FileBean> getAllApk(Context mContext) {

        ContentResolver mContentResolver = mContext.getContentResolver();
        apks.clear();

        String[] projection = new String[]{FileColumns.DATA};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";

        String[] selectionArgs = new String[]{"application/vnd.android.package-archive"};

        Cursor cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"),
                projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");


        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

            File f = new File(filePath);
            if (f.isHidden()) continue;
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileType.apk);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize(f.length());
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            apks.add(fileBean);

        }


        return apks;

    }

    public static int getAllApkNumber(Context mContext) {
        return getAllApk(mContext).size();
    }

    /*
    传入Context和文件类型，获取文件.https://www.jianshu.com/p/a6bdbefde77a
     */
    public static Cursor getSpecificTypeOfFile(Context context, String[] extension) {
        //从外存中获取
        Uri fileUri = Files.getContentUri("external");
        //筛选列，这里只筛选了：文件路径和不含后缀的文件名
        String[] projection = new String[]{
                FileColumns.DATA, FileColumns.TITLE
        };
        //构造筛选语句
        String selection = "";
        for (int i = 0; i < extension.length; i++) {
            if (i != 0) {
                selection = selection + " OR ";
            }
            selection = selection + FileColumns.DATA + " LIKE '%" + extension[i] + "'";
        }
        //按时间递减顺序对结果进行排序;
        String sortOrder = FileColumns.DATE_MODIFIED + " desc";
        //获取内容解析器对象
        ContentResolver resolver = context.getContentResolver();
        //获取游标
        Cursor cursor = resolver.query(fileUri, projection, selection, null, sortOrder);
        if (cursor == null)
            return null;
        else
            return cursor;
    }

    /*
    传入Context和关键字，获取文件.参看https://blog.csdn.net/zzh12138/article/details/71077909
     */
    public static List<FileBean> searchKeyWord(Context context, String keyword) {
        List<FileBean> fileList = new ArrayList<FileBean>();
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor = resolver.query(uri,
                new String[]{MediaStore.Files.FileColumns.DATA},
                MediaStore.Files.FileColumns.TITLE + " LIKE '%" + keyword + "%'",
                null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));

                File f = new File(path);
                if (f.isHidden() || f.isDirectory()) continue;
                FileBean fileBean = new FileBean();
                fileBean.setName(f.getName());
                fileBean.setPath(path);
                fileBean.setFileType(FileUtil.getFileType(f));
                fileBean.setChildCount(FileUtil.getFileChildCount(f));//可有可无
                fileBean.setSonFolderCount(FileUtil.getSonFloderCount(f));
                fileBean.setSonFileCount(FileUtil.getSonFileCount(f));
                fileBean.setSize(f.length());
                fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
                fileList.add(fileBean);
            }
        }
        cursor.close();
        return fileList;
    }


    /**
     * 字符串时间戳转时间格式
     *
     * @param timeStamp
     * @return
     */
    public static String getStrTime(String timeStamp) {
        String timeString = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
        long l = Long.valueOf(timeStamp) * 1000;
        timeString = sdf.format(new Date(l));
        return timeString;
    }

    /**
     * 读取文件的最后修改时间的方法
     */
    public static String getFileLastModifiedTime(File f) {
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        SimpleDateFormat formatter = new
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
    }

    /**
     * 得到所有的存储路径
     *
     * @param context
     * @return
     */
    public static String[] getAllSdPaths(Context context) {
        Method mMethodGetPaths = null;
        String[] paths = null;
        //通过调用类的实例mStorageManager的getClass()获取StorageManager类对应的Class对象
        //getMethod("getVolumePaths")返回StorageManager类对应的Class对象的getVolumePaths方法，这里不带参数
        StorageManager mStorageManager = (StorageManager) context
                .getSystemService(context.STORAGE_SERVICE);//storage
        try {
            mMethodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths");
            paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return paths;
    }

    //得到外置SD卡的路径，来自网络
    public static String getExtendedMemoryPath(Context mContext) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static Boolean copyFile(String oldPath, String newPath) {
        Boolean state = false;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                state = true;
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }


    /*
    getAvailableBlocksLong()
    文件系统中可被应用程序使用的空闲存储区块的数量
    getBlockCountLong()
    文件系统中总的存储区块的数量
    getBlockSizeLong()
    文件系统中每个存储区块的字节数
    getFreeBlocksLong()
    文件系统中总的空闲存储区块的数量，包括保留的存储区块（不能被普通应用程序使用）

    getFreeBytes()
    文件系统中总的空闲字节数，包括保留的存储区块（不能被普通应用程序使用）
    getTotalBytes()
    文件系统支持的总的存储字节数
    getAvailableBytes()
    文件系统中可被应用程序使用的空闲字节数
     */
    public static String getAvailSpace(Context context, String path) {
        //获取可用内存大小
        StatFs statfs = new StatFs(path);
        //获取可用区块的个数
//        long count=statfs.getAvailableBlocksLong();
//        //获取每个区块大小
//        long size=statfs.getBlockSizeLong();
        String FileSize = android.text.format.Formatter.formatFileSize(context, statfs.getAvailableBytes());
        //可用空间总大小
        return FileSize;
    }

    public static String getAllSpace(Context context, String path) {
        //获取总内存大小,不包含固件大小。
        StatFs statfs = new StatFs(path);
//        //获取总区块的个数
//        long count=statfs.getBlockCountLong();
//        //获取每个区块大小
//        long size=statfs.getBlockSizeLong();
        //支持空间总大小
        String FileSize = android.text.format.Formatter.formatFileSize(context, statfs.getTotalBytes());
        return FileSize;
    }

    /**
     * 获取文件类型
     *
     * @param file
     * @return
     */
    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.directory;
        }
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".mp3")) {
            return FileType.music;
        }

        if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
                || fileName.endsWith(".3gp") || fileName.endsWith(".mov")
                || fileName.endsWith(".rmvb") || fileName.endsWith(".mkv")
                || fileName.endsWith(".flv") || fileName.endsWith(".rm")) {
            return FileType.video;
        }

        if (fileName.endsWith(".txt") || fileName.endsWith(".log") || fileName.endsWith(".xml")) {
            return FileType.txt;
        }

        if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            return FileType.zip;
        }

        if (fileName.endsWith(".png") || fileName.endsWith(".gif")
                || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return FileType.image;
        }

        if (fileName.endsWith(".apk")) {
            return FileType.apk;
        }
        if (fileName.endsWith(".pdf")) {
            return FileType.pdf;
        }
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return FileType.doc;
        }


        return FileType.other;
    }

    public static FileType getFileNameType(String fileName) {
        fileName = fileName.toLowerCase();

        if (fileName.endsWith(".mp3")) {
            return FileType.music;
        }

        if (fileName.endsWith(".mp4") || fileName.endsWith(".avi")
                || fileName.endsWith(".3gp") || fileName.endsWith(".mov")
                || fileName.endsWith(".rmvb") || fileName.endsWith(".mkv")
                || fileName.endsWith(".flv") || fileName.endsWith(".rm")) {
            return FileType.video;
        }

        if (fileName.endsWith(".txt") || fileName.endsWith(".log") || fileName.endsWith(".xml")) {
            return FileType.txt;
        }

        if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            return FileType.zip;
        }

        if (fileName.endsWith(".png") || fileName.endsWith(".gif")
                || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
            return FileType.image;
        }

        if (fileName.endsWith(".apk")) {
            return FileType.apk;
        }
        if (fileName.endsWith(".pdf")) {
            return FileType.pdf;
        }
        if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return FileType.doc;
        }


        return FileType.other;
    }

    private static boolean isLetter(String str) {
        String regex = "^[a-zA-Z]+$";//其他需要，直接修改正则表达式就好.判断为单词
//        String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$"
        return str.matches(regex);
    }

    //自定义文件排序，汉字，_，字母依次
    private static int compareAB(String left2, String right2) {
        String A = left2.substring(0, 1);
        String B = right2.substring(0, 1);
        if (!isLetter(A) && !isLetter(B)) {
            return A.compareTo(B);
        } else if (!isLetter(A)) {
            return -1;
        } else if (!isLetter(B)) {
            return 1;
        } else {
            //将字母的按字母排序，不按大小写排序
            String a = A.toLowerCase();
            String b = B.toLowerCase();
            if (a.compareTo(b) == 0) {
                return A.compareTo(B);
            } else {
                return a.compareTo(b);
            }
        }

    }

    /**
     * 文件先按照文件夹在上文件再后排序
     * 然后类型一样按名字排名，汉字，_，等在前，字母放在后面
     * 类型不同按类型排名
     */
    public static Comparator comparator = new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
            if (file1.isDirectory() && file2.isFile()) {
                return -1;
            } else if (file1.isFile() && file2.isDirectory()) {
                return 1;
            } else if (FileUtil.getFileType(file1) != FileUtil.getFileType(file2)) {
                //先按类型排序
                return FileUtil.getFileType(file1).compareTo(FileUtil.getFileType(file2));
            } else {
                //再排序同类型的文件或文件夹
                return compareAB(file1.getName(), file2.getName());
            }
        }
    };

    //按名称升序
    public static Comparator comparatorNameAsc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else {
                return fileBean1.getName().compareTo(fileBean2.getName());
            }
        }
    };

    //按名称降序
    public static Comparator comparatorNameDesc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else {
                return fileBean2.getName().compareTo(fileBean1.getName());
            }
        }
    };

    //按大小升序
    public static Comparator comparatorSizeAsc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else {
                if (fileBean1.getSize() - fileBean2.getSize() < 0) {
                    return -1;// 小文件在前
                } else if (fileBean1.getSize() - fileBean2.getSize() > 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    };

    //按大小降序
    public static Comparator comparatorSizeDesc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else {
                if (fileBean1.getSize() - fileBean2.getSize() > 0) {
                    return -1;// 大文件在前
                } else if (fileBean1.getSize() - fileBean2.getSize() < 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    };

    //按类型升序
    public static Comparator comparatorTypeAsc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else if (fileBean1.getFileType() == fileBean2.getFileType()) {
                return 0;
            } else {
                return fileBean1.getFileType().compareTo(fileBean2.getFileType());
            }
        }
    };

    //按类型降序
    public static Comparator comparatorTypeDesc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else if (fileBean1.getFileType() == fileBean2.getFileType()) {
                return 0;
            } else {
                return fileBean2.getFileType().compareTo(fileBean1.getFileType());
            }
        }
    };

    //按时间升序
    public static Comparator comparatorDateAsc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Long time1 = dateFormat.parse(fileBean1.getDate()).getTime();
                    Long time2 = dateFormat.parse(fileBean2.getDate()).getTime();
                    if (time1 < time2) {
                        return -1;// 最后修改的文件在后
                    } else if (time1 > time2) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        }
    };

    //按时间降序
    public static Comparator comparatorDateDesc = new Comparator<FileBean>() {
        @Override
        public int compare(FileBean fileBean1, FileBean fileBean2) {
            if (fileBean1.getFileType() == FileType.directory && fileBean2.getFileType() != FileType.directory) {
                return -1;
            } else if (fileBean1.getFileType() != FileType.directory && fileBean2.getFileType() == FileType.directory) {
                return 1;
            } else {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Long time1 = dateFormat.parse(fileBean1.getDate()).getTime();
                    Long time2 = dateFormat.parse(fileBean2.getDate()).getTime();
                    if (time1 > time2) {
                        return -1;// 最后修改的文件在前
                    } else if (time1 < time2) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        }
    };

    /**
     * 获取文件的子文件个数
     *
     * @param file
     * @return
     */
    public static int getFileChildCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                count++;
            }
        }
        return count;
    }

    /**
     * 获取文件的子文件个数
     *
     * @param file
     * @return
     */
    public static int getSonFileCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                if (!f.isDirectory()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 获取文件的子文件夹个数
     *
     * @param file
     * @return
     */
    public static int getSonFloderCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                if (f.isDirectory()) {
                    count++;
                }
            }
        }
        return count;
    }


    /**
     * 文件大小转换
     *
     * @param size
     * @return
     */
    public static String sizeToChange(long size) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");  //字符格式化，为保留小数做准备

        double G = size * 1.0 / 1024 / 1024 / 1024;
        if (G >= 1) {
            return df.format(G) + " GB";
        }

        double M = size * 1.0 / 1024 / 1024;
        if (M >= 1) {
            return df.format(M) + " MB";
        }

        double K = size * 1.0 / 1024;
        if (K >= 1) {
            return df.format(K) + " KB";
        }

        return size + " B";
    }

   /*
    android intent中设置如下flag，可以清除栈顶的activity：
    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);添加这一句表示对目标应用临时授权该Uri所代表的文件
    还有其他tag如下：
    1.FLAG_ACTIVITY_CLEAR_TOP：跳转到的activity若已在栈中存在，则将其上的activity都销掉。
    2.FLAG_ACTIVITY_NEW_TASK：activity要存在于activity的栈中，而非activity的途径启动activity时必然不存在一个
    activity的栈，所以要新起一个栈装入启动的activity。简而言之，跳转到的activity根据情况，可能压在一个新建的栈中。
    3.FLAG_ACTIVITY_NO_HISTORY：跳转到的activity不压在栈中。
    4.FLAG_ACTIVITY_SINGLE_TOP：和Activity的Launch mode的singleTop类似。如果某个intent添加了这个标志，
    并且这个intent的目标activity就是栈顶的activity，那么将不会新建一个实例压入栈中。简而言之，
    目标activity已在栈顶则跳转过去，不在栈顶则在栈顶新建activity。
    */

    /**
     * 安装apk
     *
     * @param context
     * @param file
     */
    public static void openAppIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);//显示数据的通用intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    /**
     * 打开图片资源
     *
     * @param context
     * @param file
     */
    public static void openImageIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "image/*");
//            Log.e(context.getPackageName(), "openImageIntent111: 222" );

            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
//            Log.e(context.getPackageName(), "openImageIntent111: 111" );
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 打开PDF资源
     *
     * @param context
     * @param file
     */
    public static void openPDFIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 打开doc资源
     *
     * @param context
     * @param file
     */
    public static void openDocIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "application/msword");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/msword");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }


    /**
     * 打开文本资源
     *
     * @param context
     * @param file
     */
    public static void openTextIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "text/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(file), "text/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 打开音频资源
     *
     * @param context
     * @param file
     */
    public static void openMusicIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "audio/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 打开视频资源
     *
     * @param context
     * @param file
     */
    public static void openVideoIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "video/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(file), "video/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);

    }

    /**
     * 打开所有能打开应用资源
     *
     * @param context
     * @param file
     */
    public static void openApplicationIntent(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "application/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);

    }

    /**
     * 发送文件给第三方app
     *
     * @param context
     * @param file
     */
    public static void sendFile(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addCategory("android.intent.category.DEFAULT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext().getPackageName() + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("*/*");//此处可发送多种文件
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("*/*");//此处可发送多种文件
        }
        context.startActivity(Intent.createChooser(intent, "发送"));
    }

    public static List<FileBean> getPhotos() {
        return photos;
    }

    public static List<FileBean> getMusics() {
        return musics;
    }

    public static List<FileBean> getVideos() {
        return videos;
    }

    public static List<FileBean> getTexts() {
        return texts;
    }

    public static List<FileBean> getZips() {
        return zips;
    }

    public static List<FileBean> getApks() {
        return apks;
    }

    public static String getFileNameMD5(String fileName) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(fileName.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


}
