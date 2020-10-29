package com.mehmetakiftutuncu.muezzin.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.PlaceSelectionActivity;

/**
 * Created by akif on 08/05/16.
 */
public class NoPlacesFoundFragment extends Fragment implements View.OnClickListener {
    public NoPlacesFoundFragment() {}

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_noplacesfound, container, false);

        FloatingActionButton floatingActionButtonAddPlace = (FloatingActionButton) layout.findViewById(R.id.fab_addPlace);

        floatingActionButtonAddPlace.setOnClickListener(this);

        return layout;
    }

    @Override public void onClick(View v) {
        startActivity(new Intent(getActivity(), PlaceSelectionActivity.class));
    }
}
