package file.mec.com.mecfilelist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * description: 文件列表
 * user: ccy
 * date: 2018/7/17 18:39
 */
public class FileListBroadcastReceiver extends BroadcastReceiver {

    public static UsbResponseListener mUsbResponseListener;
    public static boolean isFinished;
    /**
     * 是否注册广播
     */
    private boolean isRegisterReceiver = false;


    public static boolean register(UsbResponseListener listener) {
        try {
            mUsbResponseListener = listener;
            return true;
        } catch (Exception e) {
        }

        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {

            String path = intent.getData().getPath();

            File file = new File(path);

            if (TextUtils.equals(file.getAbsolutePath(), Environment.getExternalStorageDirectory().getAbsolutePath())) {
                return;
            }
            // 插入usb
            Intent intent2 = new Intent(context, MainActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent2.putExtra("path", path);
            context.startActivity(intent2);
            if(mUsbResponseListener != null){
                mUsbResponseListener.onInsertUsb(path, true);
            }
            isFinished = false;
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)
                || intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
            // 拔出usb
            String path = intent.getData().getPath();
            if(mUsbResponseListener != null){
                mUsbResponseListener.onInsertUsb(path, false);
            }
            isFinished = true;
        }
    }
}