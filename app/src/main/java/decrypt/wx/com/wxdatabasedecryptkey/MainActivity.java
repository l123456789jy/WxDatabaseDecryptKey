package decrypt.wx.com.wxdatabasedecryptkey;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.io.File;

public class MainActivity extends AppCompatActivity {
  public static final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";
  private static final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";

  private static final String WX_DB_FILE_NAME = "EnMicroMsg.db";


  private String mCurrApkPath =  Environment.getExternalStorageDirectory().getPath() + "/";
  private static final String COPY_WX_DATA_DB = "wx_data.db";
  //拷贝到sd卡目录上
  String copyFilePath = mCurrApkPath + COPY_WX_DATA_DB;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
   //获取root权限
    DecryptUtiles.execRootCmd("chmod 777 -R " + WX_ROOT_PATH);
    //获取root权限
    DecryptUtiles.execRootCmd("chmod 777 -R " + copyFilePath);
    String password = DecryptUtiles.initDbPassword(this);
    String uid = DecryptUtiles.initCurrWxUin();
    //获取微信目录下的数据库文件
//    File wxDataDir = new File(WX_DB_DIR_PATH);
//    FileUtiles.searchFile(wxDataDir, WX_DB_FILE_NAME);
    //早期版本使用这种方式，撞库，判断哪个是当前用户的db
     //FileUtiles.open(mCurrApkPath,COPY_WX_DATA_DB,this,password);

     // MD5("mm"+auth_info_key_prefs.xml中解析出微信的uin码)得到db父目录
    try {
      String path = WX_DB_DIR_PATH +"/"+ Md5Utils.md5Encode("mm" + uid) + "/" + WX_DB_FILE_NAME;
      Log.e("onCreate",path);
      File wxDataDir = new File(path);
      FileUtiles.openWxDb(wxDataDir,this,password);
    } catch (Exception e) {
      Log.e("onCreate",e.getMessage());
      e.printStackTrace();
    }


  }
}
