package com.frca.vsexam.fragments.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PagerFragment extends BaseFragment {

    private final static int ACTION_ACTIVITY = 0;
    private final static int ACTION_INACTIVITY = 1;

    private List<Runnable> mRunnableQueue = new ArrayList<Runnable>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Iterator<Runnable> itr = mRunnableQueue.iterator();
        while (itr.hasNext()) {
            itr.next().run();
            itr.remove();
        }
    }


    public void onActive() {}

    public void onInactive() {}

    public void postActive() {
        handlePostRunnable(new Runnable() {
            @Override
            public void run() {
                onActive();
            }
        });
    }

    public void postInactive() {
        handlePostRunnable(new Runnable() {
            @Override
            public void run() {
                onInactive();
            }
        });
    }

    private void handlePostRunnable(Runnable r) {
        if (getActivity() == null)
            mRunnableQueue.add(r);
        else
            r.run();
    }
}
