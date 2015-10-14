package com.mehmetakiftutuncu.muezzin.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.WithToolbar;

public class WelcomeActivity extends AppCompatActivity  implements WithToolbar, View.OnClickListener {
    private Toolbar toolbar;

    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initializeToolbar();

        Button beginButton = (Button) findViewById(R.id.button_welcome_begin);

        beginButton.setOnClickListener(this);
    }

    @Override
    public void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // We are running on API level 23 or higher, ask for permission
            int hasWriteExternalStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.common_permissionInfo)
                            .setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            })
                            .setNegativeButton(R.string.common_cancel, null)
                            .create()
                            .show();

                    return;
                }

                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);

                return;
            }
        }

        runLocationSelectionActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    runLocationSelectionActivity();
                } else {
                    // Permission denied
                    final Snackbar snackbar = Snackbar.make(toolbar, getString(R.string.common_permissionDenined), Snackbar.LENGTH_INDEFINITE);

                    snackbar.setAction(getString(R.string.common_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    }).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void runLocationSelectionActivity() {
        finish();
        startActivity(new Intent(this, LocationSelectionActivity.class));
    }
}
