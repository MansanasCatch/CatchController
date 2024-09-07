package com.example.catchcontroller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class FragmentDialogBox extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String TAG = "myApp";
        ArrayList<String> waypoints = new ArrayList<>();
        Bundle bundle = getArguments();
        waypoints= bundle.getStringArrayList("waypoints");
        String waypointsArray[] = waypoints.toArray(new String[waypoints.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Waypoint List");
        builder.setNeutralButton("Clear Waypoints", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MyApplication) getActivity().getApplication()).clearWaypoints();
            }
        });
        builder.setItems(waypointsArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lw = ((AlertDialog) dialog).getListView();
                Object checkedItem = lw.getAdapter().getItem(which);
                Log.e(TAG,"Selected:" + checkedItem.toString());
                ((MyApplication) getActivity().getApplication()).removeWaypoint(checkedItem.toString());
            }
        });

        return builder.create();
    }
}
