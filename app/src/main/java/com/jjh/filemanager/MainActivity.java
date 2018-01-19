package com.jjh.filemanager;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.jjh.filemanager.adapter.FileHolder;
import com.jjh.filemanager.adapter.FileAdapter;
import com.jjh.filemanager.adapter.TitleAdapter;
import com.jjh.filemanager.adapter.base.RecyclerViewAdapter;
import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.bean.TitlePath;
import com.jjh.filemanager.bean.FileType;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    private RecyclerView title_recycler_view ;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileBean> beanList = new ArrayList<>();
    private File rootFile ;
    private LinearLayout empty_rel ;
    private int PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 100 ;
    private String rootPath ;
    private TitleAdapter titleAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        // 无标题
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置Title
        title_recycler_view = (RecyclerView) findViewById(R.id.title_recycler_view);
        //表示水平布局，flase表示从左往右，LinearLayoutManager也可以将布局设置为网格布局
        title_recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL , false ));
        titleAdapter = new TitleAdapter( MainActivity.this , new ArrayList<TitlePath>() ) ;
        title_recycler_view.setAdapter( titleAdapter );

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        fileAdapter = new FileAdapter(this, beanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);

        empty_rel = (LinearLayout) findViewById( R.id.empty_rel );

        fileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if ( viewHolder instanceof FileHolder ){
                    FileBean file = beanList.get(position);
                    FileType fileType = file.getFileType() ;
                    if ( fileType == FileType.directory) {
                        getFile(file.getPath());

                        refreshTitleState( file.getName() , file.getPath() );
                    }else if ( fileType == FileType.apk ){
                        //安装app
                        FileUtil.openAppIntent( MainActivity.this , new File( file.getPath() ) );
                    }else if ( fileType == FileType.image ){
                        FileUtil.openImageIntent( MainActivity.this , new File( file.getPath() ));
                    }else if ( fileType == FileType.txt ){
                        FileUtil.openTextIntent( MainActivity.this , new File( file.getPath() ) );
                    }else if ( fileType == FileType.music ){
                        FileUtil.openMusicIntent( MainActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.video ){
                        FileUtil.openVideoIntent( MainActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.pdf ){
                        FileUtil.openPDFIntent( MainActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.doc ){
                        FileUtil.openDocIntent( MainActivity.this ,  new File( file.getPath() ) );
                    }else {
                        FileUtil.openApplicationIntent( MainActivity.this , new File( file.getPath() ) );
                    }
                }
            }
        });

        fileAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if ( viewHolder instanceof  FileHolder ){
                    FileBean fileBean = (FileBean) fileAdapter.getItem( position );
                    FileType fileType = fileBean.getFileType() ;
                    if ( fileType != null && fileType != FileType.directory ){
                        FileUtil.sendFile( MainActivity.this , new File( fileBean.getPath() ) );
                    }
                }
                return false;
            }
        });

        titleAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                TitlePath titlePath = (TitlePath) titleAdapter.getItem( position );
                getFile( titlePath.getPath() );

                int count = titleAdapter.getItemCount() ;
                int removeCount = count - position - 1 ;
                for ( int i = 0 ; i < removeCount ; i++ ){
                    titleAdapter.removeLast();
                }
            }
        });

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        refreshTitleState( "内部存储设备" , rootPath );

        //申请权限，使用的是PermissionsDispatcher框架
        MainActivityPermissionsDispatcher.getMultiWithCheck(this);


//        // 先判断是否有权限。
//        if(AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE )) {
//            // 有权限，直接do anything.
//            getFile(rootPath);
//        } else {
//            //申请权限。
//            AndPermission.with(this)
//                    .requestCode(PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
//                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE )
//                    .send();
//        }
    }

    public void getFile(String path ) {
        rootFile = new File( path + File.separator  );//File.separator表示当前系统的路径分隔符
        new MyTask(rootFile).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , "") ;
    }


    class MyTask extends AsyncTask {
        File file;

        MyTask(File file) {
            this.file = file;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            List<FileBean> fileBeenList = new ArrayList<>();
            if ( file.isDirectory() ) {
                File[] filesArray = file.listFiles();
                if ( filesArray != null ){
                    List<File> fileList = new ArrayList<>() ;
                    Collections.addAll( fileList , filesArray ) ;  //把数组转化成list
                    Collections.sort( fileList , FileUtil.comparator );  //按照名字排
                    for (File f : fileList ) {
                        if (f.isHidden()) continue;
                        FileBean fileBean = new FileBean();
                        fileBean.setName(f.getName());
                        fileBean.setPath(f.getAbsolutePath());
                        fileBean.setFileType( FileUtil.getFileType( f ));
                        fileBean.setChildCount( FileUtil.getFileChildCount( f ));
                        fileBean.setSonFolderCount(FileUtil.getSonFloderCount(f));
                        fileBean.setSonFileCount(FileUtil.getSonFileCount(f));
                        fileBean.setSize( f.length() );
                        fileBean.setHolderType( 0 );
                        fileBeenList.add(fileBean);
                        FileBean lineBean = new FileBean();
                        lineBean.setHolderType( 1 );
                        fileBeenList.add( lineBean );

                    }
                }
            }

            beanList = fileBeenList;
            return fileBeenList;
        }

        @Override
        protected void onPostExecute(Object o) {
            if ( beanList.size() > 0  ){
                empty_rel.setVisibility( View.GONE );
            }else {
                empty_rel.setVisibility( View.VISIBLE );
            }
            fileAdapter.refresh(beanList);
        }
    }

    void refreshTitleState( String title , String path ){
        TitlePath filePath = new TitlePath() ;
        filePath.setNameState( title + " > " );
        filePath.setPath( path );
        titleAdapter.addItem( filePath );
        title_recycler_view.smoothScrollToPosition( titleAdapter.getItemCount());//将RecyclerView滑动到指定位置
    }

    /*监听手机back键
        event.getRepeatCount() == 0意义：一些键(MediaKey,BackKey)在系统分发时，做了特殊处理:
        当按下时，发送Message去调用KeyEvent.changeTimeRepeat
        当长按住该键时，会发多次KeyEvent.ACTION_DOWN信号，第一次event.getRepeatCount()返回0;
        第二次event.getRepeatCount()返回1;所以设置==0，可以避免长按时多次调用onKeyDown时的影响
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            List<TitlePath> titlePathList = (List<TitlePath>) titleAdapter.getAdapterData();
            if ( titlePathList.size() == 1 ){
                finish();
            }else {
                titleAdapter.removeItem( titlePathList.size() - 1 );
                getFile( titlePathList.get(titlePathList.size() - 1 ).getPath() );
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //当权限通过时执行getMulti方法
    @NeedsPermission({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void getMulti() {
        getFile(rootPath);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale({Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void tt(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("使用此功能需要同意读写文件的权限，否则无法正常使用")
                .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();//继续执行请求
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();//取消执行请求
                finish();
            }
        }).show();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        // 只需要调用这一句，其它的交给AndPermission吧，最后一个参数是PermissionListener。
//        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
//    }
//
//    private PermissionListener listener = new PermissionListener() {
//        @Override
//        public void onSucceed(int requestCode, List<String> grantedPermissions) {
//            // 权限申请成功回调。
//            if(requestCode == PERMISSION_CODE_WRITE_EXTERNAL_STORAGE ) {
//                getFile(rootPath);
//            }
//        }
//
//        @Override
//        public void onFailed(int requestCode, List<String> deniedPermissions) {
//            // 权限申请失败回调。
//            AndPermission.defaultSettingDialog( MainActivity.this, PERMISSION_CODE_WRITE_EXTERNAL_STORAGE )
//                    .setTitle("权限申请失败")
//                    .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
//                    .setPositiveButton("好，去设置")
//                    .show();
//        }
//    };
}
