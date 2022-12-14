package com.ajitmaurya.galleryimagepicker.activity.multi;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.ajitmaurya.galleryimagepicker.ImagePicker;
import com.ajitmaurya.galleryimagepicker.R;
import com.ajitmaurya.galleryimagepicker.activity.PBaseLoaderFragment;
import com.ajitmaurya.galleryimagepicker.activity.PickerActivityManager;
import com.ajitmaurya.galleryimagepicker.activity.preview.MultiImagePreviewActivity;
import com.ajitmaurya.galleryimagepicker.adapter.PickerFolderAdapter;
import com.ajitmaurya.galleryimagepicker.bean.PickerItemDisableCode;
import com.ajitmaurya.galleryimagepicker.data.IReloadExecutor;
import com.ajitmaurya.galleryimagepicker.views.PickerUiConfig;
import com.ajitmaurya.galleryimagepicker.helper.PickerErrorExecutor;
import com.ajitmaurya.galleryimagepicker.adapter.PickerItemAdapter;
import com.ajitmaurya.galleryimagepicker.bean.selectconfig.BaseSelectConfig;
import com.ajitmaurya.galleryimagepicker.bean.ImageItem;
import com.ajitmaurya.galleryimagepicker.bean.PickerError;
import com.ajitmaurya.galleryimagepicker.bean.SelectMode;
import com.ajitmaurya.galleryimagepicker.bean.ImageSet;
import com.ajitmaurya.galleryimagepicker.bean.selectconfig.MultiSelectConfig;
import com.ajitmaurya.galleryimagepicker.data.OnImagePickCompleteListener;
import com.ajitmaurya.galleryimagepicker.presenter.IPickerPresenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.ajitmaurya.galleryimagepicker.activity.multi.MultiImagePickerActivity.INTENT_KEY_SELECT_CONFIG;
import static com.ajitmaurya.galleryimagepicker.activity.multi.MultiImagePickerActivity.INTENT_KEY_PRESENTER;

/**
 * Description: ?????????
 * <p>
 * Author: peixing.yang
 * Date: 2019/2/21
 * ???????????? ???https://github.com/yangpeixing/YImagePicker/wiki/Documentation_3.x
 */
public class MultiImagePickerFragment extends PBaseLoaderFragment implements View.OnClickListener,
        PickerItemAdapter.OnActionResult, IReloadExecutor {
    private List<ImageSet> imageSets = new ArrayList<>();
    private ArrayList<ImageItem> imageItems = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private View v_masker;
    private TextView mTvTime;
    private PickerFolderAdapter mImageSetAdapter;
    private RecyclerView mFolderListRecyclerView;
    private PickerItemAdapter mAdapter;
    private ImageSet currentImageSet;
    private FrameLayout titleBarContainer;
    private FrameLayout bottomBarContainer;
    private MultiSelectConfig selectConfig;
    private IPickerPresenter presenter;
    private PickerUiConfig uiConfig;
    private FragmentActivity mContext;
    private GridLayoutManager layoutManager;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.picker_activity_multipick, container, false);
        return view;
    }

    /**
     * ??????????????????????????????
     */
    private boolean isIntentDataValid() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            selectConfig = (MultiSelectConfig) bundle.getSerializable(INTENT_KEY_SELECT_CONFIG);
            presenter = (IPickerPresenter) bundle.getSerializable(INTENT_KEY_PRESENTER);
            if (presenter == null) {
                PickerErrorExecutor.executeError(onImagePickCompleteListener,
                        PickerError.PRESENTER_NOT_FOUND.getCode());
                return false;
            }
            if (selectConfig == null) {
                PickerErrorExecutor.executeError(onImagePickCompleteListener,
                        PickerError.SELECT_CONFIG_NOT_FOUND.getCode());
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        if (isIntentDataValid()) {
            ImagePicker.isOriginalImage = selectConfig.isDefaultOriginal();
            uiConfig = presenter.getUiConfig(getWeakActivity());
            setStatusBar();
            findView();
            if (selectConfig.getLastImageList() != null) {
                selectList.addAll(selectConfig.getLastImageList());
            }
            loadMediaSets();
            refreshCompleteState();
        }
    }

    private OnImagePickCompleteListener onImagePickCompleteListener;

    /**
     * ?????????????????????????????????
     *
     * @param onImagePickCompleteListener ????????????
     */
    public void setOnImagePickCompleteListener(@NonNull OnImagePickCompleteListener onImagePickCompleteListener) {
        this.onImagePickCompleteListener = onImagePickCompleteListener;
    }

    /**
     * ???????????????
     */
    private void findView() {
        v_masker = view.findViewById(R.id.v_masker);
        mRecyclerView = view.findViewById(R.id.mRecyclerView);
        mFolderListRecyclerView = view.findViewById(R.id.mSetRecyclerView);
        mTvTime = view.findViewById(R.id.tv_time);
        mTvTime.setVisibility(View.GONE);
        titleBarContainer = view.findViewById(R.id.titleBarContainer);
        bottomBarContainer = view.findViewById(R.id.bottomBarContainer);
        initAdapters();
        initUI();
        setListener();
        refreshCompleteState();
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (mTvTime.getVisibility() == View.VISIBLE) {
                    mTvTime.setVisibility(View.GONE);
                    mTvTime.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.picker_fade_out));
                }
            } else {
                if (mTvTime.getVisibility() == View.GONE) {
                    mTvTime.setVisibility(View.VISIBLE);
                    mTvTime.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.picker_fade_in));
                }
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (imageItems != null)
                try {
                    mTvTime.setText(imageItems.get(layoutManager.findFirstVisibleItemPosition()).getTimeFormat());
                } catch (Exception ignored) {

                }
        }
    };

    /**
     * ?????????UI??????
     */
    private void initUI() {
        mRecyclerView.setBackgroundColor(uiConfig.getPickerBackgroundColor());
        titleBar = inflateControllerView(titleBarContainer, true, uiConfig);
        bottomBar = inflateControllerView(bottomBarContainer, false, uiConfig);
        setFolderListHeight(mFolderListRecyclerView, v_masker, false);
    }

    /**
     * ???????????????
     */
    private void setListener() {
        v_masker.setOnClickListener(this);
        mRecyclerView.addOnScrollListener(onScrollListener);
        mImageSetAdapter.setFolderSelectResult(new PickerFolderAdapter.FolderSelectResult() {
            @Override
            public void folderSelected(ImageSet set, int pos) {
                selectImageFromSet(pos, true);
            }
        });
    }

    /**
     * ???????????????adapter
     */
    private void initAdapters() {
        mFolderListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mImageSetAdapter = new PickerFolderAdapter(presenter, uiConfig);
        mFolderListRecyclerView.setAdapter(mImageSetAdapter);
        mImageSetAdapter.refreshData(imageSets);

        mAdapter = new PickerItemAdapter(selectList, new ArrayList<ImageItem>(), selectConfig, presenter, uiConfig);
        mAdapter.setHasStableIds(true);
        mAdapter.setOnActionResult(this);
        layoutManager = new GridLayoutManager(mContext, selectConfig.getColumnCount());
        if (mRecyclerView.getItemAnimator() instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            mRecyclerView.getItemAnimator().setChangeDuration(0);// ?????????????????????????????????0?????????????????????
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * ?????????????????????
     *
     * @param position ??????
     */
    private void selectImageFromSet(final int position, boolean isTransit) {
        currentImageSet = imageSets.get(position);
        if (isTransit) {
            toggleFolderList();
        }
        for (ImageSet set1 : imageSets) {
            set1.isSelected = false;
        }
        currentImageSet.isSelected = true;
        mImageSetAdapter.notifyDataSetChanged();
        if(currentImageSet.isAllMedia()){
            if(selectConfig.isShowCameraInAllMedia()){
                selectConfig.setShowCamera(true);
            }
        }else {
            if(selectConfig.isShowCameraInAllMedia()){
                selectConfig.setShowCamera(false);
            }
        }
        loadMediaItemsFromSet(currentImageSet);
    }


    /**
     * ??????????????????????????????????????????
     */
    @Override
    protected void toggleFolderList() {
        if (mFolderListRecyclerView.getVisibility() == View.GONE) {
            controllerViewOnTransitImageSet(true);
            v_masker.setVisibility(View.VISIBLE);
            mFolderListRecyclerView.setVisibility(View.VISIBLE);
            mFolderListRecyclerView.setAnimation(AnimationUtils.loadAnimation(mContext,
                    uiConfig.isShowFromBottom() ? R.anim.picker_show2bottom : R.anim.picker_anim_in));
        } else {
            controllerViewOnTransitImageSet(false);
            v_masker.setVisibility(View.GONE);
            mFolderListRecyclerView.setVisibility(View.GONE);
            mFolderListRecyclerView.setAnimation(AnimationUtils.loadAnimation(mContext,
                    uiConfig.isShowFromBottom() ? R.anim.picker_hide2bottom : R.anim.picker_anim_up));
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        if (onDoubleClick()) {
            return;
        }
        if (v == v_masker) {
            toggleFolderList();
        }
    }

    @Override
    protected void loadMediaSetsComplete(@Nullable List<ImageSet> imageSetList) {
        if (imageSetList == null || imageSetList.size() == 0 ||
                (imageSetList.size() == 1 && imageSetList.get(0).count == 0)) {
            tip(getString(R.string.picker_str_tip_media_empty));
            return;
        }
        this.imageSets = imageSetList;
        mImageSetAdapter.refreshData(imageSets);
        selectImageFromSet(0, false);
    }

    @Override
    protected void loadMediaItemsComplete(ImageSet set) {
        this.imageItems = set.imageItems;
        controllerViewOnImageSetSelected(set);
        mAdapter.refreshData(imageItems);
    }

    @Override
    protected void refreshAllVideoSet(ImageSet allVideoSet) {
        if (allVideoSet != null && allVideoSet.imageItems != null
                && allVideoSet.imageItems.size() > 0
                && !imageSets.contains(allVideoSet)) {
            imageSets.add(1, allVideoSet);
            mImageSetAdapter.refreshData(imageSets);
        }
    }

    @Override
    public void onTakePhotoResult(@NonNull ImageItem imageItem) {
        //??????????????????????????????????????????
        if (selectConfig.getSelectMode() == SelectMode.MODE_CROP) {
            intentCrop(imageItem);
            return;
        }
        //????????????????????????????????????
        if (selectConfig.getSelectMode() == SelectMode.MODE_SINGLE) {
            notifyOnSingleImagePickComplete(imageItem);
            return;
        }
        //??????????????????imageItem????????????????????????item????????????
        addItemInImageSets(imageSets, imageItems, imageItem);
        mAdapter.refreshData(imageItems);
        mImageSetAdapter.refreshData(imageSets);
        onCheckItem(imageItem, PickerItemDisableCode.NORMAL);
    }

    @Override
    public boolean onBackPressed() {
        if (mFolderListRecyclerView != null && mFolderListRecyclerView.getVisibility() == View.VISIBLE) {
            toggleFolderList();
            return true;
        }
        if (presenter != null && presenter.interceptPickerCancel(getWeakActivity(), selectList)) {
            return true;
        }
        PickerErrorExecutor.executeError(onImagePickCompleteListener, PickerError.CANCEL.getCode());
        return false;
    }


    @Override
    public void onClickItem(@NonNull ImageItem item, int position, int disableItemCode) {
        position = selectConfig.isShowCamera() ? position - 1 : position;
        //??????
        if (position < 0 && selectConfig.isShowCamera()) {
            if (!presenter.interceptCameraClick(getWeakActivity(), this)) {
                checkTakePhotoOrVideo();
            }
            return;
        }

        //????????????item?????????????????????
        if (interceptClickDisableItem(disableItemCode, false)) {
            return;
        }

        mRecyclerView.setTag(item);

        //????????????????????????????????????
        if (selectConfig.getSelectMode() == SelectMode.MODE_CROP) {
            if (item.isGif() || item.isVideo()) {
                notifyOnSingleImagePickComplete(item);
            } else {
                intentCrop(item);
            }
            return;
        }

        //?????????????????????item??????
        if (!mAdapter.isPreformClick() && presenter.interceptItemClick(getWeakActivity(), item, selectList, imageItems,
                selectConfig, mAdapter, false, this)) {
            return;
        }

        //????????????????????????????????????????????????????????????????????????????????????????????????
        if (item.isVideo() && selectConfig.isVideoSinglePickAndAutoComplete()) {
            notifyOnSingleImagePickComplete(item);
            return;
        }

        //??????????????????????????????????????????????????????item??????????????????????????????
        if (selectConfig.getMaxCount() <= 1 && selectConfig.isSinglePickAutoComplete()) {
            notifyOnSingleImagePickComplete(item);
            return;
        }

        //?????????????????????????????????????????????????????????????????????
        if (item.isVideo() && !selectConfig.isCanPreviewVideo()) {
            tip(getActivity().getString(R.string.picker_str_tip_cant_preview_video));
            return;
        }

        //?????????????????????????????????????????????
        if (selectConfig.isPreview()) {
            intentPreview(true, position);
        }
    }

    @Override
    public void onCheckItem(ImageItem imageItem, int disableItemCode) {
        if (selectConfig.getSelectMode() == SelectMode.MODE_SINGLE
                && selectConfig.getMaxCount() == 1
                && selectList != null && selectList.size() > 0) {
            if (selectList.contains(imageItem)) {
                selectList.clear();
            } else {
                selectList.clear();
                selectList.add(imageItem);
            }
        } else {
            //????????????item?????????????????????
            if (interceptClickDisableItem(disableItemCode, true)) {
                return;
            }

            //?????????????????????item??????
            if (!mAdapter.isPreformClick() && presenter.interceptItemClick(getWeakActivity(), imageItem, selectList, imageItems,
                    selectConfig, mAdapter, true, this)) {
                return;
            }

            //?????????????????????????????????item,????????????????????????
            if (selectList.contains(imageItem)) {
                selectList.remove(imageItem);
            } else {
                selectList.add(imageItem);
            }
        }
        mAdapter.notifyDataSetChanged();
        refreshCompleteState();
    }

    /**
     * ??????????????????
     *
     * @param imageItem ????????????
     */
    private void intentCrop(ImageItem imageItem) {
        ImagePicker.crop(getActivity(), presenter, selectConfig, imageItem, new OnImagePickCompleteListener() {
            @Override
            public void onImagePickComplete(ArrayList<ImageItem> items) {
                selectList.clear();
                selectList.addAll(items);
                mAdapter.notifyDataSetChanged();
                notifyPickerComplete();
            }
        });
    }

    /**
     * ????????????
     *
     * @param position ???????????????index
     */
    @Override
    protected void intentPreview(boolean isClickItem, int position) {
        if (!isClickItem && (selectList == null || selectList.size() == 0)) {
            return;
        }
        MultiImagePreviewActivity.intent(getActivity(), isClickItem ? currentImageSet : null,
                selectList, selectConfig, presenter, position, new MultiImagePreviewActivity.PreviewResult() {
                    @Override
                    public void onResult(ArrayList<ImageItem> mImageItems, boolean isCancel) {
                        if (isCancel) {
                            reloadPickerWithList(mImageItems);
                        } else {
                            selectList.clear();
                            selectList.addAll(mImageItems);
                            mAdapter.notifyDataSetChanged();
                            notifyPickerComplete();
                        }
                    }
                });
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    @Override
    protected void notifyPickerComplete() {
        if (presenter == null||presenter.interceptPickerCompleteClick(getWeakActivity(), selectList, selectConfig)) {
            return;
        }
        if (onImagePickCompleteListener != null) {
            for (ImageItem imageItem : selectList) {
                imageItem.isOriginalImage = ImagePicker.isOriginalImage;
            }
            onImagePickCompleteListener.onImagePickComplete(selectList);
        }
    }

    @Override
    protected BaseSelectConfig getSelectConfig() {
        return selectConfig;
    }

    @Override
    protected IPickerPresenter getPresenter() {
        return presenter;
    }

    @Override
    protected PickerUiConfig getUiConfig() {
        return uiConfig;
    }

    @Override
    public void onDestroy() {
        uiConfig.setPickerUiProvider(null);
        uiConfig = null;
        presenter = null;
        super.onDestroy();
    }

    @Override
    public void reloadPickerWithList(List<ImageItem> selectedList) {
        selectList.clear();
        selectList.addAll(selectedList);
        mAdapter.refreshData(imageItems);
        refreshCompleteState();
    }
}
