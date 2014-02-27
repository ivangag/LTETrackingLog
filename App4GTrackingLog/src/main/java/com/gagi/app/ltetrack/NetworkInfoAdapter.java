package com.gagi.app.ltetrack;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by igaglioti on 17/02/14.
 */
public class NetworkInfoAdapter extends BaseAdapter implements Filterable{

    private final Context mContext;
    private final MainActivity.PlaceholderFragment mListViewerFragment = null;

    // List of NetworkInfoItems
    private List<NetworkInfoItem> mItems = new ArrayList<NetworkInfoItem>();
    private final List<NetworkInfoItem> mOriginalItems = new ArrayList<NetworkInfoItem>();
    private String mLastFilter;
    OnDataItemChangedListener mDataNotifierCallBack;

    public interface OnDataItemChangedListener
    {
        public void OnDataItemChanged();
    }

    public void setOnDataItemChangedListener(MainActivity.PlaceholderFragment fragment)
    {
        mDataNotifierCallBack = fragment;
    }

    public  NetworkInfoAdapter(Context context)
    {
        mContext = context;
        mLastFilter = context.getResources().getString(R.string.filterALL);
    }

    public String getRawItemsInfo()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for(NetworkInfoItem item:mOriginalItems)
        {
            stringBuilder.append(item.getmTimeEventInfo() + ";" +
                    item.getNetworkTypeName() + ";" +
                    ((item.getLocation() != null) ? String.valueOf(item.getLocation().getLatitude()) : "") + ";" +
                    ((item.getLocation() != null) ? String.valueOf(item.getLocation().getLongitude()) : "")
            ).append("\r\n");
        }
        return  stringBuilder.toString();
    }


    @Override
    public int getCount() {
        return mItems.size();
    }

    public void update(String queryText)
    {
        mItems = this.getFilteredResults(queryText,true);
        notifyDataSetChanged();
    }

    // Clears the list adapter of all items.
    public void clear(){

        mOriginalItems.clear();
        mItems.clear();
        notifyDataSetChanged();
    }

    public void delete(NetworkInfoItem item)
    {
        for(NetworkInfoItem itemNet:mOriginalItems)
        {
            if(itemNet.hashCode() == item.hashCode())
            {
                mOriginalItems.remove(itemNet);
                break;
            }
        }
        this.getFilter().filter(mLastFilter);
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position)
    {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    //Create a View to display the NetworkInfoItem
   // at specified position in mItems
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final NetworkInfoItem networkInfoItem = (NetworkInfoItem)getItem(position);

        // TODO - Inflate the View for this ToDoItem
        // from todo_item.xml.
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout itemLayout = (RelativeLayout)inflater.inflate(R.layout.networkinfo_item,null);


        TextView txtSumInfo = (TextView)itemLayout.findViewById(R.id.txtViewNetworkSummary);
        txtSumInfo.setLongClickable(true);
        if(TelephonyManager.NETWORK_TYPE_LTE == networkInfoItem.getNetworkType())
            txtSumInfo.setTextColor(Color.RED);
        Button btnShowMap = (Button)itemLayout.findViewById(R.id.btnShowNetworkInfoMap);
        String latitude = "",longitude = "";

        if(networkInfoItem.getLocation() == null)
            btnShowMap.setEnabled(false);

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(networkInfoItem.getLocation() != null)
                {
                    String latitude,longitude;
                    latitude = String.valueOf(networkInfoItem.getLocation().getLatitude());
                    longitude = String.valueOf(networkInfoItem.getLocation().getLongitude());
                    String label = networkInfoItem.getOperatorName() + "[" + networkInfoItem.getNetworkTypeName() + "]";
                    String uriBegin = "geo:" + latitude + "," + longitude;
                    String query = latitude + "," + longitude + "(" + label + ")";
                    String encodedQuery = Uri.encode(query);
                    String uriString = uriBegin + "?q=" + encodedQuery + "&z=13";
                    Uri uri = Uri.parse(uriString);
                    showMap(uri);
                }
            }
        });


        String itemText = "[" + networkInfoItem.getmTimeEventInfo() + "] " +
                "[" + networkInfoItem.getOperatorName() + "] " +
                "[" + networkInfoItem.getNetworkTypeName() + "] ";
        networkInfoItem.setRawText(itemText);
        txtSumInfo.setText(/*networkInfoItem.getSummaryInfo() + */
                itemText
                //"[LOC: " + latitude + "," + longitude + "]"
        );

        return itemLayout;
    }

    public void showMap(Uri geoLocation) {
        //geo:latitude,longitude
        //Show the map at the given longitude and latitude.
        //       Example: "geo:47.6,-122.3"
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (mContext != null) {
            mContext.startActivity(intent);
        }
    }

    // Add a NetworkInfoItem to the adapter
    // Notify observers that the data set has changed
    public void add(NetworkInfoItem item) {

        mOriginalItems.add(item);
        this.getFilter().filter(mLastFilter);
        //mItems.add(0,item);
        //notifyDataSetChanged();
        mDataNotifierCallBack.OnDataItemChanged();
    }

    @Override
    public Filter getFilter()
    {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mLastFilter = constraint.toString();
                //Log.d(Constants.TAG, "**** PUBLISHING RESULTS for: " + constraint);
                mItems = (List<NetworkInfoItem>) results.values;
                NetworkInfoAdapter.this.notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                //Log.d(Constants.TAG, "**** PERFORM FILTERING for: " + constraint);
                List<NetworkInfoItem> filteredResults = getFilteredResults(constraint,false);

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }

    private List<NetworkInfoItem> getFilteredResults(CharSequence constraint, boolean IsRawSearch) {
        List<NetworkInfoItem> values = new ArrayList<NetworkInfoItem>();
        for(NetworkInfoItem item:mOriginalItems)
        {
            if(!IsRawSearch)
            {
                if((mContext.getString(R.string.filterALL).equals(constraint))
                    || item.getNetworkTypeName().contains(constraint))
                    values.add(item);
            }
            else
            {
                if(item.getRawText().contains(constraint))
                    values.add(item);
            }
        }
        Collections.sort(values, new Comparator<NetworkInfoItem>() {
            @Override
            public int compare(NetworkInfoItem lhs, NetworkInfoItem rhs)
            {
                Date mleftDate,mrightDate;
                try {
                    mleftDate = NetworkInfoItem.FORMAT.parse(lhs.getmTimeEventInfo());
                } catch (ParseException e)
                {
                    mleftDate = new Date();
                    e.printStackTrace();
                }
                try {
                    mrightDate = NetworkInfoItem.FORMAT.parse(rhs.getmTimeEventInfo());
                } catch (ParseException e)
                {
                    mrightDate = new Date();
                    e.printStackTrace();
                }
                return mleftDate.compareTo(mrightDate);
            }
        });
        return  values;
    }
}
