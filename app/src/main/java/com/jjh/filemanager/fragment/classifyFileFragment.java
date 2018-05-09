package com.jjh.filemanager.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjh.filemanager.FileUtil;
import com.jjh.filemanager.R;
import com.jjh.filemanager.fragment.adapter.FragAdapter;

/**
 * Created by jjh on 2018/1/23.
 */

public class classifyFileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classify_file, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new RefreshTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR , "") ;
    }

    class RefreshTask extends AsyncTask {
        private TextView classifyMusicNumber;
        private TextView classifyPhotoNumber;
        private TextView classifyTextNumber;
        private TextView classifyVideoNumber;
        private TextView classifyApkNumber;
        private TextView classifyZipNumber;
        private int musicTotal;
        private int photoTotal;
        private int textTotal;
        private int videoTotal;
        private int apkTotal;
        private int zipTotal;
        private ProgressBar progressBar;

        @Override
        protected Object doInBackground(Object[] params) {
//            try{
//                Thread.sleep(100);//等待onCreate结束再执行刷新界面的作用
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            //得到各种类别文件数目
            musicTotal = FileUtil.getAllMusicNumber(getContext());
            photoTotal = FileUtil.getAllPhotoNumber(getContext());
            textTotal  = FileUtil.getAllTextNumber(getContext());
            videoTotal = FileUtil.getAllVideoNumber(getContext());
            apkTotal   = FileUtil.getAllApkNumber(getContext());
            zipTotal   = FileUtil.getAllZipNumber(getContext());

            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            classifyMusicNumber = (TextView) getActivity().findViewById(R.id.classify_music_number);
            classifyPhotoNumber = (TextView) getActivity().findViewById(R.id.classify_image_number);
            classifyTextNumber  = (TextView) getActivity().findViewById(R.id.classify_txt_number);
            classifyVideoNumber = (TextView) getActivity().findViewById(R.id.classify_video_number);
            classifyApkNumber   = (TextView) getActivity().findViewById(R.id.classify_installpackage_number);
            classifyZipNumber   = (TextView) getActivity().findViewById(R.id.classify_zip_number);
            progressBar = (ProgressBar)getActivity().findViewById(R.id.classify_fragment_progress_bar);


//            TextView textView = (TextView)findViewById(R.id.classify_image_number);
//            String number =String.valueOf(photos.size()/2);
//            textView.setText(number);
            classifyMusicNumber.setText(String.valueOf(musicTotal));
            classifyPhotoNumber.setText(String.valueOf(photoTotal));
            classifyTextNumber.setText(String.valueOf(textTotal));
            classifyVideoNumber.setText(String.valueOf(videoTotal));
            classifyApkNumber.setText(String.valueOf(apkTotal));
            classifyZipNumber.setText(String.valueOf(zipTotal));
            if(progressBar.getVisibility() == View.VISIBLE){
                progressBar.setVisibility(View.GONE);
            }
        }
    }


}
