package com.jjh.filemanager;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjh.filemanager.adapter.FileAdapter;
import com.jjh.filemanager.adapter.FileHolder;
import com.jjh.filemanager.adapter.base.RecyclerViewAdapter;
import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.bean.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ClassifyFileActivity extends AppCompatActivity {

    private static final String TAG="ClassifyFileActivity";
    private TextView title ;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileBean> beanList = new ArrayList<>();
    private LinearLayout empty_rel ;
    private int Type;
    private final int TYPE_MUSIC = 1;
    private final int TYPE_IMAGE = 2;
    private final int TYPE_TXT   = 3;
    private final int TYPE_VIDEO = 4;
    private final int TYPE_APK   = 5;
    private final int TYPE_ZIP   = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify_file);
//        if (getSupportActionBar() != null){
//            getSupportActionBar().hide();
//        }
        //初始化页面和监听
        initView();
        //根据点击路径浏览文件
        browsePath();
    }


    public void initView(){
        //设置Title
        title = (TextView) findViewById(R.id.activity_classify_title);
        //文件为空时显示该界面，不为空时隐藏
        empty_rel = (LinearLayout)findViewById( R.id.activity_classify_empty_rel );
        recyclerView = (RecyclerView) findViewById(R.id.activity_classify_recycler_view);
        //初始化适配器
        fileAdapter = new FileAdapter(this, beanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //添加适配器
        recyclerView.setAdapter(fileAdapter);
        fileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if ( viewHolder instanceof FileHolder){
                    FileBean file = beanList.get(position);
                    FileType fileType = file.getFileType() ;
                    if ( fileType == FileType.apk ){
                        //安装app
                        FileUtil.openAppIntent( ClassifyFileActivity.this , new File( file.getPath() ) );
                    }else if ( fileType == FileType.image ){
                        FileUtil.openImageIntent( ClassifyFileActivity.this , new File( file.getPath() ));
                    }else if ( fileType == FileType.txt ){
                        FileUtil.openTextIntent( ClassifyFileActivity.this , new File( file.getPath() ) );
                    }else if ( fileType == FileType.music ){
                        FileUtil.openMusicIntent( ClassifyFileActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.video ){
                        FileUtil.openVideoIntent( ClassifyFileActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.pdf ){
                        FileUtil.openPDFIntent( ClassifyFileActivity.this ,  new File( file.getPath() ) );
                    }else if ( fileType == FileType.doc ){
                        FileUtil.openDocIntent( ClassifyFileActivity.this ,  new File( file.getPath() ) );
                    }else {
                        FileUtil.openApplicationIntent( ClassifyFileActivity.this , new File( file.getPath() ) );
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
                        FileUtil.sendFile( ClassifyFileActivity.this , new File( fileBean.getPath() ) );
                    }
                }
                return false;
            }
        });

    }

    public void browsePath(){
        //根据点击的不同存储地址显示文件列表
        Type = getIntent().getIntExtra("Type",0);
        List<FileBean> fileBeenList = new ArrayList<>();
        switch (Type){
            case 0:
                Log.e(TAG, "ERROR" );
                Toast.makeText(this, "抱歉，程序异常请重试或重启！", Toast.LENGTH_LONG).show();
                finish();
                break;
            case TYPE_MUSIC:
                title.setText("音乐");
                fileBeenList = FileUtil.getMusics();
                break;
            case TYPE_IMAGE:
                title.setText("图片");
                fileBeenList = FileUtil.getPhotos();
                break;
            case TYPE_TXT:
                title.setText("文档>");
                fileBeenList = FileUtil.getTexts();
                break;
            case TYPE_VIDEO:
                title.setText("视频");
                fileBeenList = FileUtil.getVideos();
                break;
            case TYPE_APK:
                title.setText("安装包");
                fileBeenList = FileUtil.getApks();
                break;
            case TYPE_ZIP:
                title.setText("压缩包");
                fileBeenList = FileUtil.getZips();
                break;
            default:

        }
        if (fileBeenList.isEmpty()){
            empty_rel.setVisibility(View.VISIBLE);
        }
        beanList = fileBeenList;
        fileAdapter.refresh(beanList);
    }


}
