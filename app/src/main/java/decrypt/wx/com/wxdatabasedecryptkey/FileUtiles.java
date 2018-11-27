package decrypt.wx.com.wxdatabasedecryptkey;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.threekilogram.objectbus.bus.ObjectBus;
import com.threekilogram.objectbus.runnable.Executable;
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
  private static final ObjectBus task = com.threekilogram.objectbus.bus.ObjectBus.newList();
  private static SQLiteDatabaseHook hook;
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

  /**
   * 连接数据库
   */
  public static void openWxDb(File dbFile, final Context mContext, String mDbPassword) {
    SQLiteDatabase.loadLibs(mContext);
     hook = new SQLiteDatabaseHook() {
      @Override
      public void preKey(SQLiteDatabase database) {
      }

      @Override
      public void postKey(SQLiteDatabase database) {
        database.rawExecSQL("PRAGMA cipher_migrate;");
      }
    };
    //打开数据库连接
    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, mDbPassword, null, hook);
    runRecontact(mContext, db);

  }

  private static void runMessage(final Context mContext, final SQLiteDatabase db) {
    task.toPool(new Runnable() {
      @Override public void run() {
        getMessageDate(db);
      }
    }).toMain(new Runnable() {
      @Override public void run() {
        Toast.makeText(mContext, "聊天信息查询完毕", Toast.LENGTH_LONG).show();
        runChatRoom(mContext,db);
      }
    }).run();
  }

  /**
   * 获取群聊成员列表
   * @param mContext
   * @param db
   */
  private static void runChatRoom(Context mContext, SQLiteDatabase db) {
       getChatRoomDate(mContext,db);
  }

  /**
   * 获取群聊成员列表
   * @param mContext
   * @param db
   */
  private static void getChatRoomDate(Context mContext, SQLiteDatabase db) {
    Cursor c1 = null;
    try {
      c1 = db.rawQuery("select * from chatroom ", null);
      Log.e("openWxDb", "群组信息记录分割线=====================================================================================");
      while (c1.moveToNext()) {
        String roomowner = c1.getString(c1.getColumnIndex("roomowner"));
        String chatroomname = c1.getString(c1.getColumnIndex("chatroomname"));
        String memberlist = c1.getString(c1.getColumnIndex("memberlist"));
        Log.e("openWxDb", "群主====" + roomowner + "    群组成员id=====" + memberlist+ "    群id=====" + chatroomname);
      }
      c1.close();
      db.close();
    } catch (Exception e) {
      c1.close();
      db.close();
      Log.e("openWxDb", "读取数据库信息失败" + e.toString());
    }
  }

  /**
   * 查询聊天信息
   *  这里查出的聊天信息包含用户主动删除的信息
   *  无心的聊天信息删除不是物理删除，所哟只要不卸载仍然可以查到聊天记录
   * @param db
   */
  private static void getMessageDate(SQLiteDatabase db) {
    Cursor c1 = null;
    try {
      //这里只查询文本消息，type=1  图片消息是47，具体信息可以自己测试  http://emoji.qpic.cn/wx_emoji/gV159fHh6rYfCMejCAU1wIoP6eywxFMYjaJiaBzPbSjoc6XlTLoMyKQEh4nswfrX5/ （发送表情连接可以拼接的）
      c1 = db.rawQuery("select * from message where type = 1 ", null);
      Log.e("openWxDb", "聊天记录分割线=====================================================================================");
      while (c1.moveToNext()) {
        String talker = c1.getString(c1.getColumnIndex("talker"));
        String content = c1.getString(c1.getColumnIndex("content"));
        String createTime = c1.getString(c1.getColumnIndex("createTime"));
        Log.e("openWxDb", "聊天对象微信号====" + talker + "    内容=====" + content+ "    时间=====" + createTime);
      }
      c1.close();
    } catch (Exception e) {
      c1.close();
      Log.e("openWxDb", "读取数据库信息失败" + e.toString());
    }
  }

  /**
   * 微信好友信息
   * @param mContext
   * @param db
   */
  private static void runRecontact(final Context mContext, final SQLiteDatabase db) {
    task.toPool(new Runnable() {
      @Override public void run() {
        getRecontactDate(db);
      }
    }).toMain(new Runnable() {
      @Override public void run() {
        Toast.makeText(mContext, "查询通讯录完毕", Toast.LENGTH_LONG).show();
        runMessage(mContext, db);
      }
    }).run();
  }

  /**
   * 获取当前用户的微信所有联系人
   */
  private static void getRecontactDate(SQLiteDatabase db) {
    Cursor c1 = null;
    try {
      //查询所有联系人（verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
      c1 = db.rawQuery(
          "select * from rcontact where verifyFlag = 0 and type != 4 and type != 2 and nickname != ''",
          null);
      while (c1.moveToNext()) {
        String userName = c1.getString(c1.getColumnIndex("username"));
        String nickName = c1.getString(c1.getColumnIndex("nickname"));
        Log.e("openWxDb", "userName====" + userName + "    nickName=====" + nickName);
      }
      c1.close();
    } catch (Exception e) {
      c1.close();
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
      Log.e("copyFile", "复制单个文件操作出错");
      e.printStackTrace();
    }
  }
}
