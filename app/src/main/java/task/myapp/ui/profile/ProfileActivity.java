package task.myapp.ui.profile;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.samples.apps.iosched.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.bloco.faker.Faker;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import task.myapp.AppClass;
import task.myapp.R;
import task.myapp.navigation.NavigationModel;
import task.myapp.ui.explore.inner.InnerData;
import task.myapp.widgets.BadgedBottomNavigationView;
import task.myapp.widgets.BottomNavigationBehaviour;

public class ProfileActivity extends BaseActivity implements AppClass.FakerReadyListener {

    @BindView(R.id.history_list)
    RecyclerView recView;

    @BindView(R.id.bottom_navigation)
    BadgedBottomNavigationView bottomNavigationView;

    private RecViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelfNavDrawerItem(NavigationModel.NavigationItemEnum.PROFILE);
        setNavigationTitleId(R.string.title_activity_profile);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setUpRecView();
        setUpBottomNav();
        ((AppClass) getApplication()).addListener(this);
    }

    private void setUpBottomNav() {

        BottomNavigationBehaviour<BadgedBottomNavigationView> behaviour = new BottomNavigationBehaviour<BadgedBottomNavigationView>(this);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        params.setBehavior(behaviour);
    }

    private void setUpRecView() {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
        recView.setLayoutManager(layoutManager);
        recView.setNestedScrollingEnabled(false);
        recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        adapter = new RecViewAdapter(new ArrayList<>());
        recView.addItemDecoration(new GridLayoutItemDecoration(2, 1, true));
        recView.setAdapter(adapter);
    }

    @Override
    public void onFakerReady(Faker faker) {
        Single.create((SingleOnSubscribe<List<InnerData>>) e -> {
            final List<InnerData> innerData = new ArrayList<>();
            for (int j = 0; j < 18 && !e.isDisposed(); j++) {
                innerData.add(createInnerData(faker));
            }

            if (!e.isDisposed()) {
                e.onSuccess(innerData);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> adapter.updateData(data));
    }

    private InnerData createInnerData(Faker faker) {
        return new InnerData(
                faker.book.title(),
                faker.name.name(),
                faker.address.city() + ", " + faker.address.stateAbbr(),
                faker.avatar.image(faker.internet.email(), "150x150", "jpg", "set1", "bg1"),
                faker.number.between(20, 50)
        );
    }

    private class RecViewAdapter extends RecyclerView.Adapter<RecViewAdapter.ViewHolder> {

        private List<InnerData> data;

        public RecViewAdapter(List<InnerData> data) {
            this.data = data;
        }

        private void updateData(List<InnerData> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = null;
            rootView = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.history_list_items, parent, false);
            return new ViewHolder(rootView);
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            bindRowItems(holder, position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        private void bindRowItems(ViewHolder holder, int position) {
            holder.setContent(data.get(position));
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            public final TextView mHeader;
            public final TextView mName;
            public final TextView mAddress;
            public final ImageView mAvatar;
            public final View mAvatarBorder;
            public final View mLine;
            private final View mInnerLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                mInnerLayout = ((ViewGroup) itemView).getChildAt(0);

                mHeader = (TextView) itemView.findViewById(R.id.tv_header);
                mName = (TextView) itemView.findViewById(R.id.tv_name);
                mAddress = (TextView) itemView.findViewById(R.id.tv_address);
                mAvatar = (ImageView) itemView.findViewById(R.id.avatar);
                mAvatarBorder = itemView.findViewById(R.id.avatar_border);
                mLine = itemView.findViewById(R.id.line);

                mInnerLayout.setOnClickListener(view -> {
                });
            }

            void setContent(InnerData data) {
                mHeader.setText(data.title);
                mName.setText(String.format("%s %s", data.name, itemView.getContext().getString(R.string.answer_low)));
                mAddress.setText(String.format("%s %s · %s",
                        data.age, mAddress.getContext().getString(R.string.years), data.address));

                Glide.with(itemView.getContext())
                        .load(data.avatarUrl)
                        .placeholder(R.drawable.avatar_placeholder)
                        .bitmapTransform(new CropCircleTransformation(itemView.getContext()))
                        .into(mAvatar);
            }
        }
    }
}

