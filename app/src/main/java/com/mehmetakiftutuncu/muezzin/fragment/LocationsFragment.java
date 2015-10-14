package com.mehmetakiftutuncu.muezzin.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerView;
import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerViewDecoration;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.WithContentStates;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.utilities.Web;
import com.squareup.okhttp.Callback;

import java.util.ArrayList;

import ru.vang.progressswitcher.ProgressWidget;

public abstract class LocationsFragment<T> extends Fragment implements WithContentStates,
                                                                       Callback,
                                                                       OnItemClickedListener {
    protected ProgressWidget progressWidget;
    protected IndexedRecyclerView recyclerView;
    protected ContentStates state;
    protected ArrayList<T> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_locations, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        progressWidget = (ProgressWidget) layout.findViewById(R.id.progressWidget_locations);

        recyclerView = (IndexedRecyclerView) layout.findViewById(R.id.recyclerView_locations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        IndexedRecyclerViewDecoration decoration = new IndexedRecyclerViewDecoration();
        recyclerView.addItemDecoration(decoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ((Button) progressWidget.findViewById(R.id.button_error_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryOnError(v);
            }
        });

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadItems(false);
    }

    @Override
    public void changeStateTo(final ContentStates newState) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == null || !state.equals(newState)) {
                    state = newState;

                    switch (newState) {
                        case LOADING:
                            progressWidget.showProgress(true);
                            break;
                        case ERROR:
                            progressWidget.showError(true);
                            break;
                        case CONTENT:
                            progressWidget.showContent(true);
                            break;
                        case NO_CONTENT:
                            progressWidget.showEmpty(true);
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void retryOnError(View retryButton) {
        loadItems(false);
    }

    public abstract void setItems(ArrayList<T> items, boolean saveData);

    public abstract void loadItems(boolean forceDownload);

    public abstract void downloadItems();
}
