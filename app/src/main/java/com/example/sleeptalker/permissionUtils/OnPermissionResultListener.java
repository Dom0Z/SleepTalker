package com.example.sleeptalker.permissionUtils;

import androidx.annotation.NonNull;

public interface OnPermissionResultListener {

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
}
