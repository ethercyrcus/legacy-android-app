package com.example.cameron.ethereumtest1.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cameron.ethereumtest1.R;
import com.example.cameron.ethereumtest1.activities.MainActivity;
import com.example.cameron.ethereumtest1.adapters.PublicationsRecyclerViewAdapter;
import com.example.cameron.ethereumtest1.database.DatabaseHelper;
import com.example.cameron.ethereumtest1.ethereum.EthereumClientService;
import com.example.cameron.ethereumtest1.util.PrefUtils;

import java.util.ArrayList;

public class PublicationListFragment extends Fragment {

    private final static String TAG = PublicationListFragment.class.getName();

    private RecyclerView mSubscribedRecyclerView;
    private EditText mSearchEditText;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case EthereumClientService.UI_UPDATE_PUBLICATION_LIST:
                    reloadPublicationsDB();
                    break;
            }
        }
    };

    public static PublicationListFragment newInstance() {
        PublicationListFragment fragment = new PublicationListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(EthereumClientService.UI_UPDATE_PUBLICATION_LIST);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
        bm.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_publications, container, false);

        mSubscribedRecyclerView = (RecyclerView) v.findViewById(R.id.publicationsFragmentPublicationsList);
        mSearchEditText = (EditText) v.findViewById(R.id.search);

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    showSearchResults(mSearchEditText.getText().toString(), SearchResultsPublicationsFragment.SORT_CATEGORY_UNIQUE_SUPPORTERS, true);
                    return true;
                }
                return false;
            }
        });

        reloadPublicationsDB();
        loadPublicationsFromEthereumChain();

        return v;
    }

    private void reloadPublicationsDB() {
        mSubscribedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSubscribedRecyclerView.setAdapter(new PublicationsRecyclerViewAdapter((AppCompatActivity) getActivity(), new DatabaseHelper(getContext()).getPublicationsSubscribedCursor()));
    }

    private void loadPublicationsFromEthereumChain() {
        if ((PrefUtils.shouldUpdateAccountContentList(getActivity()))) {
            try {
                getActivity().startService(new Intent(getContext(), EthereumClientService.class)
                        .setAction(EthereumClientService.ETH_FETCH_PUBLICATION_LIST));
            } catch (Exception e) {
                Log.e(TAG, "Error requesting publications list: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(getContext());
        bm.unregisterReceiver(mBroadcastReceiver);
    }

    public void showSearchResults(String searchTerm, int sortCategoryUniqueSupporters, boolean descending) {
        ((MainActivity)getActivity()).showSearchResultsFragment(searchTerm, sortCategoryUniqueSupporters, descending);
    }
}
