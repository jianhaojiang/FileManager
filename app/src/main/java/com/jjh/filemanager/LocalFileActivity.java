package com.jjh.filemanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jjh.filemanager.Util.FileUtil;
import com.jjh.filemanager.adapter.FileHolder;
import com.jjh.filemanager.adapter.FileAdapter;
import com.jjh.filemanager.adapter.TitleAdapter;
import com.jjh.filemanager.adapter.base.RecyclerViewAdapter;
import com.jjh.filemanager.bean.EncryptedItem;
import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.bean.TitlePath;
import com.jjh.filemanager.bean.FileType;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class LocalFileActivity extends AppCompatActivity {

    private static final String TAG="LocalFileActivity";
    private RecyclerView title_recycler_view ;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private RelativeLayout bodyRelativeLayout;
    private List<FileBean> beanList = new ArrayList<>();
    private File rootFile ;
    private LinearLayout empty_rel ;
    private String Path ;
    private int pathFlag;
    private TitleAdapter titleAdapter ;
    private ProgressBar progressBar;
    private int sortOrder;
    private final int SEARCH_FILES = 7;
    private FileBean longClickFileBean;
    private HashMap pathMap = new HashMap();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_file);
//        LinearLayout l = (LinearLayout)findViewById(R.id.sss);
//        l.setVisibility(View.GONE);

        //初始化页面和监听
        initView();
        //根据点击路径浏览文件
        browsePath();

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
                new AlertDialog.Builder(LocalFileActivity.this);
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
        final EditText editText = new EditText(LocalFileActivity.this);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(LocalFileActivity.this);
        inputDialog.setTitle("搜索文件").setView(editText);
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
                        String fileInfo = editText.getText().toString();
                        Intent searchIntent = new Intent(LocalFileActivity.this, ClassifyFileActivity.class);
                        searchIntent.putExtra("Type", SEARCH_FILES);
                        searchIntent.putExtra("Info", fileInfo);
                        startActivity(searchIntent);
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
                new AlertDialog.Builder(LocalFileActivity.this);
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
//                        Toast.makeText(LocalFileActivity.this, "你选择了", Toast.LENGTH_SHORT).show();
                        switch (sortOrder) {
                            case -1:
                                break;
                            case 0:
//                                Toast.makeText(LocalFileActivity.this, "你选择了0", Toast.LENGTH_SHORT).show();
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

    public void initView(){
        // 无标题,在activity继承AppCompatActivity的情况下好像无效，会闪退
        //supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置Title
        title_recycler_view = (RecyclerView) findViewById(R.id.title_recycler_view);
        //文件为空时显示该界面，不为空时隐藏
        empty_rel = (LinearLayout)findViewById( R.id.empty_rel );
        bodyRelativeLayout = (RelativeLayout)findViewById(R.id.body);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        //表示水平布局，flase表示从左往右，LinearLayoutManager也可以将布局设置为网格布局
        title_recycler_view.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL , false ));
        titleAdapter = new TitleAdapter( LocalFileActivity.this , new ArrayList<TitlePath>() ) ;
        title_recycler_view.setAdapter( titleAdapter );
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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
                if ( viewHolder instanceof FileHolder ){
                    FileBean file = beanList.get(position);
                    FileType fileType = file.getFileType() ;
                    try {
                        if ( fileType == FileType.directory) {
                            //111666
                            if(pathMap.containsKey(file.getPath())){
                                beanList = (List<FileBean>) pathMap.get(file.getPath());
                                fileAdapter.refresh(beanList);
                                if ( beanList.size() > 0  ){
                                    empty_rel.setVisibility( View.GONE );
                                }else {
                                    empty_rel.setVisibility( View.VISIBLE );
                                }
                            }else{
                                getFile(file.getPath());
                            }
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
                    }catch (Exception e){
                        Log.e(TAG, "呀！打开出现问题了");
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

        titleAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                TitlePath titlePath = (TitlePath) titleAdapter.getItem( position );
                //111666
                if(pathMap.containsKey(titlePath.getPath())){
                    beanList = (List<FileBean>) pathMap.get(titlePath.getPath());
                    fileAdapter.refresh(beanList);
                    if ( beanList.size() > 0  ){
                        empty_rel.setVisibility( View.GONE );
                    }else {
                        empty_rel.setVisibility( View.VISIBLE );
                    }
                }else{
                    getFile(titlePath.getPath());
                }
                int count = titleAdapter.getItemCount() ;
                int removeCount = count - position - 1 ;
                for ( int i = 0 ; i < removeCount ; i++ ){
                    titleAdapter.removeLast();
                }
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
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

        } else if (item.getItemId() == R.id.menu_attribute) {
            showAttributeDialog();
//            Toast.makeText(this, "属性被选择了", Toast.LENGTH_SHORT).show();
        }else if (item.getItemId() == R.id.menu_share) {
            FileUtil.sendFile( LocalFileActivity.this , new File( longClickFileBean.getPath() ) );
        }
        return super.onContextItemSelected(item);
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
        //111666
        if(pathMap.containsKey(Path)){
            beanList = (List<FileBean>) pathMap.get(Path);
            fileAdapter.refresh(beanList);
            if ( beanList.size() > 0  ){
                empty_rel.setVisibility( View.GONE );
            }else {
                empty_rel.setVisibility( View.VISIBLE );
            }
        }else{
            getFile(Path);
        }
    }

    public void getFile(String path ) {
//        Log.d(TAG, "getFile: 66666666666666");
        if(progressBar.getVisibility() == View.GONE){
//            Log.d("aaaaaaa", "progressBar状态不可见: 66666666666666");
            bodyRelativeLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
//            empty_rel.setVisibility( View.GONE );
//            recyclerView.setVisibility(View.GONE);
        }
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
                    List<File> fileList = new ArrayList<>();
                    Collections.addAll( fileList , filesArray ) ;  //把数组转化成list
                    Collections.sort( fileList , FileUtil.comparator );  //自定义排序
                    for (File f : fileList ) {
                       if (f.isHidden()) continue;
                        FileBean fileBean = new FileBean();
                        fileBean.setName(f.getName());
                        fileBean.setPath(f.getAbsolutePath());
//                        Log.d(TAG, "doInBackground: " +  File.separator  +"路径" + f.getAbsolutePath());
                        fileBean.setFileType( FileUtil.getFileType( f ));
                        fileBean.setChildCount( FileUtil.getFileChildCount( f ));
                        fileBean.setSonFolderCount(FileUtil.getSonFloderCount(f));
                        fileBean.setSonFileCount(FileUtil.getSonFileCount(f));
                        fileBean.setSize( f.length() );
                        fileBean.setDate(FileUtil.getFileLastModifiedTime(f));
                        fileBeenList.add(fileBean);
                    }
                }
            }
            beanList = fileBeenList;
            pathMap.put(file.getPath(),beanList);
            return fileBeenList;
        }

        @Override
        protected void onPostExecute(Object o) {
//            Log.d(TAG, "onPostExecute: 66666666666666");
            //刷新数据并控制进度条显示
            fileAdapter.refresh(beanList);
            if(progressBar.getVisibility() == View.VISIBLE){
//                Log.d(TAG, "progressBar状态显示: 66666666666666");
                progressBar.setVisibility(View.GONE);
                bodyRelativeLayout.setVisibility(View.VISIBLE);
            }
            if ( beanList.size() > 0  ){
                empty_rel.setVisibility( View.GONE );
            }else {
                empty_rel.setVisibility( View.VISIBLE );
            }
        }
    }


    //加密方法
    private boolean encryptionFile(FileBean fileBean){
        boolean encryptionState;
        try {
            String oldName = fileBean.getName();
            String privateName = "." + FileUtil.getFileNameMD5(oldName);
            String oldPath = fileBean.getPath();
            String privatePath = oldPath.substring(0,oldPath.lastIndexOf(File.separator)+1) + privateName;
            //将加密信息放进sqlite数据库
            EncryptedItem encryptedItem = new EncryptedItem();
            encryptedItem.setOldName(oldName);
            encryptedItem.setPrivateName(privateName);
            encryptedItem.setOldPath(oldPath);
            encryptedItem.setPrivatePath(privatePath);
            encryptionState = encryptedItem.save();
            if(encryptionState){
                FileUtil.encrypt(oldPath);
                new File(oldPath).renameTo(new File(privatePath));
                beanList.remove(fileBean);
//                Log.e(TAG, "getPrivateFiles: 存入数据库成功啦！" + oldName);
                //因为文件信息是external.db数据库读取的，实际删除文件后还需要去更新数据库
                FileUtil.updateExternalDB(oldPath, LocalFileActivity.this);
                FileUtil.updateExternalDB(privatePath, LocalFileActivity.this);
            }
        }catch (Exception e){
            e.printStackTrace();
            encryptionState = false;
        }
        return encryptionState;
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
                String upTitlePath =  titlePathList.get(titlePathList.size() - 1 ).getPath();
                //111666
                if(pathMap.containsKey(upTitlePath)){
                    beanList = (List<FileBean>) pathMap.get(upTitlePath);
                    fileAdapter.refresh(beanList);
                    if ( beanList.size() > 0  ){
                        empty_rel.setVisibility( View.GONE );
                    }else {
                        empty_rel.setVisibility( View.VISIBLE );
                    }
                }else{
                    getFile(upTitlePath);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
