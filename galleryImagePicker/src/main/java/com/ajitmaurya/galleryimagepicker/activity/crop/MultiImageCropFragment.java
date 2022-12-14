package com.ajitmaurya.galleryimagepicker.activity.crop;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajitmaurya.galleryimagepicker.ImagePicker;
import com.ajitmaurya.galleryimagepicker.R;
import com.ajitmaurya.galleryimagepicker.activity.PBaseLoaderFragment;
import com.ajitmaurya.galleryimagepicker.adapter.PickerFolderAdapter;
import com.ajitmaurya.galleryimagepicker.adapter.PickerItemAdapter;
import com.ajitmaurya.galleryimagepicker.bean.PickerItemDisableCode;
import com.ajitmaurya.galleryimagepicker.views.PickerUiConfig;
import com.ajitmaurya.galleryimagepicker.helper.PickerErrorExecutor;
import com.ajitmaurya.galleryimagepicker.bean.selectconfig.BaseSelectConfig;
import com.ajitmaurya.galleryimagepicker.bean.selectconfig.CropSelectConfig;
import com.ajitmaurya.galleryimagepicker.bean.ImageCropMode;
import com.ajitmaurya.galleryimagepicker.bean.ImageItem;
import com.ajitmaurya.galleryimagepicker.bean.ImageSet;
import com.ajitmaurya.galleryimagepicker.bean.PickerError;
import com.ajitmaurya.galleryimagepicker.data.OnImagePickCompleteListener;
import com.ajitmaurya.galleryimagepicker.helper.CropViewContainerHelper;
import com.ajitmaurya.galleryimagepicker.helper.RecyclerViewTouchHelper;
import com.ajitmaurya.galleryimagepicker.helper.VideoViewContainerHelper;
import com.ajitmaurya.galleryimagepicker.presenter.IPickerPresenter;
import com.ajitmaurya.galleryimagepicker.utils.PCornerUtils;
import com.ajitmaurya.galleryimagepicker.utils.PViewSizeUtils;
import com.ajitmaurya.galleryimagepicker.widget.cropimage.CropImageView;
import com.ajitmaurya.galleryimagepicker.widget.TouchRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.ajitmaurya.galleryimagepicker.activity.crop.MultiImageCropActivity.INTENT_KEY_DATA_PRESENTER;
import static com.ajitmaurya.galleryimagepicker.activity.crop.MultiImageCropActivity.INTENT_KEY_SELECT_CONFIG;

/**
 * Description: ?????????????????????fragment
 * <p>
 * Author: peixing.yang
 * Date: 2019/2/21
 * ???????????? ???https://github.com/yangpeixing/YImagePicker/wiki/Documentation_3.x
 */
public class MultiImageCropFragment extends PBaseLoaderFragment implements View.OnClickListener,
        PickerFolderAdapter.FolderSelectResult,
        PickerItemAdapter.OnActionResult {
    private TouchRecyclerView mGridImageRecyclerView;
    private RecyclerView mFolderListRecyclerView;
    private TextView mTvFullOrGap;
    private CropImageView mCropView;
    private ImageButton stateBtn;
    private FrameLayout mCropContainer;
    private RelativeLayout mCropLayout;
    private LinearLayout mInvisibleContainer;
    private View maskView, mImageSetMasker;
    private PickerItemAdapter imageGridAdapter;
    private PickerFolderAdapter folderAdapter;
    private List<ImageSet> imageSets = new ArrayList<>();
    private List<ImageItem> imageItems = new ArrayList<>();
    private int mCropSize;
    private int pressImageIndex = 0;
    //???????????????
    private RecyclerViewTouchHelper touchHelper;
    //?????????????????????
    private IPickerPresenter presenter;
    //???????????????
    private CropSelectConfig selectConfig;
    // ???????????????????????????
    private int cropMode = ImageCropMode.CropViewScale_FULL;
    private ImageItem currentImageItem;
    private View mContentView;
    // fragment ?????????????????????????????????
    private OnImagePickCompleteListener imageListener;
    //??????view???videoView???????????????
    private CropViewContainerHelper cropViewContainerHelper;
    private VideoViewContainerHelper videoViewContainerHelper;
    //UI?????????
    private PickerUiConfig uiConfig;

    private FrameLayout titleBarContainer;
    private FrameLayout bottomBarContainer;
    private FrameLayout titleBarContainer2;

    private ImageItem lastPressItem;

    /**
     * @param imageListener ??????????????????
     */
    public void setOnImagePickCompleteListener(@NonNull OnImagePickCompleteListener imageListener) {
        this.imageListener = imageListener;
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @NonNull Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.picker_activity_multi_crop, container, false);
        return mContentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isIntentDataValid()) {
            ImagePicker.isOriginalImage = false;
            uiConfig = presenter.getUiConfig(getWeakActivity());
            setStatusBar();
            initView();
            initUI();
            initGridImagesAndImageSets();
            loadMediaSets();
        }
    }

    /**
     * ??????????????????
     */
    private boolean isIntentDataValid() {
        Bundle arguments = getArguments();
        if (null != arguments) {
            presenter = (IPickerPresenter) arguments.getSerializable(INTENT_KEY_DATA_PRESENTER);
            selectConfig = (CropSelectConfig) arguments.getSerializable(INTENT_KEY_SELECT_CONFIG);
        }

        if (presenter == null) {
            PickerErrorExecutor.executeError(imageListener, PickerError.PRESENTER_NOT_FOUND.getCode());
            return false;
        }

        if (selectConfig == null) {
            PickerErrorExecutor.executeError(imageListener, PickerError.SELECT_CONFIG_NOT_FOUND.getCode());
            return false;
        }
        return true;
    }

    /**
     * ???????????????
     */
    private void initView() {
        titleBarContainer = mContentView.findViewById(R.id.titleBarContainer);
        titleBarContainer2 = mContentView.findViewById(R.id.titleBarContainer2);
        bottomBarContainer = mContentView.findViewById(R.id.bottomBarContainer);
        mTvFullOrGap = mContentView.findViewById(R.id.mTvFullOrGap);
        mImageSetMasker = mContentView.findViewById(R.id.mImageSetMasker);
        maskView = mContentView.findViewById(R.id.v_mask);
        mCropContainer = mContentView.findViewById(R.id.mCroupContainer);
        mInvisibleContainer = mContentView.findViewById(R.id.mInvisibleContainer);
        RelativeLayout topView = mContentView.findViewById(R.id.topView);
        mCropLayout = mContentView.findViewById(R.id.mCropLayout);
        stateBtn = mContentView.findViewById(R.id.stateBtn);
        mGridImageRecyclerView = mContentView.findViewById(R.id.mRecyclerView);
        mFolderListRecyclerView = mContentView.findViewById(R.id.mImageSetRecyclerView);
        mTvFullOrGap.setBackground(PCornerUtils.cornerDrawable(Color.parseColor("#80000000"), dp(15)));
        //???????????????
        stateBtn.setOnClickListener(this);
        maskView.setOnClickListener(this);
        mImageSetMasker.setOnClickListener(this);
        mTvFullOrGap.setOnClickListener(this);
        //??????????????????
        mCropLayout.setClickable(true);
        //????????????
        maskView.setAlpha(0f);
        maskView.setVisibility(View.GONE);
        //???????????????????????????
        mCropSize = PViewSizeUtils.getScreenWidth(getActivity());
        PViewSizeUtils.setViewSize(mCropLayout, mCropSize, 1.0f);
        //recyclerView???topView????????????????????????
        touchHelper = RecyclerViewTouchHelper.create(mGridImageRecyclerView)
                .setTopView(topView)
                .setMaskView(maskView)
                .setCanScrollHeight(mCropSize)
                .build();
        //?????????????????????
        cropViewContainerHelper = new CropViewContainerHelper(mCropContainer);
        //?????????????????????
        videoViewContainerHelper = new VideoViewContainerHelper();
        //????????????????????????
        if (selectConfig.hasFirstImageItem()) {
            cropMode = selectConfig.getFirstImageItem().getCropMode();
        }
    }

    /**
     * ????????????????????????
     */
    private void initUI() {
        //????????????????????????????????????
        titleBar = inflateControllerView(titleBarContainer, true, uiConfig);
        bottomBar = inflateControllerView(bottomBarContainer, false, uiConfig);
        //?????????????????????
        if (titleBar != null) {
            PViewSizeUtils.setMarginTop(mCropLayout, titleBar.getViewHeight());
            touchHelper.setStickHeight(titleBar.getViewHeight());
        }
        //?????????????????????
        if (bottomBar != null) {
            PViewSizeUtils.setMarginTopAndBottom(mGridImageRecyclerView, 0, bottomBar.getViewHeight());
        }
        //??????????????????
        mCropContainer.setBackgroundColor(uiConfig.getCropViewBackgroundColor());
        mGridImageRecyclerView.setBackgroundColor(uiConfig.getPickerBackgroundColor());
        stateBtn.setImageDrawable(getResources().getDrawable(uiConfig.getFullIconID()));
        mTvFullOrGap.setCompoundDrawablesWithIntrinsicBounds(getResources().
                getDrawable(uiConfig.getFillIconID()), null, null, null);
        //????????????????????????
        setFolderListHeight(mFolderListRecyclerView, mImageSetMasker, true);
    }

    /**
     * ?????????????????????
     */
    private void initGridImagesAndImageSets() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), selectConfig.getColumnCount());
        mGridImageRecyclerView.setLayoutManager(gridLayoutManager);
        imageGridAdapter = new PickerItemAdapter(selectList, imageItems, selectConfig, presenter, uiConfig);
        imageGridAdapter.setHasStableIds(true);
        mGridImageRecyclerView.setAdapter(imageGridAdapter);
        //????????????????????????
        mFolderListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        folderAdapter = new PickerFolderAdapter(presenter, uiConfig);
        mFolderListRecyclerView.setAdapter(folderAdapter);
        folderAdapter.refreshData(imageSets);
        mFolderListRecyclerView.setVisibility(View.GONE);
        folderAdapter.setFolderSelectResult(this);
        imageGridAdapter.setOnActionResult(this);
    }

    @Override
    public void onClick(@NonNull View view) {
        if (imageItems == null || imageItems.size() == 0) {
            return;
        }
        if (onDoubleClick()) {
            tip(getActivity().getString(R.string.picker_str_tip_action_frequently));
            return;
        }
        if (view == stateBtn) {
            fullOrFit();
        } else if (view == maskView) {
            touchHelper.transitTopWithAnim(true, pressImageIndex, true);
        } else if (view == mTvFullOrGap) {
            fullOrGap();
        } else if (mImageSetMasker == view) {
            toggleFolderList();
        }
    }


    /**
     * ????????????
     *
     * @param imageItem ??????item
     * @param position  ??????item???position
     */
    @Override
    public void onClickItem(@NonNull ImageItem imageItem, int position, int disableItemCode) {
        //??????
        if (position <= 0 && selectConfig.isShowCamera()) {
            //??????????????????
            if (presenter.interceptCameraClick(getWeakActivity(), this)) {
                return;
            }
            checkTakePhotoOrVideo();
            return;
        }

        //????????????item?????????????????????
        if (interceptClickDisableItem(disableItemCode, false)) {
            return;
        }

        //?????????????????????item??????
        pressImageIndex = position;
        //??????????????????
        if (imageItems == null || imageItems.size() == 0 ||
                imageItems.size() <= pressImageIndex) {
            return;
        }

        //??????????????????item???????????????
        if (isInterceptItemClick(imageItem, false)) {
            return;
        }

        //????????????item
        onPressImage(imageItem, true);
    }


    private boolean isInterceptItemClick(ImageItem imageItem, boolean isClickCheckbox) {
        return !imageGridAdapter.isPreformClick() && presenter.interceptItemClick(getWeakActivity(), imageItem, selectList,
                (ArrayList<ImageItem>) imageItems, selectConfig, imageGridAdapter, isClickCheckbox,
                null);
    }

    /**
     * ????????????
     *
     * @param imageItem ??????
     */
    private void onPressImage(ImageItem imageItem, boolean isShowTransit) {
        currentImageItem = imageItem;
        if (lastPressItem != null) {
            //?????????????????????item??????????????????????????????????????????
            if (lastPressItem.equals(currentImageItem)) {
                return;
            }
            //??????????????????
            lastPressItem.setPress(false);
        }
        currentImageItem.setPress(true);
        //??????????????????
        if (currentImageItem.isVideo()) {
            if (selectConfig.isVideoSinglePickAndAutoComplete()) {
                notifyOnSingleImagePickComplete(imageItem);
                return;
            }
            //????????????????????????
            videoViewContainerHelper.loadVideoView(mCropContainer, currentImageItem, presenter, uiConfig);
        } else {
            //????????????
            loadCropView();
        }
        checkStateBtn();
        imageGridAdapter.notifyDataSetChanged();
        touchHelper.transitTopWithAnim(true, pressImageIndex, isShowTransit);
        lastPressItem = currentImageItem;
    }


    /**
     * ????????????????????????????????????
     *
     * @param imageItem ??????item
     */
    @Override
    public void onCheckItem(ImageItem imageItem, int disableItemCode) {
        //????????????item?????????????????????
        if (interceptClickDisableItem(disableItemCode, true)) {
            return;
        }

        //??????????????????item???????????????
        if (isInterceptItemClick(imageItem, true)) {
            return;
        }

        //??????????????????????????????????????????item?????????????????????
        if (selectList.contains(imageItem)) {
            removeImageItemFromCropViewList(imageItem);
            checkStateBtn();
        } else {
            onPressImage(imageItem, false);
            addImageItemToCropViewList(imageItem);
        }
        imageGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void folderSelected(ImageSet set, int pos) {
        selectImageSet(pos, true);
    }

    /**
     * ??????????????????
     *
     * @param position ??????position
     */
    private void selectImageSet(int position, boolean isTransit) {
        ImageSet imageSet = imageSets.get(position);
        if (imageSet == null) {
            return;
        }
        for (ImageSet set : imageSets) {
            set.isSelected = false;
        }
        imageSet.isSelected = true;
        folderAdapter.notifyDataSetChanged();
        if (titleBar != null) {
            titleBar.onImageSetSelected(imageSet);
        }
        if (bottomBar != null) {
            bottomBar.onImageSetSelected(imageSet);
        }
        if (isTransit) {
            toggleFolderList();
        }
        loadMediaItemsFromSet(imageSet);
    }

    /**
     * ????????????view
     */
    private void loadCropView() {
        mCropView = cropViewContainerHelper.loadCropView(getContext(), currentImageItem, mCropSize,
                presenter, new CropViewContainerHelper.onLoadComplete() {
                    @Override
                    public void loadComplete() {
                        checkStateBtn();
                    }
                });
        resetCropViewSize(mCropView, false);
    }

    /**
     * ???????????????????????????????????????
     */
    private void addImageItemToCropViewList(ImageItem imageItem) {
        if (!selectList.contains(imageItem)) {
            selectList.add(imageItem);
        }
        cropViewContainerHelper.addCropView(mCropView, imageItem);
        refreshCompleteState();
    }

    /**
     * ??????????????????????????????????????????
     */
    private void removeImageItemFromCropViewList(ImageItem imageItem) {
        selectList.remove(imageItem);
        cropViewContainerHelper.removeCropView(imageItem);
        refreshCompleteState();
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    private void checkStateBtn() {
        //??????????????????item?????????????????????????????????
        if (currentImageItem.isVideo()) {
            stateBtn.setVisibility(View.GONE);
            mTvFullOrGap.setVisibility(View.GONE);
            return;
        }
        //??????????????????????????????
        if (currentImageItem.getWidthHeightType() == 0) {
            stateBtn.setVisibility(View.GONE);
            mTvFullOrGap.setVisibility(View.GONE);
            return;
        }
        //???????????????????????????????????????
        if (selectConfig.hasFirstImageItem()) {
            stateBtn.setVisibility(View.GONE);
            if (selectConfig.isAssignGapState()) {
                if (selectList.size() == 0 || (selectList.get(0) != null
                        && selectList.get(0).equals(currentImageItem))) {
                    setImageScaleState();
                } else {
                    mTvFullOrGap.setVisibility(View.GONE);
                    if (selectList.get(0).getCropMode() == ImageCropMode.ImageScale_GAP) {
                        mCropView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        mCropView.setBackgroundColor(Color.WHITE);
                    } else {
                        mCropView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mCropView.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            } else {
                setImageScaleState();
            }
            return;
        }

        //???????????????????????????0 ???
        if (selectList.size() > 0) {
            //??????????????????item??????????????????????????????stateBtn
            if (currentImageItem == selectList.get(0)) {
                stateBtn.setVisibility(View.VISIBLE);
                mTvFullOrGap.setVisibility(View.GONE);
                mCropView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                currentImageItem.setCropMode(cropMode);
            } else {
                //??????????????????item??????????????????????????????mTvFullOrGap
                stateBtn.setVisibility(View.GONE);
                setImageScaleState();
            }
        } else {//??????????????????
            stateBtn.setVisibility(View.VISIBLE);
            mTvFullOrGap.setVisibility(View.GONE);
        }
    }

    /**
     * ????????????????????????
     */
    private void resetCropViewSize(CropImageView view, boolean isShowAnim) {
        int height = mCropSize;
        int width = mCropSize;
        if (cropMode == ImageCropMode.CropViewScale_FIT) {
            ImageItem firstImageItem;
            //?????????????????????????????????????????????????????????????????????????????????
            if (selectConfig.hasFirstImageItem()) {
                firstImageItem = selectConfig.getFirstImageItem();
            } else {
                //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (selectList.size() > 0) {
                    firstImageItem = selectList.get(0);
                } else {
                    firstImageItem = currentImageItem;
                }
            }
            //?????????????????????*3/4
            height = firstImageItem.getWidthHeightType() > 0 ? ((mCropSize * 3) / 4) : mCropSize;
            //?????????????????????*3/4
            width = firstImageItem.getWidthHeightType() < 0 ? ((mCropSize * 3) / 4) : mCropSize;
        }
        view.changeSize(isShowAnim, width, height);
    }


    /**
     * ???????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private void fullOrFit() {
        if (cropMode == ImageCropMode.CropViewScale_FIT) {
            cropMode = ImageCropMode.CropViewScale_FULL;
            stateBtn.setImageDrawable(getResources().getDrawable(uiConfig.getFitIconID()));
        } else {
            cropMode = ImageCropMode.CropViewScale_FIT;
            stateBtn.setImageDrawable(getResources().getDrawable(uiConfig.getFullIconID()));
        }
        if (currentImageItem != null) {
            currentImageItem.setCropMode(cropMode);
        }

        mCropView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        resetCropViewSize(mCropView, true);
        //?????????????????????????????????????????????
        cropViewContainerHelper.refreshAllState(currentImageItem, selectList, mInvisibleContainer,
                cropMode == ImageCropMode.CropViewScale_FIT,
                new CropViewContainerHelper.ResetSizeExecutor() {
                    @Override
                    public void resetAllCropViewSize(CropImageView view) {
                        resetCropViewSize(view, false);
                    }
                });
    }


    /**
     * ????????????????????????
     */
    private void setImageScaleState() {
        //????????????????????????????????????
        if (cropMode == ImageCropMode.CropViewScale_FIT) {
            //?????????????????????????????????????????????????????????????????????????????????????????????
            mTvFullOrGap.setVisibility(View.GONE);
        } else {
            //??????????????????????????????????????????????????????????????????????????????????????????????????????
            mTvFullOrGap.setVisibility(View.VISIBLE);
            //???????????????????????????????????????????????????????????????????????????
            if (selectList.contains(currentImageItem)) {
                if (currentImageItem.getCropMode() == ImageCropMode.ImageScale_FILL) {
                    fullState();
                } else if (currentImageItem.getCropMode() == ImageCropMode.ImageScale_GAP) {
                    gapState();
                }
            } else {
                //?????????????????????????????????????????????????????????
                fullState();
                currentImageItem.setCropMode(ImageCropMode.ImageScale_FILL);
                mCropView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    /**
     * ??????????????????
     */
    private void fullOrGap() {
        //??????
        if (currentImageItem.getCropMode() == ImageCropMode.ImageScale_FILL) {
            currentImageItem.setCropMode(ImageCropMode.ImageScale_GAP);
            mCropView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            gapState();
        } else {
            //??????
            currentImageItem.setCropMode(ImageCropMode.ImageScale_FILL);
            mCropView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            fullState();
        }
        resetCropViewSize(mCropView, false);
    }

    /**
     * ????????????????????????????????????
     */
    private void gapState() {
        mTvFullOrGap.setText(getString(R.string.picker_str_redBook_full));
        mCropView.setBackgroundColor(Color.WHITE);
        mTvFullOrGap.setCompoundDrawablesWithIntrinsicBounds(getResources().
                getDrawable(uiConfig.getFillIconID()), null, null, null);
    }

    /**
     * ????????????????????????????????????
     */
    private void fullState() {
        mTvFullOrGap.setText(getString(R.string.picker_str_redBook_gap));
        mCropView.setBackgroundColor(Color.TRANSPARENT);
        mTvFullOrGap.setCompoundDrawablesWithIntrinsicBounds(getResources().
                getDrawable(uiConfig.getGapIconID()), null, null, null);
    }


    /**
     * ??????????????????????????????????????????????????????
     */
    @Override
    protected void notifyPickerComplete() {
        //?????????????????????????????????
        if (selectList.size() > 0 && selectList.get(0).isVideo()) {
        } else {
            //????????????
            if (mCropView.isEditing()) {
                return;
            }
            //??????????????????
            if (selectList.contains(currentImageItem)
                    && (mCropView.getDrawable() == null ||
                    mCropView.getDrawable().getIntrinsicHeight() == 0 ||
                    mCropView.getDrawable().getIntrinsicWidth() == 0)) {
                tip(getString(R.string.picker_str_tip_shield));
                return;
            }
            selectList = cropViewContainerHelper.generateCropUrls(selectList, cropMode);
        }

        //???????????????????????????????????????????????????????????????
        if (!presenter.interceptPickerCompleteClick(getWeakActivity(), selectList, selectConfig)) {
            if (null != imageListener) {
                imageListener.onImagePickComplete(selectList);
            }
        }
    }


    @Override
    protected void toggleFolderList() {
        if (mFolderListRecyclerView.getVisibility() == View.GONE) {
            View view = titleBarContainer.getChildAt(0);
            if (view == null) {
                return;
            }
            titleBarContainer.removeAllViews();
            titleBarContainer2.removeAllViews();
            titleBarContainer2.addView(view);

            mImageSetMasker.setVisibility(View.VISIBLE);
            controllerViewOnTransitImageSet(true);
            mFolderListRecyclerView.setVisibility(View.VISIBLE);
            mFolderListRecyclerView.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    uiConfig.isShowFromBottom() ? R.anim.picker_show2bottom : R.anim.picker_anim_in));

        } else {
            final View view = titleBarContainer2.getChildAt(0);
            if (view == null) {
                return;
            }
            mImageSetMasker.setVisibility(View.GONE);
            controllerViewOnTransitImageSet(false);
            mFolderListRecyclerView.setVisibility(View.GONE);
            mFolderListRecyclerView.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                    uiConfig.isShowFromBottom() ? R.anim.picker_hide2bottom : R.anim.picker_anim_up));

            titleBarContainer2.postDelayed(new Runnable() {
                @Override
                public void run() {
                    titleBarContainer2.removeAllViews();
                    titleBarContainer.removeAllViews();
                    titleBarContainer.addView(view);
                }
            }, 300);
        }

    }

    @Override
    protected void intentPreview(boolean isFolderListPreview, int index) {

    }

    @Override
    protected void loadMediaSetsComplete(@Nullable List<ImageSet> imageSetList) {
        if (imageSetList == null || imageSetList.size() == 0 ||
                (imageSetList.size() == 1 && imageSetList.get(0).count == 0)) {
            tip(getString(R.string.picker_str_tip_media_empty));
            return;
        }
        this.imageSets = imageSetList;
        folderAdapter.refreshData(imageSets);
        selectImageSet(0, false);
    }

    @Override
    protected void loadMediaItemsComplete(@NonNull ImageSet set) {
        if (set.imageItems != null && set.imageItems.size() > 0) {
            imageItems.clear();
            imageItems.addAll(set.imageItems);
            imageGridAdapter.notifyDataSetChanged();
            int firstImageIndex = getCanPressItemPosition();
            if (firstImageIndex < 0) {
                return;
            }
            int index = selectConfig.isShowCamera() ? firstImageIndex + 1 : firstImageIndex;
            onClickItem(imageItems.get(firstImageIndex), index, PickerItemDisableCode.NORMAL);
        }
    }

    /**
     * @return ????????????????????????item?????????????????????
     */
    private int getCanPressItemPosition() {
        for (int i = 0; i < imageItems.size(); i++) {
            ImageItem imageItem = imageItems.get(i);
            if (imageItem.isVideo() && selectConfig.isVideoSinglePickAndAutoComplete()) {
                continue;
            }
            int code = PickerItemDisableCode.getItemDisableCode(imageItem, selectConfig,
                    selectList, false);
            if (code == PickerItemDisableCode.NORMAL) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void refreshAllVideoSet(@Nullable ImageSet allVideoSet) {
        if (allVideoSet != null &&
                allVideoSet.imageItems != null
                && allVideoSet.imageItems.size() > 0
                && !imageSets.contains(allVideoSet)) {
            imageSets.add(1, allVideoSet);
            folderAdapter.refreshData(imageSets);
        }
    }

    /**
     * ????????????????????????
     */
    @Override
    public boolean onBackPressed() {
        if (mFolderListRecyclerView != null && mFolderListRecyclerView.getVisibility() == View.VISIBLE) {
            toggleFolderList();
            return true;
        }
        if (presenter != null && presenter.interceptPickerCancel(getWeakActivity(), selectList)) {
            return true;
        }
        PickerErrorExecutor.executeError(imageListener, PickerError.CANCEL.getCode());
        return false;
    }


    @Override
    public void onTakePhotoResult(@Nullable ImageItem imageItem) {
        if (imageItem != null) {
            addItemInImageSets(imageSets, imageItems, imageItem);
            onCheckItem(imageItem, PickerItemDisableCode.NORMAL);
            imageGridAdapter.notifyDataSetChanged();
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
        //???VideoView???????????????????????????
        if (videoViewContainerHelper != null) {
            videoViewContainerHelper.onDestroy();
        }
        uiConfig.setPickerUiProvider(null);
        uiConfig = null;
        presenter = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoViewContainerHelper != null) {
            videoViewContainerHelper.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoViewContainerHelper != null) {
            videoViewContainerHelper.onPause();
        }
    }
}
