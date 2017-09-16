package jadx.fxgui.ui;

import jadx.fxgui.JadxFxGUI;
import jadx.fxgui.treemodel.CodeNode;
import jadx.fxgui.treemodel.JNode;
import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.List;

/**
 * Created by stirante
 */
public class UsageWindow extends CommonFindDialog {

    private final JadxFxGUI app;
    private final JNode node;

    public UsageWindow(JadxFxGUI app, JNode node, List<CodeNode> results) {
        this.app = app;
        this.node = node;
        getResult().addAll(results);
    }

    @Override
    protected String getTitle() {
        return "Find Usage";
    }

    @Override
    protected Node getNode() {
        return new Label("Usage for " + node.getName());
    }

    @Override
    protected void onRowClicked(CodeNode node) {
        app.openTab(node);
    }
}
