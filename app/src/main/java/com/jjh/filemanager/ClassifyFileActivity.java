package com.jjh.filemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
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
import com.jjh.filemanager.bean.TitlePath;
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
    private LinearLayout activityClassify_bottom;
    private List<FileBean> beanList = new ArrayList<>();
    private List<FileBean> privateList = new ArrayList<>();
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
    private final int PRIVATE_FILE   = 8;
    private FileBean longClickFileBean;
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
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
    }


    public void initView(){
        //设置Title
        title = (TextView) findViewById(R.id.activity_classify_title);
        progressBar = (ProgressBar)findViewById(R.id.activity_classify_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        //加载时显示加载条，隐藏body界面和底部菜单界面
        bodyLayout = (RelativeLayout) findViewById(R.id.activity_classify_body);
        bodyLayout.setVisibility(View.GONE);
        activityClassify_bottom = (LinearLayout) findViewById(R.id.activityClassify_bottom);
        activityClassify_bottom.setVisibility(View.GONE);

        //文件为空时显示该界面，不为空时隐藏
        empty_rel = (LinearLayout)findViewById( R.id.activity_classify_empty_rel );
        recyclerView = (RecyclerView) findViewById(R.id.activity_classify_recycler_view);
        //初始化适配器
        fileAdapter = new FileAdapter(this, beanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //添加适配器
        recyclerView.setAdapter(fileAdapter);
        //给recyclerview注册长按监听
        registerForContextMenu(recyclerView);
        fileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if ( viewHolder instanceof FileHolder){
                    FileBean file = beanList.get(position);
                    FileType fileType = file.getFileType() ;
                    try {
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
                    }catch (Exception e){
                        Log.e(TAG, "呀！打开出现问题了" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });



        fileAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
            if ( viewHolder instanceof  FileHolder ){
                longClickFileBean = (FileBean) fileAdapter.getItem( position );
                FileType fileType = longClickFileBean.getFileType() ;
                if ( fileType != null && fileType != FileType.directory ){
                    return false;
                }
            }
            return true;
            }
        });

    }

    public void browsePath(){
        //根据点击的不同存储地址显示文件列表
        Type = getIntent().getIntExtra("Type",0);
        switch (Type){
            case 0:
                Log.e(TAG, "ERROR" );
                Toast.makeText(this, "抱歉，程序异常请重试或重启！", Toast.LENGTH_LONG).show();
                finish();
                break;
            case TYPE_MUSIC:
                title.setText("音乐");
                beanList = FileUtil.getMusics();
                break;
            case TYPE_IMAGE:
                title.setText("图片");
                beanList = FileUtil.getPhotos();
                break;
            case TYPE_TXT:
                title.setText("文档");
                beanList = FileUtil.getTexts();
                break;
            case TYPE_VIDEO:
                title.setText("视频");
                beanList = FileUtil.getVideos();
                break;
            case TYPE_APK:
                title.setText("安装包");
                beanList = FileUtil.getApks();
                break;
            case TYPE_ZIP:
                title.setText("压缩包");
                beanList = FileUtil.getZips();
                break;
            case SEARCH_FILE:
                title.setText("搜索结果");
                new SearchFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , "") ;
                break;
            case PRIVATE_FILE:
                title.setText("私人目录");
                beanList = new ArrayList<FileBean>(privateList);
                break;
            default:

        }
        //不是搜索文件跳转过来的页面时
        if(Type != SEARCH_FILE){
            if(progressBar.getVisibility() == View.VISIBLE){
                progressBar.setVisibility(View.GONE);
                bodyLayout.setVisibility(View.VISIBLE);
                activityClassify_bottom.setVisibility(View.VISIBLE);
            }
            if (beanList.isEmpty()){
                empty_rel.setVisibility(View.VISIBLE);
            }else {
                empty_rel.setVisibility(View.GONE);
            }
            fileAdapter.refresh(beanList);
        }

    }

    //删除文件方法
    private boolean deleteFile(FileBean fileBean){
        try {
            String path = fileBean.getPath();
            new File(path).delete();//因为文件信息是数据库读取的，实际删除文件后还需要去更新数据库
            FileUtil.updateExternalDB(fileBean.getPath(), ClassifyFileActivity.this);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //解密方法
    private boolean decipheringFile(FileBean fileBean){
        try {
            String privateName = fileBean.getPrivateName();
            String path = fileBean.getPath();
            String privatePath = fileBean.getPrivatePath();
            new File(privatePath).renameTo(new File(path));
            //因为文件信息是数据库读取的，实际删除文件后还需要去更新数据库
            FileUtil.updateExternalDB(fileBean.getPrivatePath(), ClassifyFileActivity.this);
            FileUtil.updateExternalDB(fileBean.getPath(), ClassifyFileActivity.this);
            beanList.remove(fileBean);
            privateList.remove(fileBean);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //加密方法
    private boolean encryptionFile(FileBean fileBean){
        try {
            String privateName = FileUtil.getFileMD5(new File(fileBean.getPath()));
            String path = fileBean.getPath();
            String privatePath = path.substring(0,path.lastIndexOf(File.separator)+1) + "." +  privateName;
            new File(path).renameTo(new File(privatePath));
            beanList.remove(fileBean);
            fileBean.setPrivateName(privateName);
            fileBean.setPrivatePath(privatePath);
            privateList.add(fileBean);
            //因为文件信息是数据库读取的，实际删除文件后还需要去更新数据库
            FileUtil.updateExternalDB(fileBean.getPath(), ClassifyFileActivity.this);
            FileUtil.updateExternalDB(fileBean.getPrivatePath(), ClassifyFileActivity.this);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
        if(Type == PRIVATE_FILE) {
            menu.findItem(R.id.menu_deciphering).setVisible(true);
        }
    }



    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {

        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_encryption) {
//            Log.e(TAG, "onContextItemSelected: " + new File(longClickFileBean.getPath()).getPath() );
            Boolean state = encryptionFile(longClickFileBean);
            if(state){
                Toast.makeText(this, "加密成功！", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "加密失败了！", Toast.LENGTH_SHORT).show();
            }
            fileAdapter.refresh(beanList);

        }else if (item.getItemId() == R.id.menu_deciphering) {
            Boolean state = decipheringFile(longClickFileBean);
            if(state){
                Toast.makeText(this, "已解密到原路径！", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "解密失败了！", Toast.LENGTH_SHORT).show();
            }
            fileAdapter.refresh(beanList);
        } else if (item.getItemId() == R.id.menu_attribute) {
            showAttributeDialog();
//            Toast.makeText(this, "属性被选择了", Toast.LENGTH_SHORT).show();
        }else if (item.getItemId() == R.id.menu_share) {
            FileUtil.sendFile( ClassifyFileActivity.this , new File( longClickFileBean.getPath() ) );
        }
        return super.onContextItemSelected(item);
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

    private void showAttributeDialog(){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(ClassifyFileActivity.this);
        normalDialog.setMessage(
                "文件名称：" + longClickFileBean.getName() + "\n" +
                "文件大小：" + FileUtil.sizeToChange(longClickFileBean.getSize()) + "\n" +
                "修改时间：" + longClickFileBean.getDate() + "\n" +
                "文件位置：\n" + longClickFileBean.getPath());
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
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
                        String fileInfo = editText.getText().toString().trim();
                        if(!beanList.isEmpty() && fileInfo != null && !"".equals(fileInfo) ){
                            Iterator it = beanList.iterator();
                            FileBean localFileBean = new FileBean();
                            while(it.hasNext()) {
                                localFileBean = (FileBean)it.next();
                                if(localFileBean.getName().contains(fileInfo.toUpperCase()) ||
                                        localFileBean.getName().contains(fileInfo.toLowerCase())){
                                    localList.add(localFileBean);
                                }
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
                                Collections.sort( beanList , FileUtil.comparatorNameDesc );
                                break;
                            case 1:
                                Collections.sort( beanList , FileUtil.comparatorSizeDesc );
                                break;
                            case 2:
                                Collections.sort( beanList , FileUtil.comparatorTypeDesc );
                                break;
                            case 3:
                                Collections.sort( beanList , FileUtil.comparatorDateDesc );
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
                                Collections.sort( beanList , FileUtil.comparatorNameAsc );
                                break;
                            case 1:
                                Collections.sort( beanList , FileUtil.comparatorSizeAsc );
                                break;
                            case 2:
                                Collections.sort( beanList , FileUtil.comparatorTypeAsc );
                                break;
                            case 3:
                                Collections.sort( beanList , FileUtil.comparatorDateAsc );
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


    class SearchFile extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            String fileInfo = getIntent().getStringExtra("Info").trim();
            if(fileInfo != null && !"".equals(fileInfo)){
                beanList = FileUtil.searchKeyWord(ClassifyFileActivity.this, fileInfo);
            }else {
                beanList = new ArrayList<>();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            //文件搜索完毕时
            if(progressBar.getVisibility() == View.VISIBLE){
                progressBar.setVisibility(View.GONE);
                bodyLayout.setVisibility(View.VISIBLE);
                activityClassify_bottom.setVisibility(View.VISIBLE);
            }
            if (beanList.isEmpty()){
                empty_rel.setVisibility(View.VISIBLE);
            }else {
                empty_rel.setVisibility(View.GONE);
            }
            fileAdapter.refresh(beanList);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            if(Type == PRIVATE_FILE){
                Intent privateIntent = new Intent(ClassifyFileActivity.this, MainActivity.class);
                startActivity(privateIntent);
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
