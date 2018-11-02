package file.mec.com.mecfilelist;

public interface UsbResponseListener {
    /**
     * usb 插入拔出
     * @param usbPath
     * @param isPullOut true 插入
     */
    void onInsertUsb(String usbPath,boolean isPullOut);
}
