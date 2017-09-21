package com.zx.ocrdemo.utils;

import android.content.Context;

import java.io.File;

/**
 * 作者： 周旭 on 2017年9月20日 0020.
 * 邮箱：374952705@qq.com
 * 博客：http://www.jianshu.com/u/56db5d78044d
 */

public class FileUtils {

    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        return file;
    }
}
