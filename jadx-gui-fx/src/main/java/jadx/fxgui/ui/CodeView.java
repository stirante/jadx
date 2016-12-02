package jadx.fxgui.ui;

import jadx.fxgui.treemodel.JNode;
import jadx.fxgui.utils.syntax.BaseSyntax;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;

/**
 * Created by stirante
 */
public class CodeView extends Tab {

    @FXML
    public StackPane content;
    private CodeArea codeArea;
    private BaseSyntax syntax;
    private JNode node;

    public CodeView(JNode node) {
        this.node = node;
        syntax = BaseSyntax.getFor(node.getSyntaxName());
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> syntax.computeHighlighting(codeArea));
        codeArea.replaceText(0, 0, node.getContent());
        codeArea.setEditable(false);
        FXMLLoader loader = new FXMLLoader(CodeView.class.getResource("/Tab.fxml"));
        loader.setController(this);
        try {
            AnchorPane pane = loader.load();
            VirtualizedScrollPane e = new VirtualizedScrollPane<>(codeArea);
            content.getChildren().add(e);
            setContent(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setText(node.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CodeView codeView = (CodeView) o;

        return node != null && node.equals(codeView.node);

    }

    @Override
    public int hashCode() {
        return node != null ? node.hashCode() : 0;
    }
}
