package com.jjh.filemanager.adapter;

import android.view.View;
import android.widget.TextView;

import com.jjh.filemanager.R;
import com.jjh.filemanager.adapter.base.RecyclerViewAdapter;
import com.jjh.filemanager.adapter.base.RecyclerViewHolder;
import com.jjh.filemanager.bean.TitlePath;

/**
 *
 */

public class TitleHolder extends RecyclerViewHolder<TitleHolder> {

    TextView textView ;

    public TitleHolder(View itemView) {
        super(itemView);

        textView = (TextView) itemView.findViewById(R.id.title_Name );
    }

    @Override
    public void onBindViewHolder(TitleHolder lineHolder, RecyclerViewAdapter adapter, int position) {
        TitlePath titlePath = (TitlePath) adapter.getItem( position );
        lineHolder.textView.setText( titlePath.getNameState() );
    }
}
