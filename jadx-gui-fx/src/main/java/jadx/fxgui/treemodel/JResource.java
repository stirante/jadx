package jadx.fxgui.treemodel;

import jadx.api.ResourceFile;
import jadx.api.ResourceFileContent;
import jadx.api.ResourceType;
import jadx.core.codegen.CodeWriter;
import jadx.core.xmlgen.ResContainer;
import jadx.fxgui.utils.Utils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JResource extends JNode implements Comparable<JResource> {
    private static final long serialVersionUID = -201018424302612434L;

    private static final Image ROOT_ICON = Utils.openIcon("cf_obj");
    private static final Image FOLDER_ICON = Utils.openIcon("folder");
    private static final Image FILE_ICON = Utils.openIcon("file_obj");
    private static final Image MANIFEST_ICON = Utils.openIcon("template_obj");
    private static final Image JAVA_ICON = Utils.openIcon("java_ovr");
    private static final Image ERROR_ICON = Utils.openIcon("error_co");
    private final String name;
    private final String shortName;
    private final List<JResource> files = new ArrayList<>(1);
    private final JResType type;
    private final ResourceFile resFile;
    private boolean loaded;
    private String content;
    private Map<Integer, Integer> lineMapping;

    public JResource(ResourceFile resFile, String name, JResType type) {
        this(resFile, name, name, type);
    }

    public JResource(ResourceFile resFile, String name, String shortName, JResType type) {
        this.resFile = resFile;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        init();
    }

    private static void addPath(String[] path, JResource root, JResource jResource) {
        if (path.length == 1) {
            root.getFiles().add(jResource);
            return;
        }
        int last = path.length - 1;
        for (int i = 0; i <= last; i++) {
            String f = path[i];
            if (i == last) {
                root.getFiles().add(jResource);
            } else {
                root = getResDir(root, f);
            }
        }
    }

    private static JResource getResDir(JResource root, String dirName) {
        for (JResource file : root.getFiles()) {
            if (file.getName().equals(dirName)) {
                return file;
            }
        }
        JResource resDir = new JResource(null, dirName, JResType.DIR);
        root.getFiles().add(resDir);
        return resDir;
    }

    public static boolean isSupportedForView(ResourceType type) {
        switch (type) {
            case CODE:
            case FONT:
            case LIB:
            case UNKNOWN:
                return false;

            case MANIFEST:
            case XML:
            case ARSC:
            case IMG:
                return true;
        }
        return true;
    }

    public final void update() {
        removeAllChildren();
        loadContent();
        for (JResource res : files) {
            res.update();
            add(res);
        }
    }

    protected void loadContent() {
        getContent();
        files.forEach(JResource::loadContent);
    }

    public String getName() {
        return name;
    }

    public List<JResource> getFiles() {
        return files;
    }

    public String getContent() {
        if (!loaded && resFile != null && type == JResType.FILE) {
            loaded = true;
            if (isSupportedForView(resFile.getType())) {
                ResContainer rc = resFile.loadContent();
                if (rc != null) {
                    addSubFiles(rc, this, 0);
                }
            }
        }
        return content;
    }

    protected void addSubFiles(ResContainer rc, JResource root, int depth) {
        CodeWriter cw = rc.getContent();
        if (cw != null) {
            if (depth == 0) {
                root.lineMapping = cw.getLineMapping();
                root.content = cw.toString();
            } else {
                String name = rc.getName();
                String[] path = name.split("/");
                String shortName = path.length == 0 ? name : path[path.length - 1];
                ResourceFileContent fileContent = new ResourceFileContent(shortName, ResourceType.XML, cw);
                addPath(path, root, new JResource(fileContent, name, shortName, JResType.FILE));
            }
        }
        List<ResContainer> subFiles = rc.getSubFiles();
        if (!subFiles.isEmpty()) {
            for (ResContainer subFile : subFiles) {
                addSubFiles(subFile, root, depth + 1);
            }
        }
    }

    @Override
    public Integer getSourceLine(int line) {
        if (lineMapping == null) {
            return null;
        }
        return lineMapping.get(line);
    }

    @Override
    public String getSyntaxName() {
        if (resFile == null) {
            return null;
        }
        switch (resFile.getType()) {
            case CODE:
                return super.getSyntaxName();

            case MANIFEST:
            case XML:
                return "xml";
        }
        String syntax = getSyntaxByExtension(resFile.getName());
        if (syntax != null) {
            return syntax;
        }
        return super.getSyntaxName();
    }

    private String getSyntaxByExtension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot == -1) {
            return null;
        }
        String ext = name.substring(dot + 1);
        if (ext.equals("js")) {
            return "js";
        }
        if (ext.equals("css")) {
            return "css";
        }
        if (ext.equals("html")) {
            return "xml";
        }
        return null;
    }

    @Override
    public Node getIcon() {
        switch (type) {
            case ROOT:
                return new ImageView(ROOT_ICON);
            case DIR:
                return new ImageView(FOLDER_ICON);

            case FILE:
                ResourceType resType = resFile.getType();
                if (resType == ResourceType.MANIFEST) {
                    return new ImageView(MANIFEST_ICON);
                }
                if (resType == ResourceType.CODE) {
                    return new Group(new ImageView(FILE_ICON), new ImageView(ERROR_ICON), new ImageView(JAVA_ICON));
                }
                if (!isSupportedForView(resType)) {
                    return new Group(new ImageView(FILE_ICON), new ImageView(ERROR_ICON));
                }
                return new ImageView(FILE_ICON);
        }
        return new ImageView(FILE_ICON);
    }

    public ResourceFile getResFile() {
        return resFile;
    }

    @Override
    public JClass getJParent() {
        return null;
    }

    @Override
    public int compareTo(JResource o) {
        return name.compareTo(o.name);
    }

    @Override
    public String makeString() {
        return shortName;
    }

    public boolean isText() {
        return type == JResType.FILE && isSupportedForView(resFile.getType()) && resFile.getType() != ResourceType.IMG;
    }

    public boolean isImage() {
        return type == JResType.FILE && resFile.getType() == ResourceType.IMG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass() && name.equals(((JResource) o).name) && resFile.equals(((JResource) o).resFile);
    }

    @Override
    public int hashCode() {
        return name.hashCode() * resFile.hashCode();
    }

    public enum JResType {
        ROOT,
        DIR,
        FILE
    }

}
