package file.mec.com.mecfilelist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileListViewHolder> {

    List<FileDetail> mList;
    Context context;

    public FileListAdapter(Context context, List<FileDetail> mList) {
        this.context = context;
        this.mList = mList;
    }

    @Override
    public FileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileListViewHolder(View.inflate(parent.getContext(), R.layout.item_file_list, null));
    }

    @Override
    public void onBindViewHolder(FileListViewHolder holder, final int position) {
        final FileDetail bean = mList.get(position);
        holder.iv_file_type.setImageResource(bean.icon);
        holder.tv_file_name.setText(bean.fileName);
        holder.rl_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFileListListener.onClickItem(bean, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public FileListListener mFileListListener;

    public void setmFileListListener(FileListListener mFileListListener) {
        this.mFileListListener = mFileListListener;
    }

    public void setData( List<FileDetail> data) {
        this.mList = data;
        notifyDataSetChanged();
    }

    public interface FileListListener {
        void onClickItem(FileDetail file, int position);
    }

    class FileListViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_file_type;
        TextView tv_file_name;
        RelativeLayout rl_main;

        public FileListViewHolder(View itemView) {
            super(itemView);
            iv_file_type = itemView.findViewById(R.id.iv_file_type);
            tv_file_name = itemView.findViewById(R.id.tv_file_name);
            rl_main = itemView.findViewById(R.id.rl_main);
        }
    }
}
