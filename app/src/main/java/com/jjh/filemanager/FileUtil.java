package com.jjh.filemanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.jjh.filemanager.bean.FileType;
import java.io.File;
import java.util.Comparator;

/**
 * Created by ${zhaoyanjun} on 2017/1/11.
 */

public class FileUtil {

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
}
