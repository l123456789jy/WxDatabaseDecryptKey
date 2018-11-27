package decrypt.wx.com.wxdatabasedecryptkey;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.threekilogram.objectbus.bus.ObjectBus;
import java.io.File;

/**
 *微信语音消息，图片，视频，收藏的语音，图片视频都是存放在本地，对应而本地的路径规则是
 *  Environment.getExternalStorageDirectory().getPath() + "/"+tencent/MicroMsg/+ Md5Utils.md5Encode("mm" + uid)
 *  这个是当前用户的所有资料，都可以打包，详细情况请看下面博客地址
 */
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
    ObjectBus.newList().toPool(new Runnable() {
      @Override public void run() {
        //获取root权限
        DecryptUtiles.execRootCmd("chmod 777 -R " + WX_ROOT_PATH);
        //获取root权限
        DecryptUtiles.execRootCmd("chmod 777 -R " + copyFilePath);
        String password = DecryptUtiles.initDbPassword(MainActivity.this);
        String uid = DecryptUtiles.initCurrWxUin();
        try {
          String path = WX_DB_DIR_PATH +"/"+ Md5Utils.md5Encode("mm" + uid) + "/" + WX_DB_FILE_NAME;
          Log.e("path",copyFilePath);
          Log.e("path",path);
          Log.e("path",password);
          //微信原始数据库的地址
          File wxDataDir = new File(path);
          //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
          FileUtiles.copyFile(wxDataDir.getAbsolutePath(), copyFilePath);
          //将微信数据库导出到sd卡操作sd卡上数据库
          FileUtiles.openWxDb(new File(copyFilePath),MainActivity.this,password);
        } catch (Exception e) {
          Log.e("path",e.getMessage());
          e.printStackTrace();
        }
      }
    }).run();

  }
}
