/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.systemui.R;

/**
 *
 */
public class QuickSettingsContainerView extends FrameLayout {

    // The number of columns in the QuickSettings grid
    private int mNumColumns;
    private int mNumFinalColumns;

    // Duplicate number of columns in the QuickSettings grid on landscape view
    private boolean mDuplicateColumnsLandscape;
    private boolean mHasFlipSettingsPanel;

    // The gap between tiles in the QuickSettings grid
    private float mCellGap;

    private Context mContext;
    private Resources mResources;
    private boolean mSingleRow;

    // Cell width for single row
    private int mCellWidth = -1;
    private int mMinCellWidth = 0;
    private int mMaxCellWidth = 0;

    public QuickSettingsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mResources = getContext().getResources();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QuickSettingsContainer, 0, 0);
        mSingleRow = a.getBoolean(R.styleable.QuickSettingsContainer_singleRow, false);

        updateResources();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // TODO: Setup the layout transitions
        LayoutTransition transitions = getLayoutTransition();
    }

    public void updateResources() {
        mCellGap = mResources.getDimension(R.dimen.quick_settings_cell_gap);
        mNumColumns = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_PER_ROW, 3, UserHandle.USER_CURRENT);

        // do not allow duplication on tablets or any device which does not have
        // flipsettings
        mHasFlipSettingsPanel = mResources.getBoolean(R.bool.config_hasFlipSettingsPanel);
        mDuplicateColumnsLandscape = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_PER_ROW_DUPLICATE_LANDSCAPE,
                1, UserHandle.USER_CURRENT) == 1
                        && mHasFlipSettingsPanel;
        QSSize size = getRibbonSize();
        mMinCellWidth = mResources.getDimensionPixelSize(R.dimen.qs_ribbon_width_min);
        mMaxCellWidth = mResources.getDimensionPixelSize(R.dimen.qs_ribbon_width_max);
        if (size == QSSize.Auto || size == QSSize.AutoNarrow) {
            mCellWidth = -1;
        } else {
            mCellWidth = mResources.getDimensionPixelSize(R.dimen.qs_ribbon_width_big);
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mDuplicateColumnsLandscape && isLandscape()) {
            mNumFinalColumns = mNumColumns * 2;
        } else {
            mNumFinalColumns = mNumColumns;
        }
        // Calculate the cell width dynamically
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float availableWidth = (width - getPaddingLeft() - getPaddingRight() -
                (mNumFinalColumns - 1) * mCellGap);
        float cellWidth = (float) Math.ceil(((float) availableWidth) / mNumFinalColumns);

        // Update each of the children's widths accordingly to the cell width
        int N = getChildCount();
        int cellHeight = 0;
        int cursor = 0;
        int totalWidth = 0;
        float cellGap = mCellGap;

        if (mSingleRow) {
            cellGap /= 2;
            cellHeight = MeasureSpec.getSize(heightMeasureSpec);
            if (mCellWidth > 0) {
                cellWidth = mCellWidth;
            } else {
                if (width <= 0) {
                    // On first layout pass the parent width is 0
                    // So set the maximum width possible here
                    cellWidth = mMaxCellWidth;
                } else {
                    int numColumns = 0;
                    for (int i = 0; i < N; ++i) {
                        QuickSettingsTileView v = (QuickSettingsTileView) getChildAt(i);
                        if (v.getVisibility() != View.GONE) {
                            numColumns += v.getColumnSpan();
                        }
                    }
                    if (numColumns == 0)
                        numColumns = 1; // Avoid division by zero
                    availableWidth -= (numColumns - 1) * cellGap;
                    cellWidth = (float) Math.floor(availableWidth / numColumns);
                    if (cellWidth < mMinCellWidth)
                        cellWidth = mMinCellWidth;
                    else if (cellWidth > mMaxCellWidth)
                        cellWidth = mMaxCellWidth;
                }
            }
        } else {
            availableWidth -= (mNumColumns - 1) * cellGap;
            cellWidth = (float) Math.floor(availableWidth / mNumColumns);
            cellHeight = getResources().getDimensionPixelSize(R.dimen.quick_settings_cell_height);
        }

        for (int i = 0; i < N; ++i) {
            // Update the child's width
            QuickSettingsTileView v = (QuickSettingsTileView) getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                int colSpan = v.getColumnSpan();
                lp.width = (int) ((colSpan * cellWidth) + (colSpan - 1) * cellGap);
                lp.height = cellHeight;

                if (mNumFinalColumns > 3 && (!isLandscape() || !mHasFlipSettingsPanel)) {
                    lp.height = (lp.width * mNumFinalColumns - 1) / mNumFinalColumns;
                }

                // Measure the child
                int newWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                int newHeightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                v.measure(newWidthSpec, newHeightSpec);
                cursor += colSpan;
                totalWidth += v.getMeasuredWidth() + cellGap;
            }
        }

        // Set the measured dimensions.  We always fill the tray width, but wrap to the height of
        // all the tiles.
        if (mSingleRow) {
            int totalHeight = cellHeight + getPaddingTop() + getPaddingBottom();
            if (totalWidth > 0)
                totalWidth -= cellGap; // No space at the end
            setMeasuredDimension(totalWidth, totalHeight);
        } else {
            // We always fill the tray width, but wrap to the height of all the
            // tiles.
            int numRows = (int) Math.ceil((float) cursor / mNumColumns);
            int newHeight = (int) ((numRows * cellHeight) + ((numRows - 1) * cellGap)) +
                    getPaddingTop() + getPaddingBottom();
            setMeasuredDimension(width, newHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int N = getChildCount();
        final int width = getWidth();

        int x = getPaddingStart();
        int y = getPaddingTop();
        int cursor = 0;

        if (mDuplicateColumnsLandscape && isLandscape()) {
            mNumFinalColumns = mNumColumns * 2;
        } else {
            mNumFinalColumns = mNumColumns;
        }

        float cellGap = mCellGap;

        if (mSingleRow) {
            cellGap /= 2;
        }

        for (int i = 0; i < N; ++i) {
            QuickSettingsTileView child = (QuickSettingsTileView) getChildAt(i);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            if (child.getVisibility() != GONE) {
                final int col = cursor % mNumFinalColumns;
                final int colSpan = child.getColumnSpan();

                final int childWidth = lp.width;
                final int childHeight = lp.height;

                int row = (int) (cursor / mNumFinalColumns);

                // Push the item to the next row if it can't fit on this one

                if ((col + colSpan) > mNumFinalColumns && !mSingleRow) {
                    x = getPaddingLeft();
                    y += lp.height + cellGap;
                    row++;
                }

                final int childLeft = isLayoutRtl() ? width - x - childWidth : x;
                final int childRight = childLeft + childWidth;

                final int childTop = y;
                final int childBottom = childTop + childHeight;

                // Layout the container
                child.layout(childLeft, childTop, childRight, childBottom);

                // Offset the position by the cell gap or reset the position and cursor when we
                // reach the end of the row

                cursor += child.getColumnSpan();
                if (cursor < (((row + 1) * mNumFinalColumns)) || mSingleRow) {
                    x += lp.width + cellGap;
                } else {
                    x = getPaddingLeft();
                    y += lp.height + cellGap;
                }
            }
        }
    }

    private boolean isLandscape() {
        return Resources.getSystem().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    public int getTileTextSize() {
        // get tile text size based on column count
        switch (mNumColumns) {
            case 5:
                return mResources.getDimensionPixelSize(R.dimen.qs_5_column_text_size);
            case 4:
                return mResources.getDimensionPixelSize(R.dimen.qs_4_column_text_size);
            case 3:
            default:
                return mResources.getDimensionPixelSize(R.dimen.qs_3_column_text_size);
        }
    }

    public int getTileTextPadding() {
        // get tile text padding based on column count
        switch (mNumColumns) {
            case 5:
                return mResources.getDimensionPixelSize(R.dimen.qs_5_column_text_padding);
            case 4:
                return mResources.getDimensionPixelSize(R.dimen.qs_4_column_text_padding);
            case 3:
            default:
                return mResources.getDimensionPixelSize(R.dimen.qs_tile_margin_below_icon);
        }
    }

    public int getTileTextColor() {
        int tileTextColor = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.QUICK_TILES_TEXT_COLOR, -2, UserHandle.USER_CURRENT);
        return tileTextColor;
    }

    public enum QSSize {
        Auto,
        AutoNarrow,
        Big,
        Narrow
    }

    public QSSize getRibbonSize() {
        int size = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.QS_QUICK_ACCESS_SIZE, 3, UserHandle.USER_CURRENT);
        switch (size) {
            case 0:
                return QSSize.Auto;
            case 1:
                return QSSize.AutoNarrow;
            case 2:
                return QSSize.Big;
            case 3:
                return QSSize.Narrow;
        }
        return QSSize.Auto;
    }
}
