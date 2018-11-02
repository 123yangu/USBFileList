package file.mec.com.mecfilelist;

import android.text.TextUtils;

import java.io.Serializable;

public class FileDetail implements Serializable {

    public boolean selected;
    public int icon;
    public String fileName;
    public String filePath;

    @Override
    public boolean equals(Object o) {
        if (o instanceof FileDetail) {
            return TextUtils.equals(((FileDetail) o).filePath, filePath);
        }

        return false;
    }
}