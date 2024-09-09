package com.haha.processor;

import com.squareup.javapoet.ClassName;

public class MConstants {


    public static ClassName CLASSNAME_LOG = ClassName.get("android.util", "Log");

    public static ClassName CLASSNAME_UI_THREAD = ClassName.get("android.support.annotation", "UiThread");

    public static ClassName CLASSNAME_VIEW = ClassName.get("android.view", "View");

    public static String INJECT_NAME = "inject";

    public static String _VIEW_BINDING = "_ViewBinding";

}
