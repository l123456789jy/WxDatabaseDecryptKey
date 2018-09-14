package decrypt.wx.com.wxdatabasedecryptkey;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

/**
 * 项目名称：WxDatabaseDecryptKey
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2018/9/14 9:49
 * 修改人：Administrator
 * 修改时间：2018/9/14 9:49
 * 修改备注：
 * 联系方式：906514731@qq.com
 */
public class FileUtiles {

  private static List<File> mWxDbPathList = new ArrayList<>();

  /**
   * 递归查询微信本地数据库文件
   *
   * @param file 目录
   * @param fileName 需要查找的文件名称
   */
  public static void searchFile(File file, String fileName) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File childFile : files) {
          searchFile(childFile, fileName);
        }
      }
    } else {
      if (fileName.equals(file.getName())) {
        mWxDbPathList.add(file);
      }
    }
  }


  public static void open(String mCurrApkPath, String COPY_WX_DATA_DB, Context mContext,
      String mDbPassword) {
    //处理多账号登陆情况
    for (int i = 0; i < mWxDbPathList.size(); i++) {
      File file = mWxDbPathList.get(i);
      String copyFilePath = mCurrApkPath + COPY_WX_DATA_DB;
      Log.e("copyFilePath","copyFilePath==="+copyFilePath);
      //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
      copyFile(file.getAbsolutePath(), copyFilePath);
      File copyWxDataDb = new File(copyFilePath);
      openWxDb(copyWxDataDb, mContext, mDbPassword);
    }

  }


  /**
   * 连接数据库
   */
  public static void openWxDb(File dbFile, Context mContext, String mDbPassword) {
    SQLiteDatabase.loadLibs(mContext);
    SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
      @Override
      public void preKey(SQLiteDatabase database) {
      }

      @Override
      public void postKey(SQLiteDatabase database) {
        database.rawExecSQL("PRAGMA cipher_migrate;");
      }
    };

    try {
      //打开数据库连接
      SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);
      //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
      Cursor c1 = db.rawQuery(
          "select * from rcontact where verifyFlag = 0 and type != 4 and type != 2 and nickname != '' limit 20, 9999",
          null);
      while (c1.moveToNext()) {
        String userName = c1.getString(c1.getColumnIndex("username"));
        String alias = c1.getString(c1.getColumnIndex("alias"));
        String nickName = c1.getString(c1.getColumnIndex("nickname"));
        Log.e("openWxDb", "userName====" + userName);
        Log.e("openWxDb", "alias====" + alias);
        Log.e("openWxDb", "nickName=====" + nickName);
      }
      c1.close();
      db.close();
    } catch (Exception e) {
      Log.e("openWxDb", "读取数据库信息失败" + e.toString());
    }
  }


  /**
   * 复制单个文件
   *
   * @param oldPath String 原文件路径 如：c:/fqf.txt
   * @param newPath String 复制后路径 如：f:/fqf.txt
   * @return boolean
   */
  public static void copyFile(String oldPath, String newPath) {
    try {
      int byteRead = 0;
      File oldFile = new File(oldPath);
      if (oldFile.exists()) { //文件存在时
        InputStream inStream = new FileInputStream(oldPath); //读入原文件
        FileOutputStream fs = new FileOutputStream(newPath);
        byte[] buffer = new byte[1444];
        while ((byteRead = inStream.read(buffer)) != -1) {
          fs.write(buffer, 0, byteRead);
        }
        inStream.close();
      }
    } catch (Exception e) {
      System.out.println("复制单个文件操作出错");
      e.printStackTrace();

    }
  }


}
