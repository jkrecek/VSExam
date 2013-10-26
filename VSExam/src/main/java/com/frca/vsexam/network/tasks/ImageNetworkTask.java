package com.frca.vsexam.network.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.frca.vsexam.R;
import com.frca.vsexam.exceptions.NoAuthException;
import com.frca.vsexam.helper.DataHolder;
import com.frca.vsexam.network.HttpRequestBuilder;
import com.frca.vsexam.network.Response;

import java.util.ArrayList;
import java.util.List;

public class ImageNetworkTask extends BaseNetworkTask {
    protected List<ImageView> imageViews = new ArrayList<ImageView>();

    private ImageNetworkTask(Context context) {
        super(DataHolder.getInstance(context), Response.Type.BITMAP);
    }

    public ImageNetworkTask(Context context, String partialUrl, View imageView) {
        this(context);
        addImageView(imageView);
        setBaseRequest(partialUrl);
    }

    public ImageNetworkTask(Context context, String partialUrl, ResponseCallback responseCallback) {
        this(context);
        this.responseCallback = responseCallback;
        setBaseRequest(partialUrl);
    }

    protected void setBaseRequest(String partialUrl) {
        String url = HttpRequestBuilder.completeURLString(partialUrl);
        try {
            request = new HttpRequestBuilder(dataHolder, url).build();
        } catch (NoAuthException e) {
            e.printStackTrace();
        }
    }

    protected void addImageView(View childOrParent) {
        ImageView properView;
        if (childOrParent instanceof ImageView)
            properView = (ImageView)childOrParent;
        else
            properView = (ImageView)childOrParent.findViewById(R.id.image);

        if (!imageViews.contains(properView))
            imageViews.add(properView);
    }

    @Override
    protected void onFinish(Response result) {
        finishOnViews(result.getBitmap());
    }

    protected void finishOnViews(Bitmap bitmap) {
        if (!imageViews.isEmpty()) {
            for (ImageView view : imageViews) {
                setImage(view, bitmap);
            }
        }
    }

    private void setImage(ImageView imageView, Bitmap bitmap) {
        if (bitmap == null)
            imageView.setImageResource(android.R.drawable.ic_delete);
        else
            imageView.setImageBitmap(bitmap);

        View parent = (View)imageView.getParent();
        if (parent != null) {
            View hide = parent.findViewById(R.id.hide);
            if (hide != null)
                hide.setVisibility(View.GONE);
        }
    }
}

