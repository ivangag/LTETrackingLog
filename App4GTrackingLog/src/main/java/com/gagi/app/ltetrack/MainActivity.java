package com.gagi.app.ltetrack;

import android.app.ActionBar;
import android.app.Application;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity
        implements FilterFragment.OnHeadlineSelectedListener{
    final static String  CUSTOM_INTENT_CONNECTIVITY_CHANGED = "CUSTOM_INTENT_CONNECTIVITY_CHANGED";
    final static String  CUSTOM_EXTRA_NETWORK_INFO = "CUSTOM_EXTRA_NETWORK_INFO";
    final static String  FRAGMENT_NETWORK_LIST_TAG = "FRAGMENT_NETWORK_LIST_TAG";
    final static String  FRAGMENT_NETWORK_FILTER_TAG = "FRAGMENT_NETWORK_FILTER_TAG";
    final String TXT_NET_STATUS = "TXT_NET_STATUS";

    public static Application application = null;



    final  String APP_TAG = "com.gagi.app.ltetrack";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(application == null)
            application = this.getApplication();
        setContentView(R.layout.activity_main);
        FilterFragment filterFragment = new FilterFragment();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, filterFragment,FRAGMENT_NETWORK_FILTER_TAG)
                    .add(R.id.container, new PlaceholderFragment(),FRAGMENT_NETWORK_LIST_TAG)
                    .commit();
        }
        else
        {
            //mTxtNetStatus = new StringBuilder(savedInstanceState.getString(TXT_NET_STATUS));
        }


        //StartPhoneStateListening();
    }

    public void workwithListActionBar()
    {
        ActionBar bar = this.getActionBar();
        /*
        bar.setTitle(tag);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(
                new SimpleSpinnerArrayAdapter(this),
                new ListListener(this,this));
                */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onArticleSelected(String filterType)
    {
        PlaceholderFragment networkFrag = (PlaceholderFragment)
                getFragmentManager().findFragmentByTag(FRAGMENT_NETWORK_LIST_TAG);

        if (networkFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the fragment to update its content
            networkFrag.UpdateFilterNetworkMessages(filterType);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends ListFragment implements AdapterView.OnItemSelectedListener,
            GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener,
            NetworkInfoAdapter.OnDataItemChangedListener
    {

        // IDs for menu items
        private static final int MENU_EDIT_SINGLE = Menu.FIRST + 2;
        private static final int MENU_DELETE_SINGLE = Menu.FIRST + 3;
        final String LOC_UNAVAILABLE = "LOC:UNAVAILABLE";
        final  String APP_TAG = "com.gagi.app.ltetrack";
        private final int mCcurrentApiVersion;

        NetworkInfoAdapter mNetAdapter;
        LocationClient mLocationClient;

        ArrayList<String> mArrayItemsNetworkInfo;
        ArrayList<String> mArrayItemsNetworkInfoWithFilter;
        ArrayAdapter mArrayAdapterNetworkInfo;

        private ShareActionProvider mShareActionProvider;
        private boolean mIsShareIntentPendind = false;

        public PlaceholderFragment()
        {
            mCcurrentApiVersion = android.os.Build.VERSION.SDK_INT;
        }

        /** Defines a default (dummy) share intent to initialize the action provider.
         * However, as soon as the actual content to be used in the intent
         * is known or changes, you must update the share intent by again calling
         * mShareActionProvider.setShareIntent()
         */
        private Intent getDefaultIntent() {
            InputStream is = getResources().openRawResource(R.drawable.trash_48x48);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +  "/imageLTETrack.pgn");
            WriteContentToFileFromInputStream(is, file);
            Uri uri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.putExtra(Intent.EXTRA_STREAM, uri)
//                .setType("image/pgn");

            return intent;
        }

        private void updateShareIntentWithText() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,mNetAdapter.getRawItemsInfo()).setType("text/plain");
            if(mShareActionProvider != null)
            {
                mShareActionProvider.setShareIntent(intent);
                mIsShareIntentPendind = false;
            }
            else
                mIsShareIntentPendind = true;
        }



        private void WriteContentToFileFromInputStream(InputStream input, File file)
        {
            try {
                OutputStream output = null;
                try {
                    output = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    try {
                        final byte[] buffer = new byte[1024];
                        int read;

                        while ((read = input.read(buffer)) != -1)
                            output.write(buffer, 0, read);

                        output.flush();
                    } finally {
                        output.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            finally {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
        {
            /*
            menu.add(Menu.NONE, MENU_DELETE,Menu.NONE,"Delete All")
                    .setIcon(R.drawable.trash_48x48)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(Menu.NONE, MENU_DUMP, Menu.NONE, "Dump to log").setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            */
            inflater.inflate(R.menu.fragment_main,menu);

            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getActivity().getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();
                }
            });

            MenuItem shareItem = menu.findItem(R.id.action_share);
            mShareActionProvider = (ShareActionProvider)shareItem.getActionProvider();

            mShareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);

            mShareActionProvider.setShareIntent(getDefaultIntent());
            if(mIsShareIntentPendind)
                updateShareIntentWithText();
            mShareActionProvider.setOnShareTargetSelectedListener(new ShareActionProvider.OnShareTargetSelectedListener() {
                @Override
                public boolean onShareTargetSelected(ShareActionProvider source, Intent intent) {

                    return false;
                }
            });

           searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
               @Override
               public boolean onQueryTextSubmit(String query) {
                   //Toast.makeText(getActivity().getApplicationContext(), "onQueryTextSubmit:" + query, Toast.LENGTH_SHORT).show();
                   mNetAdapter.update(query.toUpperCase());
                   return true;
               }

               @Override
               public boolean onQueryTextChange(String newText) {
                   //Toast.makeText(getActivity().getApplicationContext(), "onQueryTextChange:" + newText,Toast.LENGTH_SHORT).show();
                   mNetAdapter.update(newText.toUpperCase());
                   return true;
               }
           });

            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    mNetAdapter.getFilter().filter(getResources().getString(R.string.filterALL));
                    return true;
                }
            });
            super.onCreateOptionsMenu(menu,inflater);

        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    mNetAdapter.clear();
                    return true;
                case R.id.action_search:
                    //dump();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
        public void onCreateContextMenu(ContextMenu menu,
                                        View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);


            menu.setHeaderTitle("Item Context Menu");

            menu.add(0, MENU_EDIT_SINGLE, 0,"Edit");
            menu.add(0, MENU_DELETE_SINGLE, 0, "Delete");

        }


        @Override
        public boolean onContextItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case MENU_EDIT_SINGLE:
                    Toast.makeText(getActivity().getApplicationContext(),"Edit Item Menu Selected",Toast.LENGTH_SHORT).show();
//                    any_function();//add your functionality here i.e. what you want to do
                    return true;
                case MENU_DELETE_SINGLE:
                    mNetAdapter.delete((NetworkInfoItem) mNetAdapter.getItem(((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position));
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
        @Override
        public void onConnected(Bundle dataBundle) {
            this.StartPhoneStateListening();
            // Display the connection status
            Toast.makeText(this.getActivity().getApplicationContext(), "Google Play Services Connected", Toast.LENGTH_SHORT).show();
            LogManager.GetInstance().WriteToFile("Google Play Services Connected");

        }

        @Override
        public void onDisconnected() {
            // Display the connection status
            Toast.makeText(this.getActivity().getApplicationContext(), "Google Play Services Disconnected", Toast.LENGTH_SHORT).show();
            LogManager.GetInstance().WriteToFile("Google Play Services Disconnected");
        }

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            String filterSelected = (String)parent.getItemAtPosition(pos);

            mArrayItemsNetworkInfoWithFilter.clear();
            mArrayAdapterNetworkInfo.clear();

            if(filterSelected.equals("ALL"))
            {
                for(String item:mArrayItemsNetworkInfo)
                {
                    mArrayItemsNetworkInfoWithFilter.add(0,item);
                }
            }
            else
            {
                for(String item:mArrayItemsNetworkInfo)
                {
                    if(item.contains(filterSelected))
                        mArrayItemsNetworkInfoWithFilter.add(0,item);
                }
            }

            mArrayAdapterNetworkInfo.addAll(mArrayItemsNetworkInfoWithFilter);
            mArrayAdapterNetworkInfo.notifyDataSetChanged();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }


        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);

            mLocationClient = new LocationClient(getActivity().getApplicationContext(),this,this);
            mLocationClient.connect();
            //this.StartPhoneStateListening();
            // Register mMessageReceiver to receive messages.
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mMessageReceiver,
                    new IntentFilter(CUSTOM_INTENT_CONNECTIVITY_CHANGED));

        }
        @Override
        public void onDestroy() {
            // Unregister since the activity is about to be closed.
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mMessageReceiver);
            mLocationClient.disconnect();
            super.onDestroy();
        }

        @Override public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setHasOptionsMenu(true);
            registerForContextMenu(this.getListView());
            System.out.println("NetworkInfoListFragment.onActivityCreated");

            if (mNetAdapter == null) {
                // Create an empty adapter we will use to display the loaded data.
                mNetAdapter = new NetworkInfoAdapter(getActivity().getApplicationContext());
                mNetAdapter.addOnDataItemChangedListener(this);
                setListAdapter(mNetAdapter);
            }

        }

        private void StartPhoneStateListening() {
            final TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(TELEPHONY_SERVICE);
            PhoneStateListener phoneStateListener = new PhoneStateListener()
            {
                public int mSignalGsmLevel;

                @Override
                public void onCellInfoChanged (List<CellInfo> cellInfo)
                {
                    for(CellInfo cell:cellInfo)
                    {
                        //Log.i(APP_TAG,"onCellInfoChanged: " + cell.toString());
                    }

                }
                @Override
                public void onServiceStateChanged (ServiceState serviceState)
                {
                    String msg = "";

                    msg = serviceState.toString().substring(0,40);
                    LogManager.GetInstance().WriteToFile(msg);
                    String loc = LOC_UNAVAILABLE;
                    Location myLoc = null;
                    if(mLocationClient.isConnected())
                    {
                        myLoc = mLocationClient.getLastLocation();
                    }
                    if(myLoc != null)
                        loc = myLoc.toString();
                    msg = LogManager.GetInstance().GetCurrentDateTimeFormatted() + " - " + msg + loc;
                    NetworkInfoItem networkInfoItem = new NetworkInfoItem(msg);
                    networkInfoItem.setLocation(myLoc);

                    if (mCcurrentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                    {
                        // Do something for Api level 17 and above versions
                        //List<CellInfo> cells =  telephonyManager.getAllCellInfo();
                        //                       mSignalGsmLevel = cellInfo.getCellSignalStrength().getAsuLevel();
                    }
                    else
                    {
                        // do something for phones running an SDK before Api level 17
                    }
                    networkInfoItem.setOperatorName(serviceState.getOperatorAlphaShort());
                    networkInfoItem.setNetworkType(telephonyManager.getNetworkType());
                    networkInfoItem.setTimeEventInfo(LogManager.GetInstance().GetCurrentDateTimeFormatted());
                    networkInfoItem.setAntennaBars(mSignalGsmLevel);

                    mNetAdapter.add(networkInfoItem);
                    msg = "onServiceStateChanged: " + serviceState.toString();
                    LogManager.GetInstance().WriteToFile(msg);
                    Log.i(APP_TAG, msg);
                }
                @Override
                public void onSignalStrengthsChanged(SignalStrength signalStrength)
                {
                    if(signalStrength.isGsm())
                        mSignalGsmLevel = signalStrength.getGsmSignalStrength();
                    Log.i(APP_TAG,"onSignalStrengthsChanged=" + signalStrength.toString());
                }
                @Override
                public void onCallStateChanged(int state, String number) {
                    String currentPhoneState = null;
                    switch (state) {
                        case TelephonyManager.CALL_STATE_RINGING:
                            currentPhoneState = "Device is ringing. Call from " + number + ".\n\n";
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            currentPhoneState = "Device call state is currently Off Hook.\n\n";
                            break;
                        case TelephonyManager.CALL_STATE_IDLE:
                            currentPhoneState = "Device call state is currently Idle.\n\n";
                            break;
                    }
                }
            };
            //telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_SERVICE_STATE
            //       |PhoneStateListener.LISTEN_SIGNAL_STRENGTHS  | PhoneStateListener.LISTEN_CALL_STATE);
            telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_SERVICE_STATE
                    |PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }

        // Our handler for received Intents. This will be called whenever an Intent
        // with an action named "custom-event-name" is broadcasted.
        private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(CUSTOM_INTENT_CONNECTIVITY_CHANGED))
                {
                    // Get extra data included in the Intent
                    //String message = intent.getStringExtra(CUSTOM_EXTRA_NETWORK_INFO);
                    NetworkInfo info = intent.getParcelableExtra(CUSTOM_EXTRA_NETWORK_INFO);
                    String msg = "NETWORKINFO: " + info.toString() ;
                    Log.i(APP_TAG, msg);
                    String loc = LOC_UNAVAILABLE;
                    Location myLoc = null;
                    if(mLocationClient.isConnected())
                    {
                        myLoc = mLocationClient.getLastLocation();
                        loc = myLoc.toString();
                    }
                    msg = LogManager.GetInstance().GetCurrentDateTimeFormatted() + " - " + msg + loc;
                    NetworkInfoItem networkInfoItem = new NetworkInfoItem(msg);
                    networkInfoItem.setLocation(myLoc);
                    //mNetAdapter.add(networkInfoItem);
                    LogManager.GetInstance().WriteToFile(msg);
                }
            }
        };

        public void UpdateFilterNetworkMessages(String networkType)
        {
            String filter = "";
            if(networkType.equals("HSPAP"))
                filter = getActivity().getResources().getString(R.string.filterHSPAP);
            else if(networkType.equals("UMTS"))
                filter = getActivity().getResources().getString(R.string.filterUMTS);
            else if(networkType.equals("LTE"))
                filter = getActivity().getResources().getString(R.string.filterLTE);
            else
                filter = getActivity().getResources().getString(R.string.filterALL);
            mNetAdapter.getFilter().filter(filter);
        }

        @Override
        public void OnDataItemChanged() {
            updateShareIntentWithText();
        }
    }

}
