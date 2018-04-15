package com.jjh.filemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjh.filemanager.adapter.FileAdapter;
import com.jjh.filemanager.adapter.FileHolder;
import com.jjh.filemanager.adapter.base.RecyclerViewAdapter;
import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.bean.FileType;
import com.jjh.filemanager.fragment.classifyFileFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class ClassifyFileActivity extends AppCompatActivity {

    private static final String TAG="ClassifyFileActivity";
    private TextView title ;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private RelativeLayout bodyLayout;
    private List<FileBean> beanList = new ArrayList<>();
    private List<FileBean> localBeanList = new ArrayList<>();//接收不含分割线的列表，用于方便排序
    private LinearLayout empty_rel ;
    private int Type;
    private ProgressBar progressBar;
    private final int TYPE_MUSIC = 1;
    private final int TYPE_IMAGE = 2;
    private final int TYPE_TXT   = 3;
    private final int TYPE_VIDEO = 4;
    private final int TYPE_APK   = 5;
    private final int TYPE_ZIP   = 6;
    private final int SEARCH_FILE   = 7;
    private int sortOrder;


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
        progressBar = (ProgressBar)findViewById(R.id.activity_classify_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        bodyLayout = (RelativeLayout) findViewById(R.id.activity_classify_body);
        bodyLayout.setVisibility(View.GONE);

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

    public void onClickMenu(View v){
        switch (v.getId()){
            case R.id.activity_sort:
                showOrderDialog();
                break;
            case R.id.activity_search:
                showSearchDialog();
                break;
            case R.id.activity_edit:
                break;
            default:
        }

    }

    private void showSearchDialog() {
    /*@setView 装入一个EditView
     */
        final EditText editText = new EditText(ClassifyFileActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(ClassifyFileActivity.this);
        inputDialog.setTitle("搜索当前文件").setView(editText);
        inputDialog.setPositiveButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(LocalFileActivity.this,
//                                editText.getText().toString(),
//                                Toast.LENGTH_SHORT).show();
                    }
                });
        inputDialog.setNegativeButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<FileBean> localList = new ArrayList<>();
                        String fileInfo = editText.getText().toString();
                        if(!localBeanList.isEmpty() || fileInfo != null && fileInfo != ""){
                            //在每一项数据后面加分割线对象
                            Iterator it = localBeanList.iterator();
                            FileBean localFileBean = new FileBean();
                            while(it.hasNext()) {
                                localFileBean = (FileBean)it.next();
                                if(localFileBean.getName().contains(fileInfo)){
                                    localList.add(localFileBean);
                                }
                            }
                        }
                        localBeanList = localList;
                        localList = new ArrayList<>();;
                        if(!localBeanList.isEmpty()){
                            Iterator it = localBeanList.iterator();
                            while(it.hasNext()) {
                                localList.add((FileBean) it.next());
                                FileBean lineBean = new FileBean();
                                lineBean.setHolderType( 1 );
                                localList.add( lineBean );
                            }
                        }
                        beanList = localList;
                        fileAdapter.refresh(beanList);
                    }
                }
        );
        inputDialog.show();
    }

    // 排序选择
    private void showOrderDialog(){

        final String[] items = { "名称","大小","类型","时间"};
        sortOrder = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(ClassifyFileActivity.this);
        singleChoiceDialog.setTitle("选择排序方法");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                sortOrder = 0;
//                                Toast.makeText(LocalFileActivity.this, "你选择了00", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:sortOrder = 1;break;
                            case 2:sortOrder = 2;break;
                            case 3:sortOrder = 3;break;
                            default: break;
                        }
                    }
                });
        singleChoiceDialog.setPositiveButton("降序",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<FileBean> localList = new ArrayList<>();
//                        Toast.makeText(LocalFileActivity.this, "你选择了", Toast.LENGTH_SHORT).show();
                        switch (sortOrder) {
                            case -1:
                                break;
                            case 0:
//                                Toast.makeText(LocalFileActivity.this, "你选择了0jj", Toast.LENGTH_SHORT).show();
                                Collections.sort( localBeanList , FileUtil.comparatorNameDesc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            case 1:
                                Collections.sort( localBeanList , FileUtil.comparatorSizeDesc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            case 2:
                                Collections.sort( localBeanList , FileUtil.comparatorTypeDesc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            case 3:
                                Collections.sort( localBeanList , FileUtil.comparatorDateDesc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            default: break;
                        }
                        sortOrder = -1;
                        fileAdapter.refresh(beanList);//有可能涉及ui刷新
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                fileAdapter.refresh(beanList);//有可能涉及ui刷新
//                            }
//                        });
                    }
                });
        singleChoiceDialog.setNegativeButton("升序",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<FileBean> localList = new ArrayList<>();
                        switch (sortOrder) {
                            case -1:
                                break;
                            case 0:
                                Collections.sort( localBeanList , FileUtil.comparatorNameAsc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            case 1:
                                Collections.sort( localBeanList , FileUtil.comparatorSizeAsc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            case 2:
                                Collections.sort( localBeanList , FileUtil.comparatorTypeAsc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            case 3:
                                Collections.sort( localBeanList , FileUtil.comparatorDateAsc );
                                if(!localBeanList.isEmpty()){
                                    Iterator it = localBeanList.iterator();
                                    while(it.hasNext()) {
                                        localList.add((FileBean) it.next());
                                        FileBean lineBean = new FileBean();
                                        lineBean.setHolderType( 1 );
                                        localList.add( lineBean );
                                    }
                                }
                                beanList = localList;
                                break;
                            default: break;
                        }
//                        Toast.makeText(LocalFileActivity.this, "你选择了升序"+(sortOrder+1), Toast.LENGTH_SHORT).show();
                        sortOrder = -1;
                        fileAdapter.refresh(beanList);//有可能涉及ui刷新
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                fileAdapter.refresh(beanList);//有可能涉及ui刷新
//                            }
//                        });
                    }
                });
        singleChoiceDialog.show();
    }

    public void browsePath(){
        //根据点击的不同存储地址显示文件列表
        Type = getIntent().getIntExtra("Type",0);
        List<FileBean> localList = new ArrayList<>();
        switch (Type){
            case 0:
                Log.e(TAG, "ERROR" );
                Toast.makeText(this, "抱歉，程序异常请重试或重启！", Toast.LENGTH_LONG).show();
                finish();
                break;
            case TYPE_MUSIC:
                title.setText("音乐");
                localBeanList = FileUtil.getMusics();
                break;
            case TYPE_IMAGE:
                title.setText("图片");
                localBeanList = FileUtil.getPhotos();
                break;
            case TYPE_TXT:
                title.setText("文档>");
                localBeanList = FileUtil.getTexts();
                break;
            case TYPE_VIDEO:
                title.setText("视频");
                localBeanList = FileUtil.getVideos();
                break;
            case TYPE_APK:
                title.setText("安装包");
                localBeanList = FileUtil.getApks();
                break;
            case TYPE_ZIP:
                title.setText("压缩包");
                localBeanList = FileUtil.getZips();
                break;
            case SEARCH_FILE:
                title.setText("搜索结果");
                new SearchFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , "") ;
                break;
            default:

        }
        if(Type != SEARCH_FILE){
            if(progressBar.getVisibility() == View.VISIBLE){
                progressBar.setVisibility(View.GONE);
                bodyLayout.setVisibility(View.VISIBLE);
            }
            if(!localBeanList.isEmpty()){
                //在每一项数据后面加分割线对象
                Iterator it = localBeanList.iterator();
                while(it.hasNext()) {
                    localList.add((FileBean) it.next());
                    FileBean lineBean = new FileBean();
                    lineBean.setHolderType( 1 );
                    localList.add( lineBean );
                }
            }
            if (localList.isEmpty()){
                empty_rel.setVisibility(View.VISIBLE);
            }else {
                empty_rel.setVisibility(View.GONE);
            }
            beanList = localList;
            fileAdapter.refresh(beanList);
        }

    }

    class SearchFile extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            List<FileBean> localList = new ArrayList<>();
            String fileInfo = getIntent().getStringExtra("Info").trim();
            if(fileInfo != null && fileInfo != ""){
                localBeanList = FileUtil.searchKeyWord(ClassifyFileActivity.this, fileInfo);
                if(!localBeanList.isEmpty()){
                    //在每一项数据后面加分割线对象
                    Iterator it = localBeanList.iterator();
                    while(it.hasNext()) {
                        localList.add((FileBean) it.next());
                        FileBean lineBean = new FileBean();
                        lineBean.setHolderType( 1 );
                        localList.add( lineBean );
                    }
                }
                beanList = localList;
            }else {
                beanList = null;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            if(progressBar.getVisibility() == View.VISIBLE){
//                Log.e(TAG, "显示的111111");
                progressBar.setVisibility(View.GONE);
                bodyLayout.setVisibility(View.VISIBLE);
            }
            if (beanList.isEmpty()){
                empty_rel.setVisibility(View.VISIBLE);
            }else {
                empty_rel.setVisibility(View.GONE);
            }
            fileAdapter.refresh(beanList);
        }
    }

}
