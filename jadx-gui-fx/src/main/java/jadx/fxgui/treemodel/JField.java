package jadx.fxgui.treemodel;

import jadx.api.JavaField;
import jadx.api.JavaNode;
import jadx.core.dex.info.AccessInfo;
import jadx.fxgui.utils.Utils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class JField extends JNode {
    private static final long serialVersionUID = 1712572192106793359L;

    private static final Image ICON_FLD_DEF = Utils.openIcon("field_default_obj");
    private static final Image ICON_FLD_PRI = Utils.openIcon("field_private_obj");
    private static final Image ICON_FLD_PRO = Utils.openIcon("field_protected_obj");
    private static final Image ICON_FLD_PUB = Utils.openIcon("field_public_obj");

    private static final Image ICON_TRANSIENT = Utils.openIcon("transient_co");
    private static final Image ICON_VOLATILE = Utils.openIcon("volatile_co");

    private final JavaField field;
    private final JClass jParent;

    public JField(JavaField javaField, JClass jClass) {
        this.field = javaField;
        this.jParent = jClass;
        init();
    }

    @Override
    public JavaNode getJavaNode() {
        return field;
    }

    @Override
    public JClass getJParent() {
        return jParent;
    }

    @Override
    public JClass getRootClass() {
        return jParent.getRootClass();
    }

    @Override
    public int getLine() {
        return field.getDecompiledLine();
    }

    @Override
    public Node getIcon() {
        AccessInfo af = field.getAccessFlags();
        Group icon = Utils.makeIcon(af, ICON_FLD_PUB, ICON_FLD_PRI, ICON_FLD_PRO, ICON_FLD_DEF);
        if (af.isTransient()) {
            icon.getChildren().add(new ImageView(ICON_TRANSIENT));
        }
        if (af.isVolatile()) {
            icon.getChildren().add(new ImageView(ICON_VOLATILE));
        }
        return icon;
    }

    @Override
    public String makeString() {
        return Utils.typeFormat(field.getName(), field.getType());
    }

    @Override
    public String makeLongString() {
        return Utils.typeFormat(field.getFullName(), field.getType());
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof JField && field.equals(((JField) o).field);
    }
}
