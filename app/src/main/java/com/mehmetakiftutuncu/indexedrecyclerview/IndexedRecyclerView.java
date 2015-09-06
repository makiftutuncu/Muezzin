package com.mehmetakiftutuncu.indexedrecyclerview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.mehmetakiftutuncu.muezzin.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class IndexedRecyclerView extends RecyclerView {
    private int           indexBackgroundColor;
    private int           indexBackgroundStrokeColor;
    private float         indexBackgroundStrokeWidth;
    private int           indexScrollHandleColor;
    private int           indexTextColor;
    private float         indexTextSize;
    private IndexLocation indexLocation;
    private float         indexHorizontalMargin;
    private float         indexVerticalMargin;

    private int          previewBackgroundColor;
    private int          previewBackgroundStrokeColor;
    private float        previewBackgroundStrokeWidth;
    private int          previewTintColor;
    private int          previewTextColor;
    private float        previewTextSize;
    private PreviewStyle previewStyle;

    private boolean isInitialized = false;
    private boolean isShowingPreview = false;
    private String[] indices;
    private int currentIndex;
    private String currentIndexText;

    private float indicesBackgroundLeft;
    private float indicesBackgroundTop;
    private float indicesBackgroundRight;
    private float indicesBackgroundBottom;
    private float indicesBackgroundWidth;
    private float indicesBackgroundHeight;

    private float previewBackgroundLeft;
    private float previewBackgroundTop;
    private float previewBackgroundRight;
    private float previewBackgroundBottom;
    private float previewBackgroundWidth;
    private float previewBackgroundHeight;

    private final static int defaultIndexBackgroundColor         = Color.argb(0x00, 0x00, 0x00, 0x00);
    private final static int defaultIndexBackgroundStrokeColor   = Color.argb(0x00, 0x00, 0x00, 0x00);
    private final static int defaultIndexScrollHandleColor       = Color.argb(0x33, 0x00, 0x00, 0x00);
    private final static int defaultIndexTextColor               = Color.argb(0xFF, 0x00, 0x00, 0x00);
    private final static int defaultIndexLocation                = IndexLocation.RIGHT.ordinal();
    private final static float defaultIndexBackgroundStrokeWidth = 0.0f;
    private final static float defaultIndexTextSize              = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10.0f, Resources.getSystem().getDisplayMetrics());
    private final static float defaultIndexHorizontalMargin      = 0.0f;
    private final static float defaultIndexVerticalMargin        = 0.0f;

    private final static int defaultPreviewBackgroundColor         = Color.argb(0x33, 0x00, 0x00, 0x00);
    private final static int defaultPreviewBackgroundStrokeColor   = Color.argb(0x66, 0x00, 0x00, 0x00);
    private final static int defaultPreviewTintColor               = Color.argb(0x00, 0x00, 0x00, 0x00);
    private final static int defaultPreviewTextColor               = Color.argb(0xFF, 0xFF, 0xFF, 0xFF);
    private final static int defaultPreviewStyle                   = PreviewStyle.CIRCLE.ordinal();
    private final static float defaultPreviewBackgroundStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4.0f, Resources.getSystem().getDisplayMetrics());
    private final static float defaultPreviewTextSize              = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 64.0f, Resources.getSystem().getDisplayMetrics());

    public IndexedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray attributes = context
                .getTheme()
                .obtainStyledAttributes(attrs, R.styleable.IndexedRecyclerView, 0, 0);

        try {
            indexBackgroundColor       = attributes.getColor(R.styleable.IndexedRecyclerView_indexBackgroundColor, defaultIndexBackgroundColor);
            indexBackgroundStrokeColor = attributes.getColor(R.styleable.IndexedRecyclerView_indexBackgroundStrokeColor, defaultIndexBackgroundStrokeColor);
            indexBackgroundStrokeWidth = attributes.getDimension(R.styleable.IndexedRecyclerView_indexBackgroundStrokeWidth, defaultIndexBackgroundStrokeWidth);
            indexScrollHandleColor     = attributes.getColor(R.styleable.IndexedRecyclerView_indexScrollHandleColor, defaultIndexScrollHandleColor);
            indexTextColor             = attributes.getColor(R.styleable.IndexedRecyclerView_indexTextColor, defaultIndexTextColor);
            indexTextSize              = attributes.getDimension(R.styleable.IndexedRecyclerView_indexTextSize, defaultIndexTextSize);
            indexLocation              = IndexLocation.values()[attributes.getInteger(R.styleable.IndexedRecyclerView_indexLocation, defaultIndexLocation)];
            indexHorizontalMargin      = attributes.getDimension(R.styleable.IndexedRecyclerView_indexHorizontalMargin, defaultIndexHorizontalMargin);
            indexVerticalMargin        = attributes.getDimension(R.styleable.IndexedRecyclerView_indexVerticalMargin, defaultIndexVerticalMargin);

            previewBackgroundColor       = attributes.getColor(R.styleable.IndexedRecyclerView_previewBackgroundColor, defaultPreviewBackgroundColor);
            previewBackgroundStrokeColor = attributes.getColor(R.styleable.IndexedRecyclerView_previewBackgroundStrokeColor, defaultPreviewBackgroundStrokeColor);
            previewBackgroundStrokeWidth = attributes.getDimension(R.styleable.IndexedRecyclerView_previewBackgroundStrokeWidth, defaultPreviewBackgroundStrokeWidth);
            previewTintColor             = attributes.getColor(R.styleable.IndexedRecyclerView_previewTintColor, defaultPreviewTintColor);
            previewTextColor             = attributes.getColor(R.styleable.IndexedRecyclerView_previewTextColor, defaultPreviewTextColor);
            previewTextSize              = attributes.getDimension(R.styleable.IndexedRecyclerView_previewTextSize, defaultPreviewTextSize);
            previewStyle                 = PreviewStyle.values()[attributes.getInteger(R.styleable.IndexedRecyclerView_previewStyle, defaultPreviewStyle)];
        } finally {
            attributes.recycle();
        }
    }

    @Override
    public void onDraw(Canvas c) {
        if(!isInitialized) {
            initialize();
        }

        super.onDraw(c);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            isInitialized = false;
        }

        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        HashMap<String, Integer> indicesMap = ((Indices) getAdapter()).getIndicesMap();

        int currentIndexPosition = (int) Math.floor((y / (indicesBackgroundBottom - indicesBackgroundTop)) * indices.length);

        if (currentIndexPosition < 0) {
            currentIndexPosition = 0;
        } else if (currentIndexPosition >= indices.length) {
            currentIndexPosition = indices.length - 1;
        }

        currentIndex = currentIndexPosition;
        currentIndexText = indices[currentIndexPosition];

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTouchingIndices(x, y)) {
                    showPreview(indicesMap, currentIndexText);
                    return true;
                }

            case MotionEvent.ACTION_MOVE:
                if (isShowingPreview || isTouchingIndices(x, y)) {
                    showPreview(indicesMap, currentIndexText);
                    return true;
                }

            case MotionEvent.ACTION_UP:
                if (isShowingPreview) {
                    hidePreview();
                    return true;
                }
        }

        return super.onTouchEvent(event);
    }

    private boolean isTouchingIndices(float x, float y) {
        return x >= indicesBackgroundLeft && x <= indicesBackgroundRight && y >= indicesBackgroundTop && y <= indicesBackgroundBottom;
    }

    private void showPreview(HashMap<String, Integer> indicesMap, String index) {
        isShowingPreview = true;

        int positionInData = 0;

        if (indicesMap.containsKey(index)) {
            positionInData = indicesMap.get(index);
        }

        scrollToPosition(positionInData);
        refresh();
    }

    private void hidePreview() {
        isShowingPreview = false;
        refresh();
    }

    private void initialize() {
        Set<String> indexKeySet = ((Indices) getAdapter()).getIndicesMap().keySet();
        List<String> indexKeyList = new ArrayList<>(indexKeySet);
        Collections.sort(indexKeyList);

        indices = new String[indexKeyList.size()];
        for (int i = 0, size = indices.length; i < size; i++) {
            indices[i] = indexKeyList.get(i);
        }

        int measuredWidth  = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        float horizontalDistance = indexHorizontalMargin;
        float verticalDistance   = indexVerticalMargin;

        if (isIndicesOnRight()) {
            horizontalDistance += getPaddingRight();
        } else {
            horizontalDistance += getPaddingLeft();
        }

        indicesBackgroundWidth  = (float) 1.4 * indexTextSize;
        indicesBackgroundHeight = measuredHeight - (2 * verticalDistance);
        indicesBackgroundLeft   = isIndicesOnRight() ? (measuredWidth - indicesBackgroundWidth - horizontalDistance) : horizontalDistance;
        indicesBackgroundTop    = getTop() + verticalDistance;
        indicesBackgroundRight  = indicesBackgroundLeft + indicesBackgroundWidth;
        indicesBackgroundBottom = indicesBackgroundTop  + indicesBackgroundHeight;

        previewBackgroundWidth  = (float) 2.0 * previewTextSize;
        previewBackgroundHeight = (float) 1.6 * previewTextSize;
        previewBackgroundLeft   = (measuredWidth - previewBackgroundWidth) / 2;
        previewBackgroundTop    = (measuredHeight - previewBackgroundHeight) / 2;
        previewBackgroundRight  = previewBackgroundLeft + previewBackgroundWidth;
        previewBackgroundBottom = previewBackgroundTop  + previewBackgroundHeight;

        isInitialized = true;
    }

    private void refresh() {
        invalidate();
        requestLayout();
    }

    public boolean isIndicesOnRight() {
        return indexLocation.ordinal() == IndexLocation.RIGHT.ordinal();
    }

    public boolean isPreviewRectangle() {
        return previewStyle.ordinal() == PreviewStyle.RECTANGLE.ordinal();
    }

    public boolean isPreviewCircle() {
        return previewStyle.ordinal() == PreviewStyle.CIRCLE.ordinal();
    }

    public int getIndexBackgroundColor() {
        return indexBackgroundColor;
    }

    public void setIndexBackgroundColor(int indexBackgroundColor) {
        this.indexBackgroundColor = indexBackgroundColor;
        refresh();
    }

    public int getIndexTextColor() {
        return indexTextColor;
    }

    public void setIndexTextColor(int indexTextColor) {
        this.indexTextColor = indexTextColor;
        refresh();
    }

    public float getIndexTextSize() {
        return indexTextSize;
    }

    public void setIndexTextSize(float indexTextSize) {
        this.indexTextSize = indexTextSize;
        refresh();
    }

    public int getPreviewBackgroundColor() {
        return previewBackgroundColor;
    }

    public void setPreviewBackgroundColor(int previewBackgroundColor) {
        this.previewBackgroundColor = previewBackgroundColor;
        refresh();
    }

    public int getPreviewTintColor() {
        return previewTintColor;
    }

    public void setPreviewTintColor(int previewTintColor) {
        this.previewTintColor = previewTintColor;
        refresh();
    }

    public int getPreviewTextColor() {
        return previewTextColor;
    }

    public void setPreviewTextColor(int previewTextColor) {
        this.previewTextColor = previewTextColor;
        refresh();
    }

    public float getPreviewTextSize() {
        return previewTextSize;
    }

    public void setPreviewTextSize(float previewTextSize) {
        this.previewTextSize = previewTextSize;
        refresh();
    }

    public int getIndexScrollHandleColor() {
        return indexScrollHandleColor;
    }

    public void setIndexScrollHandleColor(int indexScrollHandleColor) {
        this.indexScrollHandleColor = indexScrollHandleColor;
        refresh();
    }

    public PreviewStyle getPreviewStyle() {
        return previewStyle;
    }

    public void setPreviewStyle(PreviewStyle previewStyle) {
        this.previewStyle = previewStyle;
        refresh();
    }

    public int getIndexBackgroundStrokeColor() {
        return indexBackgroundStrokeColor;
    }

    public void setIndexBackgroundStrokeColor(int indexBackgroundStrokeColor) {
        this.indexBackgroundStrokeColor = indexBackgroundStrokeColor;
        refresh();
    }

    public float getIndexBackgroundStrokeWidth() {
        return indexBackgroundStrokeWidth;
    }

    public void setIndexBackgroundStrokeWidth(float indexBackgroundStrokeWidth) {
        this.indexBackgroundStrokeWidth = indexBackgroundStrokeWidth;
        refresh();
    }

    public IndexLocation getIndexLocation() {
        return indexLocation;
    }

    public void setIndexLocation(IndexLocation indexLocation) {
        this.indexLocation = indexLocation;
        refresh();
    }

    public float getIndexHorizontalMargin() {
        return indexHorizontalMargin;
    }

    public void setIndexHorizontalMargin(float indexHorizontalMargin) {
        this.indexHorizontalMargin = indexHorizontalMargin;
        refresh();
    }

    public float getIndexVerticalMargin() {
        return indexVerticalMargin;
    }

    public void setIndexVerticalMargin(float indexVerticalMargin) {
        this.indexVerticalMargin = indexVerticalMargin;
    }

    public int getPreviewBackgroundStrokeColor() {
        return previewBackgroundStrokeColor;
    }

    public void setPreviewBackgroundStrokeColor(int previewBackgroundStrokeColor) {
        this.previewBackgroundStrokeColor = previewBackgroundStrokeColor;
    }

    public float getPreviewBackgroundStrokeWidth() {
        return previewBackgroundStrokeWidth;
    }

    public void setPreviewBackgroundStrokeWidth(float previewBackgroundStrokeWidth) {
        this.previewBackgroundStrokeWidth = previewBackgroundStrokeWidth;
    }

    public boolean isShowingPreview() {
        return isShowingPreview;
    }

    public String[] getIndices() {
        return indices;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public String getCurrentIndexText() {
        return currentIndexText;
    }

    public float getIndicesBackgroundLeft() {
        return indicesBackgroundLeft;
    }

    public float getIndicesBackgroundTop() {
        return indicesBackgroundTop;
    }

    public float getIndicesBackgroundRight() {
        return indicesBackgroundRight;
    }

    public float getIndicesBackgroundBottom() {
        return indicesBackgroundBottom;
    }

    public float getIndicesBackgroundWidth() {
        return indicesBackgroundWidth;
    }

    public float getIndicesBackgroundHeight() {
        return indicesBackgroundHeight;
    }

    public float getPreviewBackgroundLeft() {
        return previewBackgroundLeft;
    }

    public float getPreviewBackgroundTop() {
        return previewBackgroundTop;
    }

    public float getPreviewBackgroundRight() {
        return previewBackgroundRight;
    }

    public float getPreviewBackgroundBottom() {
        return previewBackgroundBottom;
    }

    public float getPreviewBackgroundWidth() {
        return previewBackgroundWidth;
    }

    public float getPreviewBackgroundHeight() {
        return previewBackgroundHeight;
    }

    public interface Indices {
        HashMap<String, Integer> getIndicesMap();
    }

    public enum PreviewStyle {
        RECTANGLE, CIRCLE
    }

    public enum IndexLocation {
        RIGHT, LEFT
    }
}
