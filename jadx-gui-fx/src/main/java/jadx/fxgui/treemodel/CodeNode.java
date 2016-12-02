package jadx.fxgui.treemodel;

import jadx.api.JavaNode;
import jadx.fxgui.utils.search.StringRef;
import javafx.scene.Node;


public class CodeNode extends JNode {

	private static final long serialVersionUID = 1658650786734966545L;

	private final JNode jNode;
	private final JClass jParent;
	private final StringRef line;
	private final int lineNum;

	public CodeNode(JNode jNode, int lineNum, StringRef line) {
		this.jNode = jNode;
		this.jParent = this.jNode.getJParent();
		this.line = line;
		this.lineNum = lineNum;
		init();
	}

	public Node getIcon() {
		return jNode.getIcon();
	}

	public JavaNode getJavaNode() {
		return jNode.getJavaNode();
	}

	public JClass getJParent() {
		return getRootClass();
	}

	public JClass getRootClass() {
		JClass parent = jParent;
		if (parent != null) {
			return parent.getRootClass();
		}
		if (jNode instanceof JClass) {
			return (JClass) jNode;
		}
		return null;
	}

	public int getLine() {
		return lineNum;
	}

	public String makeDescString() {
		return line.toString();
	}

	public boolean hasDescString() {
		return true;
	}

	public String makeString() {
		return jNode.makeLongString();
	}

	public String makeLongString() {
		return makeString();
	}
}
