package jadx.fxgui.ui;

import jadx.api.JavaClass;
import jadx.api.JavaField;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.core.dex.nodes.ProcessState;
import jadx.fxgui.JadxFxGUI;
import jadx.fxgui.treemodel.CodeNode;
import jadx.fxgui.treemodel.JClass;
import jadx.fxgui.treemodel.JMethod;
import jadx.fxgui.treemodel.JNode;
import jadx.fxgui.ui.syntax.BaseSyntax;
import jadx.fxgui.utils.AsyncTask;
import jadx.fxgui.utils.NLS;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CharacterHit;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by stirante
 */
public class CodeView extends Tab {

    private static final Pattern WORD = Pattern.compile("\\w+");
    private final MenuItem copyItem;
    private final MenuItem usageItem;
    private final MenuItem renameItem;
    @FXML
    public StackPane content;
    private CodeArea codeArea;
    private BaseSyntax syntax;
    private final JadxFxGUI app;
    private JNode node;
    private ContextMenu context;
    private JavaNode currentNode;
    private boolean initialized = false;
    private int line = 0;

    public CodeView(JadxFxGUI app, JNode node) {
        this.app = app;
        this.node = node;
        context = new ContextMenu();
        copyItem = new MenuItem(NLS.str("popup.copy"));
        copyItem.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(codeArea.getSelectedText());
            clipboard.setContent(content);
        });
        usageItem = new MenuItem(NLS.str("popup.find_usage"));
        usageItem.setOnAction(e -> {
            JNode jNode = app.getCacheObject().getNodeCache().makeFrom(currentNode);
            List<CodeNode> usageList = app.getCacheObject().getUsageInfo().getUsageList(jNode);
            new UsageWindow(app, jNode, usageList).show();
        });
        renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> {
            if (currentNode instanceof JavaClass) {
                Optional<String> s = Dialogs.queryUserInput("Rename", "Rename class", currentNode.getFullName() + "=>");
                if (s.isPresent()) {
                    app.getDecompiler().getDeobfuscator().rename(((JavaClass) currentNode).getClassInfo(), s.get());
                    app.refreshTabs();
                }
            } else if (currentNode instanceof JavaMethod && !((JavaMethod) currentNode).isConstructor()) {
                Optional<String> s = Dialogs.queryUserInput("Rename", "Rename method", currentNode.getFullName() + "=>");
                if (s.isPresent()) {
                    app.getDecompiler().getDeobfuscator().rename(((JavaMethod) currentNode).getMethodInfo(), s.get());
                    app.refreshTabs();
                }
            } else if (currentNode instanceof JavaField) {
                Optional<String> s = Dialogs.queryUserInput("Rename", "Rename field", currentNode.getFullName() + "=>");
                if (s.isPresent()) {
                    app.getDecompiler().getDeobfuscator().rename(((JavaField) currentNode).getFieldInfo(), s.get());
                    app.refreshTabs();
                }
            } else if (currentNode instanceof JavaMethod && ((JavaMethod) currentNode).isConstructor()) {
                //theoretically impossible for the code to reach here
                Dialogs.showWarning("Rename", "You can't rename constructor!");
            }
        });
        context.getItems().addAll(copyItem, renameItem, usageItem);
        syntax = BaseSyntax.getFor(node.getSyntaxName());
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> syntax.computeHighlighting(codeArea));
        codeArea.replaceText(0, 0, NLS.str("class.loading"));
        new AsyncTask<Void, Void, String>() {

            @Override
            public String doInBackground(Void[] params) {
                return node.getContent();
            }

            @Override
            public void onPostExecute(String result) {
                codeArea.replaceText(0, codeArea.getLength(), result);
                codeArea.moveTo(codeArea.position(line, 0).toOffset());
                initialized = true;
            }
        }.execute();
        codeArea.setEditable(false);
        codeArea.setOnContextMenuRequested(event -> {
            context.show(codeArea, event.getScreenX(), event.getScreenY());
            String selectedText = codeArea.getSelectedText();
            copyItem.setDisable(selectedText == null || selectedText.isEmpty());
            if (node instanceof JClass) {
                CharacterHit hit = codeArea.hit(event.getX(), event.getY());
                JClass clz = (JClass) node;
                currentNode = getJavaNodeAtOffset(clz, hit.getInsertionIndex());
                usageItem.setDisable(currentNode == null);
                renameItem.setDisable((currentNode == null) || ((currentNode instanceof JMethod) && ((JavaMethod) ((JMethod) currentNode).getJavaNode()).isConstructor()));
            }
        });
        codeArea.setOnMouseClicked(event -> {
            context.hide();
            if (event.isControlDown() && node instanceof JClass) {
                CharacterHit hit = codeArea.hit(event.getX(), event.getY());
                JClass clz = (JClass) node;
                JavaNode jnode = getJavaNodeAtOffset(clz, hit.getInsertionIndex());
                System.out.println(jnode);
                if (jnode != null) {
                    if (jnode.equals(((JClass) node).getCls()))
                        codeArea.moveTo(codeArea.position(clz.getCls().getDefinitionPosition(jnode).getLine() - 1, 0).toOffset());
                    else {
                        app.openTab(jnode);
                    }
                }
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

    public void goTo(JNode node) {
        if (initialized)
            codeArea.moveTo(codeArea.position(node.getLine() - 1, 0).toOffset());
        else line = node.getLine() - 1;
    }

    public void goTo(JavaNode node) {
        int i = ((JClass) this.node).getCls().getDefinitionPosition(node).getLine() - 1;
        if (initialized)
            codeArea.moveTo(codeArea.position(i, 0).toOffset());
        else line = i;
    }

    public void goTo(int line) {
        if (initialized)
            codeArea.moveTo(codeArea.position(line - 1, 0).toOffset());
        else this.line = line - 1;
    }

    public void refresh() {
        int pos = codeArea.getCaretPosition();
        if (node instanceof JClass) {
            ((JClass) node).getCls().getClassNode().setState(ProcessState.NOT_LOADED);
            ((JClass) node).getCls().decompile();
        }
        setText(node.getName());
        codeArea.replaceText(0, codeArea.getLength(), node.getContent());
        codeArea.moveTo(pos);
    }
}
