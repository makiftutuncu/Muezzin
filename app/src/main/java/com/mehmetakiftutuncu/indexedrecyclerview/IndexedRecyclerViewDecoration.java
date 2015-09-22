package com.mehmetakiftutuncu.indexedrecyclerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class IndexedRecyclerViewDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);

        IndexedRecyclerView indexedRecyclerView = (IndexedRecyclerView) parent;

        String[] indices = indexedRecyclerView.getIndices();

        LinearLayoutManager layoutManager = (LinearLayoutManager) indexedRecyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();
        int itemCount        = layoutManager.getItemCount();

        if (visibleItemCount * 2 >= itemCount || indices.length <= 5) {
            // Too few items, do not draw at all!
            indexedRecyclerView.setIsPreviewAndIndicesEnabled(false);
            return;
        }

        int currentIndex        = indexedRecyclerView.getCurrentIndex();
        String currentIndexText = indexedRecyclerView.getCurrentIndexText();

        if (indexedRecyclerView.isShowingPreview() && currentIndexText != null && !currentIndexText.equals("")) {
            Paint tint = new Paint();
            tint.setColor(indexedRecyclerView.getPreviewTintColor());

            // Draw tint
            canvas.drawRect(indexedRecyclerView.getLeft(), indexedRecyclerView.getTop(), indexedRecyclerView.getWidth(), indexedRecyclerView.getHeight(), tint);

            float previewBackgroundLeft        = indexedRecyclerView.getPreviewBackgroundLeft();
            float previewBackgroundTop         = indexedRecyclerView.getPreviewBackgroundTop();
            float previewBackgroundRight       = indexedRecyclerView.getPreviewBackgroundRight();
            float previewBackgroundBottom      = indexedRecyclerView.getPreviewBackgroundBottom();
            float previewBackgroundWidth       = indexedRecyclerView.getPreviewBackgroundWidth();
            float previewBackgroundHeight      = indexedRecyclerView.getPreviewBackgroundHeight();
            float previewBackgroundStrokeWidth = indexedRecyclerView.getPreviewBackgroundStrokeWidth();
            float previewTextSize              = indexedRecyclerView.getPreviewTextSize();

            Paint previewBackground = new Paint();
            previewBackground.setStyle(Paint.Style.FILL);
            previewBackground.setColor(indexedRecyclerView.getPreviewBackgroundColor());

            Paint previewBackgroundStroke = new Paint();
            previewBackgroundStroke.setStyle(Paint.Style.STROKE);
            previewBackgroundStroke.setStrokeWidth(previewBackgroundStrokeWidth);
            previewBackgroundStroke.setColor(indexedRecyclerView.getPreviewBackgroundStrokeColor());

            // Draw preview
            if (indexedRecyclerView.isPreviewRectangle()) {
                canvas.drawRect(previewBackgroundLeft - (previewBackgroundStrokeWidth / 2), previewBackgroundTop - (previewBackgroundStrokeWidth / 2), previewBackgroundRight + (previewBackgroundStrokeWidth / 2), previewBackgroundBottom + (previewBackgroundStrokeWidth / 2), previewBackgroundStroke);
                canvas.drawRect(previewBackgroundLeft, previewBackgroundTop, previewBackgroundRight, previewBackgroundBottom, previewBackground);
            } else if (indexedRecyclerView.isPreviewCircle()) {
                canvas.drawCircle(previewBackgroundLeft + (previewBackgroundWidth / 2), previewBackgroundTop + (previewBackgroundHeight / 2), ((previewBackgroundWidth + previewBackgroundStrokeWidth) / 2), previewBackgroundStroke);
                canvas.drawCircle(previewBackgroundLeft + (previewBackgroundWidth / 2), previewBackgroundTop + (previewBackgroundHeight / 2), previewBackgroundWidth / 2, previewBackground);
            }

            Paint previewText = new Paint();
            previewText.setColor(indexedRecyclerView.getPreviewTextColor());
            previewText.setTextSize(previewTextSize);
            previewText.setTextAlign(Paint.Align.CENTER);
            previewText.setStyle(Paint.Style.FILL);
            previewText.setAntiAlias(true);
            previewText.setFakeBoldText(true);

            Rect previewTextBounds = new Rect();
            previewText.getTextBounds(currentIndexText, 0, currentIndexText.length(), previewTextBounds);

            float previewTextLeft = previewBackgroundLeft + (previewBackgroundWidth / 2);
            float previewTextTop  = previewBackgroundTop + (previewBackgroundHeight / 2) + ((previewTextBounds.height() - previewTextBounds.bottom) / 2);

            // Draw preview text
            canvas.drawText(currentIndexText, previewTextLeft, previewTextTop, previewText);
        }

        float indicesBackgroundLeft        = indexedRecyclerView.getIndicesBackgroundLeft();
        float indicesBackgroundTop         = indexedRecyclerView.getIndicesBackgroundTop();
        float indicesBackgroundRight       = indexedRecyclerView.getIndicesBackgroundRight();
        float indicesBackgroundBottom      = indexedRecyclerView.getIndicesBackgroundBottom();
        float indicesBackgroundWidth       = indexedRecyclerView.getIndicesBackgroundWidth();
        float indicesBackgroundHeight      = indexedRecyclerView.getIndicesBackgroundHeight();
        float indicesBackgroundStrokeWidth = indexedRecyclerView.getIndexBackgroundStrokeWidth();

        Paint indicesBackground = new Paint();
        indicesBackground.setStyle(Paint.Style.FILL);
        indicesBackground.setColor(indexedRecyclerView.getIndexBackgroundColor());

        // Draw indices background
        if (indicesBackgroundStrokeWidth > 0) {
            Paint indicesBackgroundStroke = new Paint();
            indicesBackgroundStroke.setStyle(Paint.Style.STROKE);
            indicesBackgroundStroke.setStrokeWidth(indicesBackgroundStrokeWidth);
            indicesBackgroundStroke.setColor(indexedRecyclerView.getIndexBackgroundStrokeColor());

            canvas.drawRect(indicesBackgroundLeft - (indicesBackgroundStrokeWidth / 2), indicesBackgroundTop - (indicesBackgroundStrokeWidth / 2), indicesBackgroundRight + (indicesBackgroundStrokeWidth / 2), indicesBackgroundBottom + (indicesBackgroundStrokeWidth / 2), indicesBackgroundStroke);
        }
        canvas.drawRect(indicesBackgroundLeft, indicesBackgroundTop, indicesBackgroundRight, indicesBackgroundBottom, indicesBackground);

        Paint index = new Paint();
        index.setAntiAlias(true);
        index.setStyle(Paint.Style.FILL);
        index.setTextAlign(Paint.Align.CENTER);
        index.setTextSize(indexedRecyclerView.getIndexTextSize());
        index.setColor(indexedRecyclerView.getIndexTextColor());

        float initialAverageHeight   = getAverageHeight(indices, index);
        float initialIndexMargin     = initialAverageHeight / 2;
        String[] optimizedIndices    = getOptimizedIndices(indices, indicesBackgroundHeight, initialAverageHeight, initialIndexMargin);
        float optimizedAverageHeight = getAverageHeight(optimizedIndices, index);
        float optimizedIndexMargin   = getIndexMargin(optimizedIndices.length, indicesBackgroundHeight, optimizedAverageHeight);

        if (indexedRecyclerView.isShowingPreview()) {
            Paint scrollHandle = new Paint();
            scrollHandle.setStyle(Paint.Style.FILL);
            scrollHandle.setColor(indexedRecyclerView.getIndexScrollHandleColor());

            int scrollHandlePosition = currentIndex <= 1 ? 1 : (currentIndex < (optimizedIndices.length - 2) ? currentIndex : (optimizedIndices.length - 2));
            float scrollHandleTop    = indicesBackgroundTop + ((scrollHandlePosition - 1) * (optimizedAverageHeight + optimizedIndexMargin)) + (optimizedIndexMargin / 2);
            float scrollHandleHeight = ((scrollHandlePosition + 2) * (optimizedAverageHeight + optimizedIndexMargin)) + (optimizedIndexMargin / 2);
            float scrollHandleBottom = indicesBackgroundTop + scrollHandleHeight;

            // Draw scroll handle
            canvas.drawRect(indicesBackgroundLeft, scrollHandleTop, indicesBackgroundRight, scrollHandleBottom, scrollHandle);
        }

        // Draw indices
        for (int i = 0, size = optimizedIndices.length; i < size; i++) {
            float indexLeft = indicesBackgroundLeft + (indicesBackgroundWidth / 2);
            float indexTop  = indicesBackgroundTop + ((optimizedAverageHeight + optimizedIndexMargin) * (i + 1));

            canvas.drawText(optimizedIndices[i], indexLeft, indexTop, index);
        }
    }

    private String[] getOptimizedIndices(String[] indices, float height, float averageHeight, float indexMargin) {
        if (indices == null || indices.length <= 3 || ((indices.length + 2) * (averageHeight + indexMargin)) + indexMargin < height) {
            return indices;
        } else {
            int removePoint1 = (indices.length / 4) - 1;
            int removePoint2 = removePoint1 * 2;
            int removePoint3 = removePoint1 * 3;

            String[] newIndices = new String[indices.length - 3];

            for (int i = 0, j = 0, size = indices.length, newSize = newIndices.length; i < size && j < newSize; i++) {
                if (i != removePoint1 && i != removePoint2 && i != removePoint3) {
                    newIndices[j] = indices[i];
                    j++;
                }
            }

            return getOptimizedIndices(newIndices, height, averageHeight, indexMargin);
        }
    }

    private float getAverageHeight(String[] indices, Paint indexPaint) {
        Rect indexBounds = new Rect();
        float average = 0;

        for (int i = 0, size = indices.length; i < size; i++) {
            indexPaint.getTextBounds(indices[i], 0, 1, indexBounds);
            average += indexBounds.height();
        }

        return average / indices.length;
    }

    private float getIndexMargin(int indexCount, float indicesBackgroundHeight, float averageHeight) {
        return (indicesBackgroundHeight - (indexCount * averageHeight)) / (indexCount + 1);
    }
}
