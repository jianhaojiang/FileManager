package com.jjh.filemanager.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjh.filemanager.Util.FileUtil;
import com.jjh.filemanager.R;
import com.jjh.filemanager.bean.EncryptedItem;

import org.litepal.crud.DataSupport;

/**
 *
 */

public class localFileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_file, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , "") ;
    }

    class RefreshTask extends AsyncTask {
        private RelativeLayout externalStorage;
        private TextView insideFileSize;
        private TextView externalFileSize;
        private TextView privateFileSize;
        private String insideInfo;
        private String externalInfo;
        String rootPath;
        String totalPath;
        String SDPath;

        @Override
        protected Object doInBackground(Object[] params) {

            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();//手机自带外部存储根目录
            totalPath = Environment.getDataDirectory().getPath();  //手机所有文件根目录
            SDPath = FileUtil.getExtendedMemoryPath(getContext());
            if(totalPath != null){
                insideInfo = "总共：" + FileUtil.getAllSpace(getContext(), totalPath) + "，可用："
                        + FileUtil.getAvailSpace(getContext(), totalPath) ;
            }else {
                insideInfo = "总共：0GB，可用：0GB";
            }
            if(SDPath != null){
                externalInfo = "总共：" + FileUtil.getAllSpace(getContext(), SDPath) + "，可用："
                        + FileUtil.getAvailSpace(getContext(), SDPath) ;
            }else {
                externalInfo = "总共：0GB，可用：0GB";
            }
            return SDPath;
        }

        @Override
        protected void onPostExecute(Object o) {
            externalStorage = (RelativeLayout)getActivity().findViewById(R.id.external_storage);
            insideFileSize   = (TextView)getActivity().findViewById(R.id.insideFileSize);
            externalFileSize = (TextView)getActivity().findViewById(R.id.externalFileSize);
            privateFileSize = (TextView)getActivity().findViewById(R.id.privateFileSize);
//            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                //检测手机自带的外部存储是否装载
//                externalStorage.setVisibility(View.VISIBLE);
//            }else {
//                externalStorage.setVisibility(View.GONE);
//            }

            //如果没有外置SD卡则不显示SD卡选项
            if(SDPath == null){
                externalStorage.setVisibility(View.GONE);
            }else {
                externalStorage.setVisibility(View.VISIBLE);
            }
            insideFileSize.setText(insideInfo);
            externalFileSize.setText(externalInfo);
            //显示已加密文件个数
            privateFileSize.setText(DataSupport.findAll(EncryptedItem.class).size() + "项");
        }
    }

}
