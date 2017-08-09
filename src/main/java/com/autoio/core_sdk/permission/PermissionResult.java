package com.autoio.core_sdk.permission;

public interface PermissionResult {
    void onGranted();

    void onDenied();
}
