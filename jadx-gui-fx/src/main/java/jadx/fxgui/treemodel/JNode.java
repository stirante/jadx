package jadx.fxgui.treemodel;

import jadx.api.JavaNode;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

import java.io.Serializable;

public abstract class JNode extends TreeItem<String> implements Serializable {
//    protected Vector<JNode> children = new Vector<>();

    protected void init() {
        setValue(toString());
        setGraphic(getIcon());
    }

    protected void add(JNode node) {
//        children.add(node);
        getChildren().add(node);
    }

    protected void removeAllChildren() {
//        children.clear();
        getChildren().clear();
    }

    public abstract JClass getJParent();

    public JClass getRootClass() {
        return null;
    }

    public JavaNode getJavaNode() {
        return null;
    }

    public String getContent() {
        return null;
    }

    public String getSyntaxName() {
        return "";
    }

    public int getLine() {
        return 0;
    }

    public Integer getSourceLine(int line) {
        return null;
    }

    public abstract Node getIcon();

    public String getName() {
        JavaNode javaNode = getJavaNode();
        if (javaNode == null) {
            return null;
        }
        return javaNode.getName();
    }

    public abstract String makeString();

    public String makeDescString() {
        return null;
    }

    public boolean hasDescString() {
        return false;
    }

    public void invalidateValues() {
        setValue(toString());
        for (TreeItem<String> item : getChildren()) {
            if (item instanceof JNode) ((JNode) item).invalidateValues();
        }
    }


    public String makeLongString() {
        return makeString();
    }

    @Override
    public String toString() {
        return makeString();
    }

}
