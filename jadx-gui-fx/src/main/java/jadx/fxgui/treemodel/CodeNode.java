package jadx.fxgui.treemodel;

import jadx.api.JavaNode;
import jadx.fxgui.utils.search.StringRef;


public class CodeNode {

    private final JNode jNode;
    private final StringRef line;
    private final int lineNum;

    public CodeNode(JNode jNode, int lineNum, StringRef line) {
        this.jNode = jNode;
        this.line = line;
        this.lineNum = lineNum;
    }

    public JavaNode getJavaNode() {
        return jNode.getJavaNode();
    }

    public JClass getRootClass() {
        JClass parent = jNode.getJParent();
        if (parent == null && jNode instanceof JClass) return (JClass) jNode;
        else if (parent == null) return null;
        while (parent.getJParent() != null) {
            parent = parent.getJParent();
        }
        return parent;
    }

    public JClass getContainingClass() {
        if (jNode instanceof JClass) return (JClass) jNode;
        else return jNode.getJParent();
    }

    public int getLine() {
        return lineNum;
    }

    public String getLineString() {
        return getRootClass().getContent().split("\n")[getLine() - 1].trim();
    }

    @Override
    public String toString() {
        return getLineString();
    }
}
