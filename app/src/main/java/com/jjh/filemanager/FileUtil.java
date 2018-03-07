package com.jjh.filemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.bean.FileType;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.Formatter.*;
import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;

/**
 * Created by jjh on 2017.
 */

public class FileUtil {
    private static List<FileBean> photos = new ArrayList<>();
    private static List<FileBean> musics = new ArrayList<>();
    private static List<FileBean> videos = new ArrayList<>();
    private static List<FileBean> texts  = new ArrayList<>();
    private static List<FileBean> zips   = new ArrayList<>();
    private static List<FileBean> apks   = new ArrayList<>();


    private static List<FileBean> getAllPhoto(Context mContext) {

        ContentResolver mContentResolver =  mContext.getContentResolver();
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
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType( FileType.image);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize( f.length() );
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            fileBean.setHolderType( 0 );
            photos.add(fileBean);
            FileBean lineBean = new FileBean();
            lineBean.setHolderType( 1 );
            photos.add( lineBean );

//            Log.e("phone_photo",  " -- " + filePath);
        }
        cursor.close();

        cursor = null;

        return photos;

    }

    public static int getAllPhotoNumber(Context mContext){
        return getAllPhoto(mContext).size()/2;//每一个数据都放了一个分割线数据，所以除以2
    }

    private static List<FileBean> getAllMusic(Context mContext) {

        ContentResolver mContentResolver =  mContext.getContentResolver();
        musics.clear();

        String[] projection = new String[]{MediaStore.Audio.AudioColumns.DATA};


        Cursor cursor = mContentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Audio.AudioColumns.DATE_MODIFIED + " desc");



        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));

            File f = new File(filePath);
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType( FileType.music);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize( f.length() );
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            fileBean.setHolderType( 0 );
            musics.add(fileBean);
            FileBean lineBean = new FileBean();
            lineBean.setHolderType( 1 );
            musics.add( lineBean );

        }

        cursor.close();

        cursor = null;

        return musics;

    }

    public static int getAllMusicNumber(Context mContext){
        return getAllMusic(mContext).size()/2;
    }

    private static List<FileBean> getAllVideo(Context mContext) {

        ContentResolver mContentResolver =  mContext.getContentResolver();
        videos.clear();

        String[] projection = new String[]{MediaStore.Video.VideoColumns.DATA};


        Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Video.VideoColumns.DATE_MODIFIED + " desc");

        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));


            File f = new File(filePath);
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType( FileType.video);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize( f.length() );
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            fileBean.setHolderType( 0 );
            videos.add(fileBean);
            FileBean lineBean = new FileBean();
            lineBean.setHolderType( 1 );
            videos.add( lineBean );


        }

        cursor.close();
        cursor = null;

        return videos;

    }

    public static int getAllVideoNumber(Context mContext){
        return getAllVideo(mContext).size()/2;
    }

    private static List<FileBean> getAllText(Context mContext) {

        ContentResolver mContentResolver =  mContext.getContentResolver();
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
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileUtil.getFileType( f ));
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize( f.length() );
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            fileBean.setHolderType( 0 );
            texts.add(fileBean);
            FileBean lineBean = new FileBean();
            lineBean.setHolderType( 1 );
            texts.add( lineBean );

        }


        cursor.close();
        cursor = null;


        return texts;

    }

    public static int getAllTextNumber(Context mContext){
        return getAllText(mContext).size()/2;
    }

    private static List<FileBean> getAllZip(Context mContext) {

        ContentResolver mContentResolver =  mContext.getContentResolver();
        zips.clear();

        String[] projection = new String[]{FileColumns.DATA};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";

        String[] selectionArgs = new String[]{"application/zip"};

        Cursor cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"), projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");


        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

            File f = new File(filePath);
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileType.zip);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize( f.length() );
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            fileBean.setHolderType( 0 );
            zips.add(fileBean);
            FileBean lineBean = new FileBean();
            lineBean.setHolderType( 1 );
            zips.add( lineBean );



        }


        return zips;

    }

    public static int getAllZipNumber(Context mContext){
        return getAllZip(mContext).size()/2;
    }

    private static List<FileBean> getAllApk(Context mContext) {

        ContentResolver mContentResolver =  mContext.getContentResolver();
        apks.clear();

        String[] projection = new String[]{FileColumns.DATA};

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? ";

        String[] selectionArgs = new String[]{"application/vnd.android.package-archive"};

        Cursor cursor = mContentResolver.query(MediaStore.Files.getContentUri("external"),
                projection, selection, selectionArgs, MediaStore.Files.FileColumns.DATE_MODIFIED + " desc");


        while (cursor.moveToNext()) {

            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

            File f = new File(filePath);
            FileBean fileBean = new FileBean();
            fileBean.setName(f.getName());
            fileBean.setPath(filePath);
            fileBean.setFileType(FileType.apk);
//            fileBean.setChildCount(0);int在类中默认初始化值为0
//            fileBean.setSonFolderCount(0);
//            fileBean.setSonFileCount(0);
            fileBean.setSize( f.length() );
            fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
            fileBean.setHolderType( 0 );
            apks.add(fileBean);
            FileBean lineBean = new FileBean();
            lineBean.setHolderType( 1 );
            apks.add( lineBean );

        }


        return apks;

    }

    public static int getAllApkNumber(Context mContext){
        return getAllApk(mContext).size()/2;
    }

    /*
    传入Context和文件类型，获取文件.https://www.jianshu.com/p/a6bdbefde77a
     */
    public static Cursor getSpecificTypeOfFile(Context context,String[] extension)
    {
        //从外存中获取
        Uri fileUri=Files.getContentUri("external");
        //筛选列，这里只筛选了：文件路径和不含后缀的文件名
        String[] projection=new String[]{
                FileColumns.DATA,FileColumns.TITLE
        };
        //构造筛选语句
        String selection="";
        for(int i=0;i<extension.length;i++)
        {
            if(i!=0)
            {
                selection=selection+" OR ";
            }
            selection=selection+FileColumns.DATA+" LIKE '%"+extension[i]+"'";
        }
        //按时间递减顺序对结果进行排序;
        String sortOrder=FileColumns.DATE_MODIFIED + " desc";
        //获取内容解析器对象
        ContentResolver resolver=context.getContentResolver();
        //获取游标
        Cursor cursor=resolver.query(fileUri, projection, selection, null, sortOrder);
        if(cursor == null)
            return null;
        else
            return cursor;
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
        StorageManager mStorageManager = (StorageManager)context
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
        StatFs statfs=new StatFs(path);
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
        StatFs statfs=new StatFs(path);
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
     * @param file
     * @return
     */
    public static FileType getFileType(File file ){
        if (file.isDirectory()) {
            return FileType.directory ;
        }
        String fileName = file.getName().toLowerCase() ;

        if ( fileName.endsWith(".mp3")) {
            return FileType.music ;
        }

        if ( fileName.endsWith(".mp4") || fileName.endsWith( ".avi")
                || fileName.endsWith( ".3gp") || fileName.endsWith( ".mov")
                || fileName.endsWith( ".rmvb") || fileName.endsWith( ".mkv")
                || fileName.endsWith( ".flv") || fileName.endsWith( ".rm")) {
            return FileType.video ;
        }

        if ( fileName.endsWith(".txt") || fileName.endsWith(".log") || fileName.endsWith(".xml")) {
            return FileType.txt ;
        }

        if ( fileName.endsWith(".zip") || fileName.endsWith( ".rar")) {
            return FileType.zip ;
        }

        if ( fileName.endsWith(".png") || fileName.endsWith( ".gif")
                || fileName.endsWith( ".jpeg") || fileName.endsWith( ".jpg")   ) {
            return FileType.image ;
        }

        if ( fileName.endsWith(".apk") ) {
            return FileType.apk ;
        }
        if ( fileName.endsWith(".pdf") ) {
            return FileType.pdf ;
        }
        if ( fileName.endsWith(".doc") ) {
            return FileType.doc ;
        }


        return FileType.other ;
    }


    private static boolean isLetter(String str) {
        String regex = "^[a-zA-Z]+$";//其他需要，直接修改正则表达式就好
//        String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$"
        return str.matches(regex);
    }

    //自定义文件排序，汉字，_，字母依次
    private static int compareAB(String left2, String right2){
        String A = left2.substring(0,1);
        String B = right2.substring(0,1);
        if(!isLetter(A) && !isLetter(B)){
            return A.compareTo(B);
        }else if(!isLetter(A)){
            return -1;
        }else if(!isLetter(B)){
            return 1;
        }else {
            //将字母的按字母排序，不按大小写排序
            String a = A.toLowerCase();
            String b = B.toLowerCase();
            if(a.compareTo(b) == 0){
                return A.compareTo(B);
            }else {
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
        public int compare(File file1 , File file2 ) {
            if ( file1.isDirectory() && file2.isFile() ){
                return -1 ;
            }else if ( file1.isFile() && file2.isDirectory() ){
                return 1 ;
            }else if(FileUtil.getFileType(file1) != FileUtil.getFileType(file2)){
                //先按类型排序
                return FileUtil.getFileType(file1).compareTo(FileUtil.getFileType(file2));
            }else {
                //再排序同类型的文件或文件夹
                return compareAB(file1.getName(), file2.getName()) ;
            }
        }
    } ;

    /**
     * 获取文件的子文件个数
     * @param file
     * @return
     */
    public static int getFileChildCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                count ++ ;
            }
        }
        return count;
    }

    /**
     * 获取文件的子文件个数
     * @param file
     * @return
     */
    public static int getSonFileCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                if(!f.isDirectory()){
                    count ++ ;
                }
            }
        }
        return count;
    }

    /**
     * 获取文件的子文件夹个数
     * @param file
     * @return
     */
    public static int getSonFloderCount(File file) {
        int count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                if(f.isDirectory()){
                    count ++ ;
                }
            }
        }
        return count;
    }


    /**
     * 文件大小转换
     * @param size
     * @return
     */
    public static String sizeToChange( long size ){
        java.text.DecimalFormat df   =new   java.text.DecimalFormat("#.00");  //字符格式化，为保留小数做准备

        double G = size * 1.0 / 1024 / 1024 /1024 ;
        if ( G >= 1 ){
            return df.format( G ) + " GB";
        }

        double M = size * 1.0 / 1024 / 1024  ;
        if ( M >= 1 ){
            return df.format( M ) + " MB";
        }

        double K = size  * 1.0 / 1024   ;
        if ( K >= 1 ){
            return df.format( K ) + " KB";
        }

        return size + " B" ;
    }

   /*
    android intent中设置如下flag，可以清除栈顶的activity：
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
     * @param context
     * @param file
     */
    public static void openAppIntent(Context context , File file ){
        Intent intent = new Intent(Intent.ACTION_VIEW);//显示数据的通用intent
        intent.setDataAndType(Uri.fromFile( file ), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 打开图片资源
     * @param context
     * @param file
     */
    public static void openImageIntent( Context context , File file ) {
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(path, "image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 打开PDF资源
     * @param context
     * @param file
     */
    public static void openPDFIntent( Context context , File file ) {
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(path, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 打开doc资源
     * @param context
     * @param file
     */
    public static void openDocIntent( Context context , File file ) {
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(path, "application/msword");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }


    /**
     * 打开文本资源
     * @param context
     * @param file
     */
    public static void openTextIntent( Context context , File file ) {
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(path, "text/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * 打开音频资源
     * @param context
     * @param file
     */
    public static void openMusicIntent( Context context , File file ){
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setDataAndType(path, "audio/*");
        context.startActivity(intent);
    }

    /**
     * 打开视频资源
     * @param context
     * @param file
     */
    public static void openVideoIntent( Context context , File file ){
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setDataAndType(path, "video/*");
        context.startActivity(intent);
    }

    /**
     * 打开所有能打开应用资源
     * @param context
     * @param file
     */
    public static void openApplicationIntent( Context context , File file ){
        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setDataAndType(path, "application/*");
        context.startActivity(intent);
    }

    /**
     * 发送文件给第三方app
     * @param context
     * @param file
     */
    public static void sendFile( Context context , File file ){
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_STREAM,
                Uri.fromFile(file));
        share.setType("*/*");//此处可发送多种文件
        context.startActivity(Intent.createChooser(share, "发送"));
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

}
