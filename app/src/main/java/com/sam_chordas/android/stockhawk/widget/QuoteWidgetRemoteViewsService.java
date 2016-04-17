package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;


/**
 * Created by Natarajan on 4/16/2016.
 */

// We need a service class to ensure that it is running in background to Observe the changes if any

// and provide update

public class QuoteWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListViewFactory(this.getApplicationContext(), intent);
    }
}


class ListViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private Cursor mCursor;
    private int mAppWidgetId;

    public ListViewFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // logic in onDataSetChanged() follows this call
    }

    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    public int getCount() {
        return mCursor.getCount();
    }


    // will be called automatically

    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
        String ticker = "";
        String bidPrice = "";
        String change = "";
        int isUp = 1;
        if (mCursor.moveToPosition(position)) {
            final int symbolColIndex = mCursor.getColumnIndex(QuoteColumns.SYMBOL);
            final int bidPriceColIndex = mCursor.getColumnIndex(QuoteColumns.BIDPRICE);
            final int changeColIndex = mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE);
            final int isUpIndex = mCursor.getColumnIndex(QuoteColumns.ISUP);
            ticker = mCursor.getString(symbolColIndex);
            bidPrice = mCursor.getString(bidPriceColIndex);
            change = mCursor.getString(changeColIndex);
            isUp = mCursor.getInt(isUpIndex);
        }

        // Fill data in UI
        final int itemId = R.layout.widget_collection_item;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.stock_symbol, ticker);
        rv.setTextViewText(R.id.bid_price, bidPrice);
        rv.setTextViewText(R.id.change, change);
        if (isUp == 1) {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        // Set the click intent
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putString("ticker", ticker);
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);
        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // on Change of data, we need to refresh the data
        // cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }
}