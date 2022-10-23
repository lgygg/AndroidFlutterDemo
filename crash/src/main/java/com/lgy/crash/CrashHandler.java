package com.lgy.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.text.Format;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrashHandler implements ICrash {
    private StringBuilder crashHead;
    private String storageType = STORAGE_NOT;
    private String mCrashDirPath;
    private Format FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault());
    private ExecutorService sExecutor;
    private OnHandlerListener onHandlerListener;

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        String crashInfo = obtainExceptionInfo(throwable);
        switch (this.storageType) {
            case STORAGE_LOCAL:
                localStorage(crashInfo);
                break;
            case STORAGE_NET:
                netStorage(crashInfo);
                break;
            case STORAGE_LOCAL_AND_NET:
                localStorage(crashInfo);
                netStorage(crashInfo);
                break;
            default:
                break;
        }
        if (onHandlerListener != null) {
            onHandlerListener.onCrash(throwable.getMessage());
        }
        killCurrentProcess();
    }


    @Override
    public void init(Context context,LinkedHashMap<String,String> msg) {
        crashHead = new StringBuilder();
        mCrashDirPath = context.getFilesDir() + File.separator +"crash/";
        String versionName = "";
        int versionCode = 0;
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        crashHead.append("\n************* Crash Log Head ****************")
                .append("\nDevice Manufacturer: ")// 设备厂商
                .append(Build.MANUFACTURER)
                .append("\nDevice Model       : ")// 设备型号
                .append(Build.MODEL)
                .append("\nAndroid Version    : ")// 系统版本
                .append(Build.VERSION.RELEASE)
                .append("\nAndroid SDK        : ")// SDK版本
                .append(Build.VERSION.SDK_INT)
                .append("\nApp VersionName    : ")
                .append(versionName)
                .append("\nApp VersionCode    : ")
                .append(versionCode);
        if (msg != null) {
            for (String key : msg.keySet()) {
                crashHead.append("\n")
                        .append(key)
                        .append(": ")
                        .append(msg.get(key));
            }
        }
        crashHead.append("\n************* Crash Log Head ****************\n\n");
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void setStorageType(String type) {
        this.storageType = type;
    }

    @Override
    public void setStoragePath(String path) {
        this.mCrashDirPath = path;
    }

    @Override
    public void setOnHandlerListener(OnHandlerListener listener) {
        this.onHandlerListener = listener;
    }

    /**
     * 获取系统未捕捉的错误信息
     *
     * @param throwable
     * @return
     */
    private String obtainExceptionInfo(Throwable throwable) {
        StringWriter mStringWriter = new StringWriter();
        PrintWriter mPrintWriter = new PrintWriter(mStringWriter);
        mPrintWriter.write(crashHead.toString());
        throwable.printStackTrace(mPrintWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(mPrintWriter);
            cause = cause.getCause();
        }
        mPrintWriter.close();
        return mStringWriter.toString();
    }

    private void netStorage (String msg) {

    }

    private void localStorage(String msg) {
        if (TextUtils.isEmpty(mCrashDirPath)) {
            return;
        }
        Date now = new Date(System.currentTimeMillis());
        String fileName = FORMAT.format(now) + ".txt";
        final String fullPath = mCrashDirPath + fileName;
        if (!createOrExistsFile(fullPath)) {
            return;
        }
        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        sExecutor.execute(() -> {
            PrintWriter pw = null;
            FileWriter fw = null;
            try {
                fw = new FileWriter(fullPath, false);
                pw = new PrintWriter(fw);
                pw.write(msg);
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (pw != null) {
                    pw.close();
                }
            }
        });
    }

    private boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else {
            return false;
        }
    }

    private boolean createOrExistsFile(final String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.isFile();
        }
        if (!createOrExistsDir(file.getParentFile())) {
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    private void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
