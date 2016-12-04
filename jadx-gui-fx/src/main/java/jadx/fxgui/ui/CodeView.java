package jadx.fxgui.ui;

import jadx.api.JavaNode;
import jadx.fxgui.treemodel.JClass;
import jadx.fxgui.treemodel.JNode;
import jadx.fxgui.ui.syntax.BaseSyntax;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class CodeView extends Tab {

    private static final Pattern WORD = Pattern.compile("\\w+");
    @FXML
    public StackPane content;
    private CodeArea codeArea;
    private BaseSyntax syntax;
    private JNode node;
    private ContextMenu context;

    public CodeView(JNode node) {
        this.node = node;
        context = new ContextMenu();
        MenuItem item1 = new MenuItem("Copy");
        item1.setOnAction(e -> System.out.println("Copy"));
        MenuItem item2 = new MenuItem("Find usage");
        item2.setOnAction(e -> System.out.println("Find usage"));
        context.getItems().addAll(item1, item2);
        syntax = BaseSyntax.getFor(node.getSyntaxName());
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> syntax.computeHighlighting(codeArea));
        codeArea.replaceText(0, 0, node.getContent());
        codeArea.setEditable(false);
        codeArea.setOnContextMenuRequested(event -> {
            if (node instanceof JClass) {
                CharacterHit hit = codeArea.hit(event.getX(), event.getY());
                JClass clz = (JClass) node;
                JavaNode jnode = getJavaNodeAtOffset(clz, hit.getInsertionIndex());
                System.out.println(jnode);
                context.show(codeArea, event.getScreenX(), event.getScreenY());
            }
        });
        codeArea.setOnMouseClicked(event -> {
            context.hide();
            if (event.isControlDown() && node instanceof JClass) {
                CharacterHit hit = codeArea.hit(event.getX(), event.getY());
                JClass clz = (JClass) node;
                JavaNode jnode = getJavaNodeAtOffset(clz, hit.getInsertionIndex());
                System.out.println(jnode);
                if (jnode != null)
                    codeArea.moveTo(codeArea.position(clz.getCls().getDefinitionPosition(jnode).getLine() - 1, 0).toOffset());
            }
        });
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

//    private Position getDefPosition(JClass jCls, int offset) {
//        JavaNode node = getJavaNodeAtOffset(jCls, offset);
//        if (node == null) {
//            return null;
//        }
//        CodePosition pos = jCls.getCls().getDefinitionPosition(node);
//        if (pos == null) {
//            return null;
//        }
//        return new Position(pos);
//    }

    private JavaNode getJavaNodeAtOffset(JClass jCls, int offset) {
        int index = offset;
        Matcher matcher = WORD.matcher(codeArea.getText());
        while (matcher.find()) {
            if (matcher.start() <= offset && matcher.end() >= offset) {
                index = matcher.start();
            }
        }
        int[] ints = toLineAndCol(index);
        int line = ints[0];
        int lineOffset = ints[1];
        return jCls.getCls().getJavaNodeAtPosition(line, lineOffset);
    }

    private int[] toLineAndCol(int index) {
        int c = 0;
        int line = 1, col = 1;
        while (c <= index) {
            if (codeArea.getText().charAt(c) == '\n') {
                ++line;
                col = 1;
            } else {
                ++col;
            }
            c++;
        }
        return new int[]{line, col - 1};
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
