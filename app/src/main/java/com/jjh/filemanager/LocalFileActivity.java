package com.jjh.filemanager;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
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


public class LocalFileActivity extends AppCompatActivity {

    private static final String TAG="LocalFileActivity";
    private RecyclerView title_recycler_view ;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileBean> beanList = new ArrayList<>();
    private File rootFile ;
    private LinearLayout empty_rel ;
    private String Path ;
    private int pathFlag;
    private TitleAdapter titleAdapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_file);

        //初始化页面和监听
        initView();
        //根据点击路径浏览文件
        browsePath();

    }

    public void initView(){
        // 无标题
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置Title
        title_recycler_view = (RecyclerView) findViewById(R.id.title_recycler_view);
        //文件为空时显示该界面，不为空时隐藏
        empty_rel = (LinearLayout) findViewById( R.id.empty_rel );
        //表示水平布局，flase表示从左往右，LinearLayoutManager也可以将布局设置为网格布局
        title_recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL , false ));
        titleAdapter = new TitleAdapter( LocalFileActivity.this , new ArrayList<TitlePath>() ) ;
        title_recycler_view.setAdapter( titleAdapter );
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //初始化适配器
        fileAdapter = new FileAdapter(this, beanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //添加适配器
        recyclerView.setAdapter(fileAdapter);
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
                        FileUtil.openAppIntent( LocalFileActivity.this , new File( file.getPath() ) );
                    }else if ( fileType == FileType.image ){
                        FileUtil.openImageIntent( LocalFileActivity.this , new File( file.getPath() ));
                    }else if ( fileType == FileType.txt ){
                        FileUtil.openTextIntent( LocalFileActivity.this , new File( file.getPath() ) );
                    }else if ( fileType == FileType.music ){
                        FileUtil.openMusicIntent( LocalFileActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.video ){
                        FileUtil.openVideoIntent( LocalFileActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.pdf ){
                        FileUtil.openPDFIntent( LocalFileActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.doc ){
                        FileUtil.openDocIntent( LocalFileActivity.this ,  new File( file.getPath() ) );
                    }else {
                        FileUtil.openApplicationIntent( LocalFileActivity.this , new File( file.getPath() ) );
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
                        FileUtil.sendFile( LocalFileActivity.this , new File( fileBean.getPath() ) );
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
    }

    public void browsePath(){
//      得到手机上的所有路径
//      String[] Paths = FileUtil.getAllSdPaths(this);
//      String rootPath = Paths[0];
//      String SDPath =Paths[1];
//      Log.e(TAG, "browsePath: " + SDPath );
        //根据点击的不同存储地址显示文件列表
        Path = getIntent().getStringExtra("Path");
        pathFlag = getIntent().getIntExtra("flagPath", -1);
        switch (pathFlag){
            case 0:
                refreshTitleState( "内部存储设备" , Path );
                break;
            case 1:
                refreshTitleState( "SD卡存储设备" , Path );
                break;
            case -1:
                Log.e(TAG, "ERROR" );
                Toast.makeText(this, "抱歉，程序异常请重试或重启！", Toast.LENGTH_LONG).show();
                finish();
                break;
            default:
        }
        getFile(Path);
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
                        fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
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

}
