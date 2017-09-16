package jadx.fxgui.treemodel;

import jadx.api.JavaClass;
import jadx.api.JavaPackage;
import jadx.fxgui.utils.Utils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JPackage extends JNode implements Comparable<JPackage> {
    private static final long serialVersionUID = -4120718634156839804L;

    private static final Image PACKAGE_ICON = Utils.openIcon("package_obj");
    private final List<JClass> classes;
    private final List<JPackage> innerPackages = new ArrayList<>(1);
    private String name;

    public JPackage(JavaPackage pkg) {
        this.name = pkg.getName();
        List<JavaClass> javaClasses = pkg.getClasses();
        this.classes = new ArrayList<>(javaClasses.size());
        classes.addAll(javaClasses.stream().map(JClass::new).collect(Collectors.toList()));
        init();
        update();
    }

    public JPackage(String name) {
        this.name = name;
        this.classes = new ArrayList<>(1);
        init();
    }

    public final void update() {
        removeAllChildren();
        for (JPackage pkg : innerPackages) {
            pkg.update();
            add(pkg);
        }
        for (JClass cls : classes) {
            cls.update();
            add(cls);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        init();
    }

    public List<JPackage> getInnerPackages() {
        return innerPackages;
    }

    public List<JClass> getClasses() {
        return classes;
    }

    @Override
    public Node getIcon() {
        return new ImageView(PACKAGE_ICON);
    }

    @Override
    public JClass getJParent() {
        return null;
    }

    @Override
    public int getLine() {
        return 0;
    }

    @Override
    public int compareTo(@NotNull JPackage o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass() && name.equals(((JPackage) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String makeString() {
        return name;
    }

    @Override
    public String makeLongString() {
        return name;
    }
}
