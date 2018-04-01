package com.jjh.filemanager.adapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.jjh.filemanager.FileUtil;
import com.jjh.filemanager.R;
import com.jjh.filemanager.adapter.base.RecyclerViewAdapter;
import com.jjh.filemanager.adapter.base.RecyclerViewHolder;
import com.jjh.filemanager.bean.FileBean;
import com.jjh.filemanager.bean.FileType;

import java.io.File;

/**
 * Created by ${zhaoyanjun} on 2017/1/12.
 */

public class FileHolder extends RecyclerViewHolder<FileHolder> {

    ImageView fileIcon;
    TextView fileName;
    TextView fileChildCount;
    TextView fileSize;
    ImageView dir_enter_image;
    TextView fileDate;

    public FileHolder(View view) {
        super(view);
        fileIcon = (ImageView) view.findViewById(R.id.fileIcon);
        fileName = (TextView) view.findViewById(R.id.fileName);
        fileChildCount = (TextView) view.findViewById(R.id.fileChildCount);
        fileSize = (TextView) view.findViewById(R.id.fileSize);
        dir_enter_image = (ImageView) view.findViewById(R.id.dir_enter_image);
        fileDate = (TextView) view.findViewById(R.id.fileDate);
    }

    @Override
    public void onBindViewHolder(final FileHolder fileHolder, RecyclerViewAdapter adapter, int position) {
        FileBean fileBean = (FileBean) adapter.getItem(position);
        fileHolder.fileName.setText(fileBean.getName());

        FileType fileType = fileBean.getFileType();
        if (fileType == FileType.directory) {
            fileHolder.fileChildCount.setVisibility(View.VISIBLE);
            fileHolder.fileChildCount.setText("文件: " +  fileBean.getSonFileCount() + ", 文件夹: " +fileBean.getSonFolderCount()  );

            fileHolder.fileSize.setVisibility(View.GONE);
            fileHolder.dir_enter_image.setVisibility(View.VISIBLE);
            fileHolder.fileDate.setVisibility(View.GONE);
        } else {
            fileHolder.fileChildCount.setVisibility(View.GONE);

            fileHolder.fileSize.setVisibility(View.VISIBLE);
            fileHolder.fileSize.setText(FileUtil.sizeToChange(fileBean.getSize()));
            fileHolder.dir_enter_image.setVisibility(View.GONE);
            fileHolder.fileDate.setVisibility(View.VISIBLE);
            fileHolder.fileDate.setText(fileBean.getDate());
        }

        //设置图标
        if (fileType == FileType.directory) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_dir);
        } else if (fileType == FileType.music) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_music);
        } else if (fileType == FileType.video) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_video);
        } else if (fileType == FileType.txt) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_txt);
        } else if (fileType == FileType.zip) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_zip);
        } else if (fileType == FileType.image) {
            Glide.with(fileHolder.itemView.getContext()).load(new File(fileBean.getPath())).into(fileHolder.fileIcon);
        } else if (fileType == FileType.apk) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_apk);
        } else if (fileType == FileType.doc) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_doc);
        } else if (fileType == FileType.pdf) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_pdf);
        } else {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_other);
        }
    }
}
