package com.example.tabview;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {



private TextView pythonTest;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

       View v= inflater.inflate(R.layout.fragment_chat, container, false);
pythonTest=v.findViewById(R.id.testPython);

if(!Python.isStarted()) Python.start(new AndroidPlatform(getContext()));


Python py= Python.getInstance();
PyObject object= py.getModule("script");
PyObject obj=null;

//obj=object.callAttr("main", 1, 2);
//pythonTest.setText(obj.toString());




        return v;
    }
}