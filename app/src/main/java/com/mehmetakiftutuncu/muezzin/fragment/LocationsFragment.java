package com.mehmetakiftutuncu.muezzin.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.WithContentStates;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;
import com.squareup.okhttp.Callback;

import java.util.ArrayList;

import ru.vang.progressswitcher.ProgressWidget;

public abstract class LocationsFragment<T> extends Fragment implements WithContentStates,
                                                                       Callback,
                                                                       OnItemClickedListener {
    protected ProgressWidget progressWidget;
    protected RecyclerView recyclerView;
    protected RecyclerFastScroller recyclerFastScroller;
    protected ContentStates state;
    protected ArrayList<T> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_locations, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        progressWidget = (ProgressWidget) layout.findViewById(R.id.progressWidget_locations);

        recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerView_locations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerFastScroller = (RecyclerFastScroller) layout.findViewById(R.id.recyclerFastScroller_locations);
        recyclerFastScroller.setRecyclerView(recyclerView);

        progressWidget.findViewById(R.id.button_error_retry).setOnClickListener(new View.OnClickListener() {
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
