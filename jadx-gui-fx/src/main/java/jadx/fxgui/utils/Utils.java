package jadx.fxgui.utils;

import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.utils.exceptions.JadxRuntimeException;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;

public class Utils {

    private static final Image ICON_STATIC = Utils.openIcon("static_co");
    private static final Image ICON_FINAL = Utils.openIcon("final_co");
    private static final Image ICON_ABSTRACT = Utils.openIcon("abstract_co");
    private static final Image ICON_NATIVE = Utils.openIcon("native_co");

    private Utils() {
    }

    public static Image openIcon(String name) {
        String iconPath = "/icons-16/" + name + ".png";
        InputStream is = Utils.class.getResourceAsStream(iconPath);
        if (is == null) {
            throw new JadxRuntimeException("Icon not found: " + iconPath);
        }
        return new Image(is);
    }

    public static String typeFormat(String name, ArgType type) {
        return name + " : " + typeStr(type);
    }

    public static String typeStr(ArgType type) {
        if (type == null) {
            return "null";
        }
        if (type.isObject()) {
            String cls = type.getObject();
            int dot = cls.lastIndexOf('.');
            if (dot != -1) {
                return cls.substring(dot + 1);
            } else {
                return cls;
            }
        }
        if (type.isArray()) {
            return typeStr(type.getArrayElement()) + "[]";
        }
        return type.toString();
    }

    public static Group makeIcon(AccessInfo af, Image pub, Image pri, Image pro, Image def) {
        Image icon;
        if (af.isPublic()) {
            icon = pub;
        } else if (af.isPrivate()) {
            icon = pri;
        } else if (af.isProtected()) {
            icon = pro;
        } else {
            icon = def;
        }
        Group overIcon = new Group(new ImageView(icon));
        if (af.isFinal()) {
            overIcon.getChildren().add(new ImageView(ICON_FINAL));
        }
        if (af.isStatic()) {
            overIcon.getChildren().add(new ImageView(ICON_STATIC));
        }
        if (af.isAbstract()) {
            overIcon.getChildren().add(new ImageView(ICON_ABSTRACT));
        }
        if (af.isNative()) {
            overIcon.getChildren().add(new ImageView(ICON_NATIVE));
        }
        return overIcon;
    }

    public static boolean isFreeMemoryAvailable() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalFree = runtime.freeMemory() + maxMemory - runtime.totalMemory();
        return totalFree > maxMemory * 0.2;
    }

    public static String memoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("heap: ").append(format(allocatedMemory - freeMemory));
        sb.append(", allocated: ").append(format(allocatedMemory));
        sb.append(", free: ").append(format(freeMemory));
        sb.append(", total free: ").append(format(freeMemory + maxMemory - allocatedMemory));
        sb.append(", max: ").append(format(maxMemory));

        return sb.toString();
    }

    private static String format(long mem) {
        return Long.toString((long) (mem / 1024. / 1024.)) + "MB";
    }

}
