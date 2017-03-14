package jadx.fxgui;

import jadx.fxgui.jobs.BackgroundWorker;
import jadx.fxgui.jobs.DecompileJob;
import jadx.fxgui.jobs.IndexJob;
import jadx.fxgui.settings.JadxSettings;
import jadx.fxgui.settings.JadxSettingsAdapter;
import jadx.fxgui.treemodel.*;
import jadx.fxgui.ui.CodeView;
import jadx.fxgui.ui.DrawableView;
import jadx.fxgui.utils.AsyncTask;
import jadx.fxgui.utils.CacheObject;
import jadx.fxgui.utils.LogCollector;
import jadx.fxgui.utils.NLS;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class JadxFxGUI extends Application {

    public static final String JAVA_CODE = "package com.stirante.instaprefs;\n" +
            "\n" +
            "import android.app.Application;\n" +
            "import android.app.Dialog;\n" +
            "import android.content.ComponentName;\n" +
            "import android.content.Context;\n" +
            "import android.content.DialogInterface;\n" +
            "import android.content.Intent;\n" +
            "import android.content.res.Resources;\n" +
            "import android.text.SpannableStringBuilder;\n" +
            "import android.widget.Toast;\n" +
            "\n" +
            "import com.stirante.instaprefs.utils.FileUtils;\n" +
            "\n" +
            "import java.io.File;\n" +
            "import java.lang.reflect.Field;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Collections;\n" +
            "import java.util.List;\n" +
            "\n" +
            "import de.robv.android.xposed.IXposedHookLoadPackage;\n" +
            "import de.robv.android.xposed.IXposedHookZygoteInit;\n" +
            "import de.robv.android.xposed.XC_MethodHook;\n" +
            "import de.robv.android.xposed.XC_MethodReplacement;\n" +
            "import de.robv.android.xposed.XSharedPreferences;\n" +
            "import de.robv.android.xposed.XposedBridge;\n" +
            "import de.robv.android.xposed.XposedHelpers;\n" +
            "import de.robv.android.xposed.callbacks.XC_LoadPackage;\n" +
            "\n" +
            "import static de.robv.android.xposed.XposedHelpers.*;\n" +
            "\n" +
            "/**\n" +
            " * Created by stirante\n" +
            " */\n" +
            "public class InstaprefsModule implements IXposedHookLoadPackage, IXposedHookZygoteInit {\n" +
            "\n" +
            "    private XSharedPreferences prefs;\n" +
            "    private boolean debug;\n" +
            "    private String[] ignored_tags;\n" +
            "\n" +
            "\n" +
            "    @Override\n" +
            "    public void initZygote(StartupParam startupParam) throws Throwable {\n" +
            "        prefs = new XSharedPreferences(\"com.stirante.instaprefs\");\n" +
            "        prefs.makeWorldReadable();\n" +
            "    }\n" +
            "\n" +
            "    @Override\n" +
            "    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam packageParam) throws Throwable {\n" +
            "        if (packageParam.packageName.equalsIgnoreCase(\"com.instagram.android\")) {\n" +
            "            findAndHookMethod(Application.class, \"attach\", Context.class, new XC_MethodHook() {\n" +
            "                @Override\n" +
            "                protected void afterHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                    prefs.makeWorldReadable();\n" +
            "                    prefs.reload();\n" +
            "                    debug = prefs.getBoolean(\"enable_spam\", false);\n" +
            "                    debug(\"Hooked into Intagram\");\n" +
            "                    ignored_tags = prefs.getString(\"ignored_tags\", \"\").split(\";\");\n" +
            "                    if (ignored_tags.length == 1 && ignored_tags[0].isEmpty()) ignored_tags = new String[]{};\n" +
            "                    final Context context = (Context) param.args[0];\n" +
            "                    //disable double tap to like\n" +
            "                    if (prefs.getBoolean(\"disable_double_tap_like\", false)) {\n" +
            "                        findAndHookMethod(\"com.instagram.android.feed.d.a.b\", packageParam.classLoader, \"k\", findClass(\"com.instagram.feed.a.x\", packageParam.classLoader), findClass(\"com.instagram.feed.ui.h\", packageParam.classLoader), int.class, new XC_MethodReplacement() {\n" +
            "                            @Override\n" +
            "                            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {\n" +
            "                                return null;\n" +
            "                            }\n" +
            "                        });\n" +
            "                    }\n" +
            "                    //Add download and zoom\n" +
            "                    findAndHookMethod(\"com.instagram.android.feed.adapter.a.an\", packageParam.classLoader, \"b\", new XC_MethodHook() {\n" +
            "                        @Override\n" +
            "                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                            CharSequence[] result = (CharSequence[]) param.getResult();\n" +
            "                            ArrayList<CharSequence> newResult = new ArrayList<>();\n" +
            "                            Collections.addAll(newResult, result);\n" +
            "                            newResult.add(\"Download\");\n" +
            "                            Object media = getObjectField(param.thisObject, \"e\");\n" +
            "                            int type = getIntField(getObjectField(media, \"f\"), \"f\");\n" +
            "                            if (type == 1)\n" +
            "                                newResult.add(\"Zoom\");\n" +
            "                            CharSequence[] arr = new CharSequence[newResult.size()];\n" +
            "                            newResult.toArray(arr);\n" +
            "                            param.setResult(arr);\n" +
            "                        }\n" +
            "                    });\n" +
            "                    //handle download and zoom\n" +
            "                    findAndHookMethod(\"com.instagram.android.feed.adapter.a.aj\", packageParam.classLoader, \"onClick\", findClass(\"android.content.DialogInterface\", packageParam.classLoader), int.class, new XC_MethodHook() {\n" +
            "                        @Override\n" +
            "                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                            String clicked = ((Object[]) callMethod(getObjectField(param.thisObject, \"a\"), \"b\"))[(int) param.args[1]].toString();\n" +
            "                            if (clicked.equalsIgnoreCase(\"Download\") || clicked.equalsIgnoreCase(\"Zoom\")) {\n" +
            "                                ((DialogInterface) param.args[0]).dismiss();\n" +
            "                                Object media = getObjectField(getObjectField(param.thisObject, \"a\"), \"e\");\n" +
            "                                if (clicked.equalsIgnoreCase(\"Download\")) {\n" +
            "                                    Object userObject = getObjectField(media, \"e\");\n" +
            "                                    String user = (String) getObjectField(userObject, \"a\");\n" +
            "                                    String video = (String) getObjectField(media, \"ac\");\n" +
            "                                    String image = (String) callMethod(media, \"a\", context);\n" +
            "                                    String id = (String) callMethod(media, \"n\");\n" +
            "                                    int type = getIntField(getObjectField(media, \"f\"), \"f\");\n" +
            "                                    if (type == 1) {\n" +
            "                                        FileUtils.download(image, new File(FileUtils.INSTAPREFS_DIR, user + \"/\" + id + \".jpg\"), context);\n" +
            "                                    } else if (type == 2) {\n" +
            "                                        FileUtils.download(video, new File(FileUtils.INSTAPREFS_DIR, user + \"/\" + id + \".mp4\"), context);\n" +
            "                                    } else {\n" +
            "                                        debug(\"Unsupported media type \" + getObjectField(media, \"f\").toString());\n" +
            "                                    }\n" +
            "                                } else if (clicked.equalsIgnoreCase(\"Zoom\")) {\n" +
            "                                    String image = (String) callMethod(media, \"a\", context);\n" +
            "                                    Intent intent = new Intent();\n" +
            "                                    intent.setComponent(ComponentName.unflattenFromString(\"com.stirante.instaprefs/.ZoomActivity\"));\n" +
            "                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);\n" +
            "                                    intent.putExtra(\"url\", image);\n" +
            "                                    context.startActivity(intent);\n" +
            "                                }\n" +
            "                                param.setResult(null);\n" +
            "                            }\n" +
            "                        }\n" +
            "                    });\n" +
            "                    //add download and zoom to direct\n" +
            "                    findAndHookMethod(\"com.instagram.android.directsharev2.b.dn\", packageParam.classLoader, \"b\", findClass(\"com.instagram.direct.model.n\", packageParam.classLoader), new XC_MethodReplacement() {\n" +
            "                        @Override\n" +
            "                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                            Object directMessage_l = param.args[0];\n" +
            "                            ArrayList<CharSequence> arrayList = new ArrayList<>();\n" +
            "                            if ((boolean) callMethod(directMessage_l, \"t\")) {\n" +
            "                                arrayList.add(context.getResources().getString(context.getResources().getIdentifier(\"direct_unsend_message\", \"string\", context.getPackageName())));\n" +
            "                            }\n" +
            "                            if ((boolean) callMethod(directMessage_l, \"u\")) {\n" +
            "                                arrayList.add(context.getResources().getString(context.getResources().getIdentifier(\"direct_report_message\", \"string\", context.getPackageName())));\n" +
            "                            }\n" +
            "                            CharSequence a = (CharSequence) callStaticMethod(findClass(\"com.instagram.direct.model.q\", packageParam.classLoader), \"a\", new Class[]{findClass(\"com.instagram.direct.model.n\", packageParam.classLoader), findClass(\"android.content.res.Resources\", packageParam.classLoader)}, directMessage_l, callMethod(param.thisObject, \"getResources\"));\n" +
            "                            String type = (String) callMethod(callMethod(directMessage_l, \"b\"), \"name\");\n" +
            "                            if (!(type.equalsIgnoreCase(\"MEDIA\") || type.equalsIgnoreCase(\"MEDIA_SHARE\") || TextUtils.isEmpty(a))) {\n" +
            "                                arrayList.add(context.getResources().getString(context.getResources().getIdentifier(\"direct_copy_message_text\", \"string\", context.getPackageName())));\n" +
            "                            }\n" +
            "                            if (type.equalsIgnoreCase(\"MEDIA\") || type.equalsIgnoreCase(\"MEDIA_SHARE\")) {\n" +
            "                                arrayList.add(\"Download\");\n" +
            "                                arrayList.add(\"Zoom\");\n" +
            "                            }\n" +
            "                            boolean z = !arrayList.isEmpty();\n" +
            "                            if (z) {\n" +
            "                                Object onClick = newInstance(findClass(\"com.instagram.android.directsharev2.b.cw\", packageParam.classLoader), new Class[]{findClass(\"com.instagram.android.directsharev2.b.dn\", packageParam.classLoader), ArrayList.class, findClass(\"com.instagram.direct.model.l\", packageParam.classLoader), String.class}, param.thisObject, arrayList, directMessage_l, a);\n" +
            "                                Object builder = newInstance(findClass(\"com.instagram.ui.dialog.e\", packageParam.classLoader), new Class[]{Context.class}, callMethod(param.thisObject, \"getContext\"));\n" +
            "                                callMethod(builder, \"a\", arrayList.toArray(new CharSequence[arrayList.size()]), onClick);\n" +
            "                                callMethod(builder, \"a\", true);\n" +
            "                                callMethod(builder, \"b\", true);\n" +
            "                                Dialog dialog = (Dialog) callMethod(builder, \"c\");\n" +
            "                                dialog.show();\n" +
            "                            }\n" +
            "                            callMethod(param.thisObject, \"m\");\n" +
            "                            return z;\n" +
            "                        }\n" +
            "                    });\n" +
            "                    //handle download and zoom in direct\n" +
            "                    findAndHookMethod(\"com.instagram.android.directsharev2.b.cw\", packageParam.classLoader, \"onClick\", findClass(\"android.content.DialogInterface\", packageParam.classLoader), int.class, new XC_MethodHook() {\n" +
            "                        @Override\n" +
            "                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                            String clicked = (String) ((ArrayList) getObjectField(param.thisObject, \"a\")).get((Integer) param.args[1]);\n" +
            "                            if (clicked.equalsIgnoreCase(\"Download\")) {\n" +
            "                                Object media = getObjectField(getObjectField(param.thisObject, \"b\"), \"B\");\n" +
            "                                Object userObject = getObjectField(getObjectField(param.thisObject, \"b\"), \"d\");\n" +
            "                                String user = (String) getObjectField(userObject, \"a\");\n" +
            "                                String video = (String) getObjectField(media, \"ac\");\n" +
            "                                String image = (String) callMethod(media, \"a\", context);\n" +
            "                                String id = callMethod(getObjectField(param.thisObject, \"b\"), \"i\").toString();\n" +
            "                                int type = getIntField(getObjectField(media, \"f\"), \"f\");\n" +
            "                                if (type == 1) {\n" +
            "                                    FileUtils.download(image, new File(FileUtils.INSTAPREFS_DIR, user + \"/\" + id + \".jpg\"), context);\n" +
            "                                } else if (type == 2) {\n" +
            "                                    FileUtils.download(video, new File(FileUtils.INSTAPREFS_DIR, user + \"/\" + id + \".mp4\"), context);\n" +
            "                                } else {\n" +
            "                                    debug(\"Unsupported media type \" + getObjectField(media, \"f\").toString());\n" +
            "                                }\n" +
            "                            } else if (clicked.equalsIgnoreCase(\"Zoom\")) {\n" +
            "                                Object media = getObjectField(getObjectField(param.thisObject, \"b\"), \"B\");\n" +
            "                                String image = (String) callMethod(media, \"a\", context);\n" +
            "                                int type = getIntField(getObjectField(media, \"f\"), \"f\");\n" +
            "                                if (type == 1) {\n" +
            "                                    Intent intent = new Intent();\n" +
            "                                    intent.setComponent(ComponentName.unflattenFromString(\"com.stirante.instaprefs/.ZoomActivity\"));\n" +
            "                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);\n" +
            "                                    intent.putExtra(\"url\", image);\n" +
            "                                    context.startActivity(intent);\n" +
            "                                } else if (type == 2) {\n" +
            "                                    Toast.makeText(context, \"Can't zoom video!\", Toast.LENGTH_SHORT).show();\n" +
            "                                } else {\n" +
            "                                    debug(\"Unsupported media type \" + getObjectField(media, \"f\").toString());\n" +
            "                                }\n" +
            "                            }\n" +
            "                        }\n" +
            "                    });\n" +
            "                    //hiding some things\n" +
            "                    final boolean disable_ads = prefs.getBoolean(\"disable_ads\", false);\n" +
            "                    final boolean disable_tags = prefs.getBoolean(\"disable_tags\", false);\n" +
            "                    if (disable_ads || disable_tags) {\n" +
            "                        findAndHookMethod(\"com.instagram.feed.a.z\", packageParam.classLoader, \"a\", findClass(\"com.instagram.feed.a.x\", packageParam.classLoader), new XC_MethodHook() {\n" +
            "                            @Override\n" +
            "                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                                Object media = param.args[0];\n" +
            "                                //hiding ads\n" +
            "                                if (disable_ads) {\n" +
            "                                    if (getObjectField(media, \"z\") != null) {\n" +
            "                                        param.setResult(null);\n" +
            "                                        return;\n" +
            "                                    }\n" +
            "                                }\n" +
            "                                //hiding posts with specified hashtags\n" +
            "                                if (disable_tags) {\n" +
            "                                    List o = (List) XposedHelpers.callMethod(XposedHelpers.callMethod(media, \"J\"), \"c\");\n" +
            "                                    if (o != null && o.size() > 0) {\n" +
            "                                        Object comment = o.get(0);\n" +
            "                                        if (((Enum) XposedHelpers.callMethod(comment, \"i\")).name().equalsIgnoreCase(\"Caption\")) {\n" +
            "                                            String text = (String) XposedHelpers.callMethod(comment, \"f\");\n" +
            "                                            for (String tag : ignored_tags) {\n" +
            "                                                if (text.toLowerCase().contains(\"#\" + tag.toLowerCase())) {\n" +
            "                                                    param.setResult(null);\n" +
            "                                                    return;\n" +
            "                                                }\n" +
            "                                            }\n" +
            "                                        }\n" +
            "                                    }\n" +
            "                                }\n" +
            "                            }\n" +
            "                        });\n" +
            "                    }\n" +
            "                    //hide recommendation\n" +
            "                    if (prefs.getBoolean(\"disable_suggested_follow\", false)) {\n" +
            "                        XposedHelpers.findAndHookMethod(\"com.instagram.g.q\", packageParam.classLoader, \"a\", XposedHelpers.findClass(\"com.instagram.g.a.g\", packageParam.classLoader), new XC_MethodHook() {\n" +
            "                            @Override\n" +
            "                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                                XposedBridge.log(\"Hid the recommendation v3\");//still trying xD\n" +
            "                                param.setResult(null);\n" +
            "                            }\n" +
            "                        });\n" +
            "                        XposedHelpers.findAndHookMethod(\"com.instagram.g.q\", packageParam.classLoader, \"getCount\", new XC_MethodHook() {\n" +
            "                            @Override\n" +
            "                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                                XposedBridge.log(\"Hid the recommendation v2\");\n" +
            "                                param.setResult(0);\n" +
            "                            }\n" +
            "                        });\n" +
            "                    }\n" +
            "                    if (prefs.getBoolean(\"disable_comments\", false)) {\n" +
            "                        XposedHelpers.findAndHookMethod(\"com.instagram.feed.ui.text.ae\", packageParam.classLoader, \"a\", Resources.class, SpannableStringBuilder.class, boolean.class, boolean.class, XposedHelpers.findClass(\"com.instagram.feed.a.i\", packageParam.classLoader), XposedHelpers.findClass(\"com.instagram.feed.ui.text.b\", packageParam.classLoader), new XC_MethodHook() {\n" +
            "                            @Override\n" +
            "                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {\n" +
            "                                if (!((Enum) XposedHelpers.callMethod(param.args[4], \"i\")).name().equalsIgnoreCase(\"Caption\")) {// || !((boolean) param.args[2])\n" +
            "                                    param.setResult(((SpannableStringBuilder) param.args[1]).length());\n" +
            "                                }\n" +
            "                            }\n" +
            "                        });\n" +
            "                    }\n" +
            "                }\n" +
            "            });\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    private void traceObject(Object obj, String name) {\n" +
            "        Field[] fields = obj.getClass().getDeclaredFields();\n" +
            "        for (Field f : fields) {\n" +
            "            try {\n" +
            "                f.setAccessible(true);\n" +
            "                debug(name + \".\" + f.getName() + \" = \" + f.get(obj));\n" +
            "            } catch (IllegalAccessException e) {\n" +
            "                e.printStackTrace();\n" +
            "            }\n" +
            "        }\n" +
            "        debug(\"-------------\");\n" +
            "    }\n" +
            "\n" +
            "    private void debug(String string) {\n" +
            "        if (debug)\n" +
            "            XposedBridge.log(\"[Instaprefs] \" + string);\n" +
            "    }\n" +
            "}\n";
    public static final String MANIFEST_CODE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "    package=\"com.stirante.instaprefs\">\n" +
            "\n" +
            "    <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
            "\n" +
            "    <application\n" +
            "        android:allowBackup=\"true\"\n" +
            "        android:icon=\"@mipmap/instaprefs\"\n" +
            "        android:label=\"@string/app_name\"\n" +
            "        android:supportsRtl=\"true\"\n" +
            "        android:theme=\"@style/AppTheme\">\n" +
            "        <meta-data\n" +
            "            android:name=\"xposedmodule\"\n" +
            "            android:value=\"true\" />\n" +
            "        <meta-data\n" +
            "            android:name=\"xposeddescription\"\n" +
            "            android:value=\"Instaprefs\" />\n" +
            "        <meta-data\n" +
            "            android:name=\"xposedminversion\"\n" +
            "            android:value=\"30\" />\n" +
            "\n" +
            "        <activity android:name=\".ZoomActivity\">\n" +
            "            <intent-filter>\n" +
            "                <action android:name=\"android.intent.action.MAIN\" />\n" +
            "            </intent-filter>\n" +
            "        </activity>\n" +
            "        <activity\n" +
            "            android:name=\".SettingsActivity\"\n" +
            "            android:label=\"@string/app_name\">\n" +
            "            <intent-filter>\n" +
            "                <action android:name=\"android.intent.action.MAIN\" />\n" +
            "\n" +
            "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
            "            </intent-filter>\n" +
            "        </activity>\n" +
            "        <activity android:name=\".TagsActivity\"></activity>\n" +
            "    </application>\n" +
            "\n" +
            "</manifest>\n";

    private static final String DEFAULT_TITLE = "JADX-FxGUI";
    @FXML
    public TreeView<String> fileTree;
    @FXML
    public TabPane tabs;
    @FXML
    public ProgressBar progress;
    @FXML
    public Label statusText;
    @FXML
    public HBox pbox;
    private JadxWrapper wrapper;
    private JadxSettings settings;
    private CacheObject cacheObject;
    private Stage stage;
    private BackgroundWorker backgroundWorker;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        LogCollector.register();
        final JadxSettings jadxArgs = JadxSettingsAdapter.load();
        if (!jadxArgs.processArgs(getParameters().getRaw().toArray(new String[getParameters().getRaw().size()]))) {
            return;
        }
        this.wrapper = new JadxWrapper(jadxArgs);
        this.settings = jadxArgs;
        this.cacheObject = new CacheObject();
        resetCache();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(JadxFxGUI.class.getResource("/MainWindow.fxml"));
            fxmlLoader.setController(this);
            VBox node = fxmlLoader.load();
            Scene scene = new Scene(node, 1280, 720);
            scene.getStylesheets().add(JadxFxGUI.class.getResource("/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle(DEFAULT_TITLE);
            primaryStage.setOnCloseRequest(event -> cancelBackgroundJobs());
            pbox.prefWidthProperty().bind(fileTree.widthProperty());
            progress.prefWidthProperty().bind(fileTree.widthProperty());
            fileTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            fileTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                openTab((JNode) newValue);
                //TODO: add new tab
            });
            fileTree.setEditable(false);
            primaryStage.show();
            stage = primaryStage;
            if (settings.getInput().isEmpty()) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open file");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("APK file", "*.apk"),
                        new FileChooser.ExtensionFilter("DEX file", "*.dex"),
                        new FileChooser.ExtensionFilter("ZIP file", "*.zip"),
                        new FileChooser.ExtensionFilter("AAR file", "*.aar"),
                        new FileChooser.ExtensionFilter("JAR file", "*.jar"),
                        new FileChooser.ExtensionFilter("Class file", "*.class"),
                        new FileChooser.ExtensionFilter("All files", "*.*")
                );
                File f = fileChooser.showOpenDialog(primaryStage);
                openFile(f);
            } else {
                openFile(settings.getInput().get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFile(File f) {
        tabs.getTabs().clear();
        resetCache();
        TreeItem<String> item = new TreeItem<>(NLS.str("tree.loading"));
        fileTree.setRoot(item);
        new AsyncTask<Void, Void, JRoot>() {
            @Override
            public JRoot doInBackground(Void[] params) {
                wrapper.openFile(f);
                JRoot treeRoot = new JRoot(wrapper);
                treeRoot.setFlatPackages(settings.isFlattenPackage());
                return treeRoot;
            }

            @Override
            public void onPostExecute(JRoot result) {
                fileTree.setRoot(result);
                //TODO: Disabled for testing
//                runBackgroundJobs();
            }
        }.execute();
        stage.setTitle(DEFAULT_TITLE + " - " + f.getName());
        settings.addRecentFile(f.getAbsolutePath());
    }

    private synchronized void runBackgroundJobs() {
        cancelBackgroundJobs();
        backgroundWorker = new BackgroundWorker(cacheObject, this);
        if (settings.isAutoStartJobs()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    backgroundWorker.exec();
                }
            }, 1000);
        }
    }

    public synchronized void cancelBackgroundJobs() {
        if (backgroundWorker != null) {
            backgroundWorker.stop();
            backgroundWorker = new BackgroundWorker(cacheObject, this);
            resetCache();
        }
    }

    protected void resetCache() {
        cacheObject.reset();
        int threadsCount = 1;
        cacheObject.setDecompileJob(new DecompileJob(wrapper, threadsCount));
        cacheObject.setIndexJob(new IndexJob(wrapper, cacheObject, threadsCount));
    }

    public void openTab(JNode node) {
        Tab tab;
        if ((node instanceof JClass && node.getJParent() == null) || (node instanceof JResource && ((JResource) node).isText()))
            tab = new CodeView(node);
        else if (node instanceof JResource && ((JResource) node).isImage())
            tab = new DrawableView(node);
        else {
            if (node instanceof JField || node instanceof JMethod || (node instanceof JClass && node.getJParent() != null)) {
                JClass parent = node.getJParent();
                tab = new CodeView(parent);
                if (!tabs.getTabs().contains(tab)) {
                    tabs.getTabs().add(tab);
                    tabs.getSelectionModel().select(tab);
                    ((CodeView) tab).goTo(node);
                } else {
                    tabs.getSelectionModel().select(tab);
                    tabs.getTabs().stream().filter(tab1 -> tab1.equals(tab)).forEach(tab1 -> ((CodeView) tab1).goTo(node));
                }

            }
            return;
        }
        if (!tabs.getTabs().contains(tab)) {
            tabs.getTabs().add(tab);
            tabs.getSelectionModel().select(tab);
        } else {
            tabs.getSelectionModel().select(tab);
        }
    }

    public class DummyJavaNode extends JNode {

        @Override
        public JClass getJParent() {
            return null;
        }

        @Override
        public Node getIcon() {
            return null;
        }

        @Override
        public String makeString() {
            return null;
        }

        @Override
        public String getContent() {
            return JAVA_CODE;
        }

        @Override
        public String getName() {
            return "SampleClass.class";
        }

        @Override
        public String getSyntaxName() {
            return "java";
        }
    }

    public class DummyXmlNode extends JNode {

        @Override
        public JClass getJParent() {
            return null;
        }

        @Override
        public Node getIcon() {
            return null;
        }

        @Override
        public String makeString() {
            return null;
        }

        @Override
        public String getContent() {
            return MANIFEST_CODE;
        }

        @Override
        public String getName() {
            return "AndroidManifest.xml";
        }

        @Override
        public String getSyntaxName() {
            return "manifest";
        }
    }

}

