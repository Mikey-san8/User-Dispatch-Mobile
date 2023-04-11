package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class MapFragment extends Fragment
{

    public View main;

    public MapFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        main = inflater.inflate(R.layout.activity_maps, container, false);


        return main;
    }
}
