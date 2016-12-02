package jadx.fxgui.treemodel;

import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.fxgui.utils.Utils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Iterator;

public class JMethod extends JNode {
    private static final long serialVersionUID = 3834526867464663751L;

    private static final Image ICON_MTH_DEF = Utils.openIcon("methdef_obj");
    private static final Image ICON_MTH_PRI = Utils.openIcon("methpri_obj");
    private static final Image ICON_MTH_PRO = Utils.openIcon("methpro_obj");
    private static final Image ICON_MTH_PUB = Utils.openIcon("methpub_obj");

    private static final Image ICON_CONSTRUCTOR = Utils.openIcon("constr_ovr");
    private static final Image ICON_SYNC = Utils.openIcon("synch_co");

    private final JavaMethod mth;
    private final JClass jParent;

    public JMethod(JavaMethod javaMethod, JClass jClass) {
        this.mth = javaMethod;
        this.jParent = jClass;
    }

    @Override
    public JavaNode getJavaNode() {
        return mth;
    }

    @Override
    public JClass getJParent() {
        return jParent;
    }

    public ArgType getReturnType() {
        return mth.getReturnType();
    }

    @Override
    public JClass getRootClass() {
        return jParent.getRootClass();
    }

    @Override
    public int getLine() {
        return mth.getDecompiledLine();
    }

    @Override
    public Node getIcon() {
        AccessInfo accessFlags = mth.getAccessFlags();
        Group icon = Utils.makeIcon(accessFlags, ICON_MTH_PUB, ICON_MTH_PRI, ICON_MTH_PRO, ICON_MTH_DEF);
        if (accessFlags.isConstructor()) {
            icon.getChildren().add(new ImageView(ICON_CONSTRUCTOR));
        }
        if (accessFlags.isSynchronized()) {
            icon.getChildren().add(new ImageView(ICON_SYNC));
        }
        return icon;
    }

    String makeBaseString() {
        if (mth.isClassInit()) {
            return "{...}";
        }
        StringBuilder base = new StringBuilder();
        if (mth.isConstructor()) {
            base.append(mth.getDeclaringClass().getName());
        } else {
            base.append(mth.getName());
        }
        base.append('(');
        for (Iterator<ArgType> it = mth.getArguments().iterator(); it.hasNext(); ) {
            base.append(Utils.typeStr(it.next()));
            if (it.hasNext()) {
                base.append(", ");
            }
        }
        base.append(')');
        return base.toString();
    }

    @Override
    public String makeString() {
        return Utils.typeFormat(makeBaseString(), getReturnType());
    }

    @Override
    public String makeLongString() {
        String name = mth.getDeclaringClass().getFullName() + "." + makeBaseString();
        return Utils.typeFormat(name, getReturnType());
    }

    @Override
    public int hashCode() {
        return mth.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof JMethod && mth.equals(((JMethod) o).mth);
    }
}
