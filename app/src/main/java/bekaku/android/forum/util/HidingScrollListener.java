package bekaku.android.forum.util;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
/*
//    public void showViews() {
//        hidingScrollListener.resetScrollDistance();
//        binding.threadDataHolder.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(300);
//    }
//
//    public void hideViews() {
//        binding.threadDataHolder.animate().translationY(binding.threadDataHolder.getHeight()).setInterpolator(new AccelerateInterpolator(2)).setDuration(300);
//    }
 */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

    public boolean mControlsVisible = true;
    private int HIDE_THRESHOLD = 50;
    private int mScrolledDistance = 0;

    public HidingScrollListener(int threshold) {
        this.HIDE_THRESHOLD = threshold;
    }

    private HidingScrollListener() {
    }

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        if (firstVisibleItem == 0) {
            if (!mControlsVisible) {
                onShow();
                mControlsVisible = true;
            }
        } else {
            if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
                onHide();
                mControlsVisible = false;
                mScrolledDistance = 0;
            } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
                onShow();
                mControlsVisible = true;
                mScrolledDistance = 0;
            }
        }
        if ((mControlsVisible && dy > 0) || (!mControlsVisible && dy < 0)) {
            mScrolledDistance += dy;
        }
    }

    public void resetScrollDistance() {
        mControlsVisible = true;
        mScrolledDistance = 0;
    }

    public abstract void onHide();

    public abstract void onShow();
}
