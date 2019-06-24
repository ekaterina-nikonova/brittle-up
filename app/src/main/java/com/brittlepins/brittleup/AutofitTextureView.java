package com.brittlepins.brittleup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

public class AutofitTextureView extends TextureView {
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutofitTextureView(Context ctx) {
        this(ctx, null);
    }

    public AutofitTextureView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 0);
    }

    public AutofitTextureView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }

        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }
}
