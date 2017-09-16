package jadx.fxgui.utils;

import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.fxgui.treemodel.JClass;
import jadx.fxgui.treemodel.JField;
import jadx.fxgui.treemodel.JMethod;
import jadx.fxgui.treemodel.JNode;

import java.util.HashMap;
import java.util.Map;

public class JNodeCache {

	private final Map<JavaNode, JNode> cache = new HashMap<>();

	public JNode makeFrom(JavaNode javaNode) {
		if (javaNode == null) {
			return null;
		}
        JNode jNode = cache.computeIfAbsent(javaNode, this::convert);
        return jNode;
	}

	private JNode convert(JavaNode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof JavaClass) {
			JClass p = (JClass) makeFrom(node.getDeclaringClass());
			return new JClass((JavaClass) node, p);
		}
		if (node instanceof JavaMethod) {
			JavaMethod mth = (JavaMethod) node;
			return new JMethod(mth, (JClass) makeFrom(mth.getDeclaringClass()));
		}
		if (node instanceof JavaField) {
			JavaField fld = (JavaField) node;
			return new JField(fld, (JClass) makeFrom(fld.getDeclaringClass()));
		}
		throw new JadxRuntimeException("Unknown type for JavaNode: " + node.getClass());
	}
}
