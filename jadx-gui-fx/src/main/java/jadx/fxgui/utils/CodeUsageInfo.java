package jadx.fxgui.utils;

import jadx.api.CodePosition;
import jadx.api.JavaClass;
import jadx.api.JavaNode;
import jadx.fxgui.treemodel.CodeNode;
import jadx.fxgui.treemodel.JNode;
import jadx.fxgui.utils.search.StringRef;

import java.util.*;

public class CodeUsageInfo {

    private final JNodeCache nodeCache;
    private final Map<JNode, UsageInfo> usageMap = new HashMap<>();

    public CodeUsageInfo(JNodeCache nodeCache) {
        this.nodeCache = nodeCache;
    }

    public void processClass(JavaClass javaClass, CodeLinesInfo linesInfo, List<StringRef> lines) {
        Map<CodePosition, JavaNode> usage = javaClass.getUsageMap();
        for (Map.Entry<CodePosition, JavaNode> entry : usage.entrySet()) {
            CodePosition codePosition = entry.getKey();
            JavaNode javaNode = entry.getValue();
            addUsage(nodeCache.makeFrom(javaNode), javaClass, linesInfo, codePosition, lines);
        }
    }

    private void addUsage(JNode jNode, JavaClass javaClass,
                          CodeLinesInfo linesInfo, CodePosition codePosition, List<StringRef> lines) {
        UsageInfo usageInfo = usageMap.computeIfAbsent(jNode, k -> new UsageInfo());
        int line = codePosition.getLine();
        JavaNode javaNodeByLine = linesInfo.getJavaNodeByLine(line);
        StringRef codeLine = lines.get(line - 1);
        JNode node = nodeCache.makeFrom(javaNodeByLine == null ? javaClass : javaNodeByLine);
        CodeNode codeNode = new CodeNode(node, line, codeLine);
        usageInfo.getUsageList().add(codeNode);
    }

    public List<CodeNode> getUsageList(JNode node) {
        UsageInfo usageInfo = usageMap.get(node);
        if (usageInfo == null) {
            return Collections.emptyList();
        }
        return usageInfo.getUsageList();
    }

    public static class UsageInfo {
        private final List<CodeNode> usageList = new ArrayList<>();

        public List<CodeNode> getUsageList() {
            return usageList;
        }
    }
}
