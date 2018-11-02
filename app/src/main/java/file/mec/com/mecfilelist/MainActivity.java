package file.mec.com.mecfilelist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UsbResponseListener, FileListAdapter.FileListListener {


    RecyclerView rv_list;
    ImageView id_back;
    FileListAdapter mFileListAdapter;
    List<FileDetail> mFileList;
    File currentFilePath;
    String rootPath;
    LinearLayout pathLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv_list = (RecyclerView) findViewById(R.id.rv_list);
        id_back = (ImageView) findViewById(R.id.id_back);
        pathLinearLayout = (LinearLayout) findViewById(R.id.id_path_layout);

        id_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileList(false);
            }
        });
        mFileList = new ArrayList<>();
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        mFileListAdapter = new FileListAdapter(this, mFileList);
        mFileListAdapter.setmFileListListener(this);
        rv_list.setAdapter(mFileListAdapter);

        FileListBroadcastReceiver.register(this);
        String usrPath = getIntent().getStringExtra("path");
//        String usrPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (!TextUtils.isEmpty(usrPath)) {
            this.rootPath = usrPath;
            new FileListTask(usrPath).execute();
        }
    }


    @Override
    public void onInsertUsb(String usbPath, boolean isPullOut) {
        if (!isPullOut) {
            finish();
        }
    }

    public void setPath(List<String> allPath) {
        if (allPath == null) {
            return;
        }

        pathLinearLayout.removeAllViews();
        for (int i = allPath.size() - 1; i >= 0; i--) {
            String p = allPath.get(i);
            View view = getLayoutInflater().inflate(R.layout.layout_file_path_item, null);
            File file = new File(p);
            ((TextView) view.findViewById(R.id.id_path)).setText(file.getName() + (i == 0 ? "" : "  >  "));
            pathLinearLayout.addView(view);
            view.findViewById(R.id.id_path).setTag(R.id.id_tag, p);
            view.findViewById(R.id.id_path).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = (String) v.getTag(R.id.id_tag);
                    currentFilePath = new File(path);
                    openFileList(true);
                }
            });
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            openFileList(false);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void openFileList(boolean isClickPath) {
        if(currentFilePath != null){
            clickOnFile(currentFilePath.getPath(),isClickPath);
        }else{
            finish();
        }
    }

    private void setPath(String rootPath, String path) {
        File root = new File(rootPath);
        File cur = new File(path);

        List<String> allPath = new ArrayList<String>();
        while (!TextUtils.equals(cur.getAbsolutePath(), root.getAbsolutePath())) {
            allPath.add(cur.getAbsolutePath());
            cur = cur.getParentFile();
        }
        allPath.add(rootPath);
        setPath(allPath);
    }

    public void clickOnFile(String path, boolean isClickPath) {
        if (!rootExists(isClickPath)) {
            return;
        }
        setPath(rootPath, path);
        File file = new File(path);
        if(isClickPath){
            if (file.isDirectory()) {
                new FileListTask(file.getPath()).execute();
            }
        }else{
            if (file.getParentFile().isDirectory()) {
                new FileListTask(file.getParentFile().getPath()).execute();
            }
        }
    }

    private boolean rootExists(boolean isClickPath) {
        File root = new File(rootPath);

        if (!root.exists() || root.list() == null || root.list().length <= 0) {
            if(!isClickPath){
                finish();
            }
            return false;
        }
        if (TextUtils.equals(currentFilePath.getAbsolutePath(), root.getAbsolutePath())) {
            if(!isClickPath){
                finish();
                return false;
            }else{
                return true;
            }
        }
        return true;
    }

    @Override
    public void onClickItem(FileDetail file, int position) {
        if (new File(file.filePath).isDirectory()) {
            new FileListTask(file.filePath).execute();
            return;
        }

        // 拷贝文件到本地
        File localFile = new File(getDir(), file.fileName);
        if (localFile.exists()) {
            try {
                localFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileUtls.copyFile(file.filePath, localFile.getPath());
        FileUtls.openFile(this, localFile.getPath());
    }

    class FileListTask extends AsyncTask<Integer, Integer, List<FileDetail>> {
        String usbPath;

        public FileListTask(String usbPath) {
            this.usbPath = usbPath;
        }

        @Override
        protected List<FileDetail> doInBackground(Integer... integers) {
            File file = new File(usbPath);
            currentFilePath = file;
            final List<String> docTypes = Arrays.asList(getResources().getStringArray(R.array.docType));
            final List<String> pptTypes = Arrays.asList(getResources().getStringArray(R.array.pptType));
            if (!file.isDirectory()) {
                return null;
            }
            List<FileDetail> details = new ArrayList<>();
            File[] files = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (new File(dir.getPath(), filename).isDirectory() && !new File(dir.getPath(), filename).isHidden()) {
                        return true;
                    }

                    if (docTypes.contains(FileUtls.getFileSuffix(filename)) || pptTypes.contains(FileUtls.getFileSuffix(filename))) {
                        return true;
                    }
                    return false;
                }
            });
            if (files == null) {
                return null;
            }


            for (File f : files) {
                if (!f.isHidden()) {
                    // 隐藏文件夹或者文件不显示
                    FileDetail fd = new FileDetail();
                    fd.fileName = f.getName();
                    fd.selected = false;
                    fd.filePath = f.getAbsolutePath();
                    if (f.isDirectory()) {
                        fd.icon = R.mipmap.ic_file_floder_icon;
                        f.listFiles(new FilenameFilter() {

                            @Override
                            public boolean accept(File dir, String filename) {
                                if (new File(dir.getPath(), filename).isDirectory() && !new File(dir.getPath(), filename).isHidden()) {
                                    return true;
                                }

                                if (docTypes.contains(FileUtls.getFileSuffix(filename))
                                        || pptTypes.contains(FileUtls.getFileSuffix(filename))) {
                                    return true;
                                }
                                return false;
                            }
                        });
                    } else {
                        if (docTypes.contains(FileUtls.getFileSuffix(f.getAbsolutePath()))) {
                            fd.icon = R.mipmap.ic_word;
                        } else if (pptTypes.contains(FileUtls.getFileSuffix(f.getAbsolutePath()))) {
                            fd.icon = R.mipmap.ic_ppt;
                        }
                    }
                    details.add(fd);
                }

            }

            Collections.sort(details, new Comparator<FileDetail>() {

                @Override
                public int compare(FileDetail lhs, FileDetail rhs) {
                    File lf = new File(lhs.filePath);
                    File rf = new File(rhs.filePath);
                    if (lf.isDirectory()) {
                        return -1;
                    } else if (!rf.isDirectory()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
            return details;
        }

        @Override
        protected void onPostExecute(List<FileDetail> fileDetails) {
            super.onPostExecute(fileDetails);
            setPath(rootPath, usbPath);
            if (fileDetails == null) {
                return;
            }
            mFileList = fileDetails;
            mFileListAdapter.setData(mFileList);

        }
    }

    public String getDir() {
        String createFileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName();
        File file = new File(createFileDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return createFileDir;
    }
}
