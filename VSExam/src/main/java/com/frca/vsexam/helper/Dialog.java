package com.frca.vsexam.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public abstract class Dialog {

    public static AlertDialog Ok(Context context, Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        callback.call(builder);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

    public static interface Callback {
        abstract void call(AlertDialog.Builder dialog);
    }
}
