package com.jjh.filemanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jjh.filemanager.R;
import com.jjh.filemanager.adapter.base.RecyclerViewAdapter;
import com.jjh.filemanager.bean.FileBean;
import java.util.List;

/**
 *
 */

public class FileAdapter extends RecyclerViewAdapter {

    private Context context;
    private List<FileBean> list ;
    private LayoutInflater mLayoutInflater ;

    public FileAdapter(Context context, List<FileBean> list) {
        this.context = context;
        this.list = list;
        mLayoutInflater = LayoutInflater.from( context ) ;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view ;
            view = mLayoutInflater.inflate(R.layout.list_item_file, parent, false) ;
            return new FileHolder( view );
    }

    @Override
    public void onBindViewHolders(final RecyclerView.ViewHolder  holder,
                                  final int position) {
        if ( holder instanceof  FileHolder ){
            FileHolder fileHolder = (FileHolder) holder;
            fileHolder.onBindViewHolder( fileHolder , this , position );
        }
    }

    @Override
    public Object getAdapterData() {
        return list ;
    }

    @Override
    public Object getItem(int positon) {
        return list.get( positon );
    }



    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public void refresh( List<FileBean> list ){
        this.list = list;
        notifyDataSetChanged();
    }
}
