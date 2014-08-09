package com.frca.vsexam.fragments.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frca.vsexam.R;
import com.frca.vsexam.context.MainActivity;
import com.frca.vsexam.helper.ViewProvider;

public abstract class ContentFragment extends BaseFragment {

    private Class<? extends ViewProvider> mProviderClasses[] = null;

    protected static final String EXTRA_ID = "id";

    private Handler mHandler = new Handler(Looper.getMainLooper());

    @SuppressWarnings("unchecked")
    public ContentFragment(Class<? extends ViewProvider> firstProvider, Class<? extends ViewProvider> secondProvider, Class<? extends ViewProvider> buttonProvider) {
        super();

        mProviderClasses = new Class[] { firstProvider, secondProvider, buttonProvider };
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        if (!(getParentActivity() instanceof MainActivity)) {
            Log.e(getClass().getName(), "This class must be child of MainActivity");
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadToView();
            }
        }, 100);*/

        SlidingPaneLayout slidingPaneLayout = getMainActivity().getSlidingLayout();
        if (slidingPaneLayout.isSlideable() && !slidingPaneLayout.isOpen()) {
            ActionBar actionBar = getMainActivity().getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(getTitle());
        }
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public final void onViewCreated(View view, Bundle savedInstanceBundle) {
        loadToView();
    }

    private void loadToView() {
        final LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup layouts[] = {
            (ViewGroup) getView().findViewById(R.id.layout_first),
            (ViewGroup) getView().findViewById(R.id.layout_second),
            (ViewGroup) getView().findViewById(R.id.layout_buttons)
        };

        for (int i = 0; i < mProviderClasses.length; ++i) {
            final int idx = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getProvider(mProviderClasses[idx], layouts[idx], inflater).load();
                }
            }).start();
        }
    }

    public void reload() {
        loadToView();
    }

    private ViewProvider getProvider(Class<? extends ViewProvider> providerClass, final ViewGroup parent, final LayoutInflater inflater) {

        try {
            if (providerClass != null) {
                return providerClass
                    .getDeclaredConstructor(getClass(), ViewGroup.class, LayoutInflater.class)
                    .newInstance(this, parent, inflater);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DefaultViewProvider(ContentFragment.this, parent);
    }

    public void notifyResult(ViewProvider provider, ViewProvider.Result result) {
        if (provider.getParent().getId() == R.id.layout_second)
            setSecondPanelVisibility(result == ViewProvider.Result.DONE);
    }

    private void setSecondPanelVisibility(boolean apply) {
        View view = getView();
        if (view != null) {
            View secondLayout = view.findViewById(R.id.layout_second);
            secondLayout.setVisibility(apply ? View.VISIBLE : View.GONE);
        }
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getParentActivity();
    }

    public abstract String getTitle();

    private class DefaultViewProvider extends ViewProvider {

        public DefaultViewProvider(ContentFragment baseFragment, ViewGroup parent) {
            super(baseFragment, parent, null, 0);
        }

        @Override
        public Result doLoad() {
            return Result.HIDE;
        }
    }

}
