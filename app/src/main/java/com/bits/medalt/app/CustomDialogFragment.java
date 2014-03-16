package com.bits.medalt.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by ayush on 16/3/14.
 * @author Ayush Kumar
 */
public class CustomDialogFragment extends DialogFragment {

    private static final String TITLE_KEY = "title";
    private static final String MESSAGE_KEY = "message";

    public CustomDialogFragment() {
        super();
    }

    public static CustomDialogFragment newInstance(String title, String message){
        CustomDialogFragment customDialogFragment = new CustomDialogFragment();
        //Add arguments to the fragment
        Bundle args = new Bundle();
        args.putString(TITLE_KEY,title);
        args.putString(MESSAGE_KEY,message);
        customDialogFragment.setArguments(args);
        return customDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString(TITLE_KEY,"Title");
        String message = getArguments().getString(MESSAGE_KEY);

        //TODO : Find a way to make Phone number in the DialogFragment clickable
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });
        return alertDialogBuilder.create();
    }
}
