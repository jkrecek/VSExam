package com.frca.vsexam.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frca.vsexam.entities.exam.ExamList;
import com.frca.vsexam.fragments.BrowserPaneFragment;
import com.frca.vsexam.fragments.base.ContentFragment;

public abstract class ViewProvider {

    public enum Result {
        DELAYED,
        DONE,
        HIDE
    }

    private ViewGroup mParent;

    private ContentFragment mBaseFragment;

    private View mView;

    private LayoutInflater mInflater;

    private int mResourceId;

    private boolean validated;

    public ViewProvider(ContentFragment baseFragment, ViewGroup parent, LayoutInflater inflater, int resourceId) {
        mBaseFragment = baseFragment;
        mParent = parent;
        mInflater = inflater;
        mResourceId = resourceId;

        invalidateView();
    }

    public final void load() {
        invalidateView();
        final Result result = doLoad();
        if (result != Result.DELAYED) {
            if (result == Result.HIDE) {
                mView = null;
            }

            Helper.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        validateView();
                        mBaseFragment.notifyResult(ViewProvider.this, result);
                    }
                }
            );
        }
    }

    public abstract Result doLoad();

    protected void validateView() {
        if (validated)
            return;

        validated = true;
        mParent.removeAllViews();
        if (mView != null)
            mParent.addView(mView);
    }

    protected void invalidateView() {
        validated = false;
        if (mInflater != null && mResourceId != 0)
            mView = mInflater.inflate(mResourceId, null, false);
    }

    protected void setViewText(int id, CharSequence sequence) {
        TextView textView = (TextView) mView.findViewById(id);
        if (textView != null)
            textView.setText(sequence);
    }

    protected void setViewText(int id, int textResource) {
        TextView textView = (TextView) mView.findViewById(id);
        if (textView != null)
            textView.setText(textResource);
    }

    protected void setOnClickListener(int id, View.OnClickListener onClickListener) {
        View view = mView.findViewById(id);
        if (view != null)
            view.setOnClickListener(onClickListener);
    }

    protected void loopThoughChildren(Helper.ViewCallback callback) {
        if (mView instanceof ViewGroup)
            Helper.loopThoughLayout((ViewGroup) mView, callback);
    }

    protected View findViewById(int id) {
        return mView.findViewById(id);
    }

    protected Context getContext() {
        return mBaseFragment.getActivity();
    }

    protected BrowserPaneFragment getMainFragment() {
        return mBaseFragment.getBrowserPaneFragment();
    }

    protected ExamList getExams() {
        return getMainFragment().getExams();
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    public ViewGroup getParent() {
        return mParent;
    }

}
