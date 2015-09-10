package com.mehmetakiftutuncu.muezzin.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerView;
import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerViewDecoration;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.WithContentStates;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.utilities.Web;

import java.util.List;

import ru.vang.progressswitcher.ProgressWidget;

public abstract class LocationsFragment<T> extends Fragment implements WithContentStates,
                                                                       Web.OnRequestFailure,
                                                                       Web.OnResponse,
                                                                       SwipeRefreshLayout.OnRefreshListener,
                                                                       OnItemClickedListener {
    protected ProgressWidget progressWidget;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected IndexedRecyclerView recyclerView;
    protected ContentStates state;
    protected List<T> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_locations, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        progressWidget = (ProgressWidget) layout.findViewById(R.id.progressWidget_locations);

        swipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout_locations);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getContext(), R.color.primary),
                ContextCompat.getColor(getContext(), R.color.primaryDark),
                ContextCompat.getColor(getContext(), R.color.accent)
        );

        recyclerView = (IndexedRecyclerView) layout.findViewById(R.id.recyclerView_locations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        IndexedRecyclerViewDecoration decoration = new IndexedRecyclerViewDecoration();
        recyclerView.addItemDecoration(decoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadItems(false);
    }

    @Override
    public void changeStateTo(ContentStates newState) {
        if (state == null || !state.equals(newState)) {
            state = newState;

            switch (newState) {
                case LOADING:
                    progressWidget.showProgress(true);
                    swipeRefreshLayout.setRefreshing(true);
                    break;
                case ERROR:
                    progressWidget.showError(true);
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case CONTENT:
                    progressWidget.showContent(true);
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case NO_CONTENT:
                    progressWidget.showEmpty(true);
                    swipeRefreshLayout.setRefreshing(false);
                    break;
            }
        }
    }

    public abstract void setItems(List<T> items, boolean saveData);

    public abstract void loadItems(boolean forceDownload);

    public abstract void downloadItems();

    @Override
    public void onRefresh() {
        loadItems(true);
    }
}
