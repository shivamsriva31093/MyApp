package task.myapp.ui.explore.outer;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ramotion.garlandview.TailAdapter;


import java.util.List;

import task.myapp.R;
import task.myapp.ui.explore.inner.InnerData;

public class OuterAdapter extends TailAdapter<OuterItem> {

    private final int POOL_SIZE = 16;

    private final List<List<InnerData>> mData;
    private final RecyclerView.RecycledViewPool mPool;
    private OuterItem.BottomSheetStateListener listener;

    public OuterAdapter(List<List<InnerData>> data, OuterItem.BottomSheetStateListener listener) {
        this.mData = data;
        this.listener = listener;
        mPool = new RecyclerView.RecycledViewPool();
        mPool.setMaxRecycledViews(0, POOL_SIZE);
    }

    @Override
    public OuterItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new OuterItem(view, mPool, listener);
    }

    @Override
    public void onBindViewHolder(OuterItem holder, int position) {
        holder.setContent(mData.get(position));
    }

    @Override
    public void onViewRecycled(OuterItem holder) {
        holder.clearContent();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.outer_item;
    }

}
