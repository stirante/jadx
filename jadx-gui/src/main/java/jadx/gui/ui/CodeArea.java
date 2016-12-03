package jadx.gui.ui;

import jadx.api.*;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.args.ArgType;
import jadx.gui.settings.JadxSettings;
import jadx.gui.treemodel.JClass;
import jadx.gui.treemodel.JNode;
import jadx.gui.utils.Position;
import org.fife.ui.rsyntaxtextarea.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

class CodeArea extends RSyntaxTextArea {
    public static final Color BACKGROUND = new Color(0xFAFAFA);
    public static final Color JUMP_TOKEN_FGD = new Color(0x491BA1);
    private static final Logger LOG = LoggerFactory.getLogger(CodeArea.class);
    private static final long serialVersionUID = 6312736869579635796L;
    private static final String GET_OBJECT = "XposedHelpers.getObjectField(object, \"%fieldName\");";
    private static final String GET_STATIC_OBJECT = "XposedHelpers.getStaticObjectField(%classObj, \"%fieldName\");";
    private static final String METHOD_HOOK = "XposedHelpers.findAndHookMethod(\"%className\", lpparam.classLoader, \"%methodName\", %paramshook);";
    private static final String CONSTRUCTOR_HOOK = "XposedHelpers.findAndHookConstructor(\"%className\", lpparam.classLoader, %paramshook);";
    private static final String SET_OBJECT = "XposedHelpers.setObjectField(object, \"%fieldName\", value);";
    private static final String SET_STATIC_OBJECT = "XposedHelpers.setStaticObjectField(%classObj, \"%fieldName\", value);";
    private static final String METHOD_CALL = "XposedHelpers.callMethod(object, \"%methodName\", new Class[]{%params}, params);";
    private static final String METHOD_STATIC_CALL = "XposedHelpers.callStaticMethod(%classObj, \"%methodName\", new Class[]{%params}, params);";
    private static final String NEW_INSTANCE = "XposedHelpers.newInstance(%classObj, new Class[]{%params}, params);";
    private static final String FIND_CLASS = "XposedHelpers.findClass(\"%className\", lpparam.classLoader)";
    private final CodePanel contentPanel;
    private final JNode node;

    CodeArea(CodePanel panel) {
        this.contentPanel = panel;
        this.node = panel.getNode();

        setMarkOccurrences(true);
        setBackground(BACKGROUND);
        setAntiAliasingEnabled(true);
        setEditable(false);
        loadSettings();
        Caret caret = getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret) caret).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }
        caret.setVisible(true);

        setSyntaxEditingStyle(node.getSyntaxName());

        if (node instanceof JClass) {
            SyntaxScheme scheme = getSyntaxScheme();
            scheme.getStyle(Token.FUNCTION).foreground = Color.BLACK;

            setHyperlinksEnabled(true);
            CodeLinkGenerator codeLinkProcessor = new CodeLinkGenerator((JClass) node);
            setLinkGenerator(codeLinkProcessor);
            addHyperlinkListener(codeLinkProcessor);
            addMenuItems(this, (JClass) node);
        }

        setText(node.getContent());
    }

    static Position getDefPosition(JClass jCls, RSyntaxTextArea textArea, int offset) {
        JavaNode node = getJavaNodeAtOffset(jCls, textArea, offset);
        if (node == null) {
            return null;
        }
        CodePosition pos = jCls.getCls().getDefinitionPosition(node);
        if (pos == null) {
            return null;
        }
        return new Position(pos);
    }

    static JavaNode getJavaNodeAtOffset(JClass jCls, RSyntaxTextArea textArea, int offset) {
        try {
            int line = textArea.getLineOfOffset(offset);
            int lineOffset = offset - textArea.getLineStartOffset(line);
            return jCls.getCls().getJavaNodeAtPosition(line + 1, lineOffset + 1);
        } catch (BadLocationException e) {
            LOG.error("Can't get java node by offset", e);
        }
        return null;
    }

    private void addMenuItems(CodeArea codeArea, JClass jCls) {
        // TODO: hotkey works only when popup menu is shown
        // findUsage.putValue(Action.ACCELERATOR_KEY, getKeyStroke(KeyEvent.VK_F7, KeyEvent.ALT_DOWN_MASK));

        JPopupMenu popup = getPopupMenu();
        popup.addSeparator();

        Action findUsage = new FindUsageAction(codeArea, jCls);
        popup.add(findUsage);
        popup.addPopupMenuListener((PopupMenuListener) findUsage);

        Action genHook = new GenerateHookAction(codeArea, jCls);
        popup.add(genHook);
        popup.addPopupMenuListener((PopupMenuListener) genHook);


        Action genCall = new GenerateCallerAction(codeArea, jCls);
        popup.add(genCall);
        popup.addPopupMenuListener((PopupMenuListener) genCall);

        Action genJOBF = new GenerateJOBFString(codeArea, jCls);
        popup.add(genJOBF);
        popup.addPopupMenuListener((PopupMenuListener) genJOBF);
    }

    public void loadSettings() {
        JadxSettings settings = contentPanel.getTabbedPane().getMainWindow().getSettings();
        setFont(settings.getFont());
    }

    private boolean isJumpToken(Token token) {
        if (token.getType() == TokenTypes.IDENTIFIER) {
            // fast skip
            if (token.length() == 1) {
                char ch = token.getTextArray()[token.getTextOffset()];
                if (ch == '.' || ch == ',' || ch == ';') {
                    return false;
                }
            }
            if (node instanceof JClass) {
                Position pos = getDefPosition((JClass) node, this, token.getOffset());
                if (pos != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Color getForegroundForToken(Token t) {
        if (isJumpToken(t)) {
            return JUMP_TOKEN_FGD;
        }
        return super.getForegroundForToken(t);
    }

    public Position getCurrentPosition() {
        return new Position(node, getCaretLineNumber() + 1);
    }

    Integer getSourceLine(int line) {
        return node.getSourceLine(line);
    }

    void scrollToLine(int line) {
        int lineNum = line - 1;
        if (lineNum < 0) {
            lineNum = 0;
        }
        setCaretAtLine(lineNum);
        centerCurrentLine();
        forceCurrentLineHighlightRepaint();
    }

    public void centerCurrentLine() {
        JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if (viewport == null) {
            return;
        }
        try {
            Rectangle r = modelToView(getCaretPosition());
            if (r == null) {
                return;
            }
            int extentHeight = viewport.getExtentSize().height;
            Dimension viewSize = viewport.getViewSize();
            if (viewSize == null) {
                return;
            }
            int viewHeight = viewSize.height;

            int y = Math.max(0, r.y - extentHeight / 2);
            y = Math.min(y, viewHeight - extentHeight);

            viewport.setViewPosition(new Point(0, y));
        } catch (BadLocationException e) {
            LOG.debug("Can't center current line", e);
        }
    }

    private void setCaretAtLine(int line) {
        try {
            setCaretPosition(getLineStartOffset(line));
        } catch (BadLocationException e) {
            LOG.debug("Can't scroll to {}", line, e);
        }
    }

    private class FindUsageAction extends AbstractAction implements PopupMenuListener {
        private static final long serialVersionUID = 4692546569977976384L;

        private final CodeArea codeArea;
        private final JClass jCls;

        private JavaNode node;

        public FindUsageAction(CodeArea codeArea, JClass jCls) {
            super("Find Usage");
            this.codeArea = codeArea;
            this.jCls = jCls;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (node == null) {
                return;
            }
            MainWindow mainWindow = contentPanel.getTabbedPane().getMainWindow();
            JNode jNode = mainWindow.getCacheObject().getNodeCache().makeFrom(node);
            UsageDialog usageDialog = new UsageDialog(mainWindow, jNode);
            usageDialog.setVisible(true);
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            node = null;
            Point pos = codeArea.getMousePosition();
            if (pos != null) {
                Token token = codeArea.viewToToken(pos);
                if (token != null) {
                    node = getJavaNodeAtOffset(jCls, codeArea, token.getOffset());
                }
            }
            setEnabled(node != null);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    private static String generateParams(JavaMethod meth) {
        String params = "";
        for (ArgType arg : meth.getArguments()) {
            if (!params.isEmpty()) params += ", ";
            if (arg.isPrimitive())
                params += arg.getPrimitiveType().getLongName() + ".class";
            else if (arg.getObject().equalsIgnoreCase(ArgType.STRING.getObject()))
                params += "String.class";
            else if (arg.getObject().equalsIgnoreCase(ArgType.OBJECT.getObject()))
                params += "Object.class";
            else
                params += FIND_CLASS.replace("%className", arg.getObject());
        }
        return params;
    }

    private class GenerateHookAction extends AbstractAction implements PopupMenuListener {
        private static final long serialVersionUID = 4692546569977976385L;

        private final CodeArea contentArea;
        private final JClass jCls;

        private JavaNode node;

        public GenerateHookAction(CodeArea contentArea, JClass jCls) {
            super("Generate Xposed getter/hook/finder");
            this.contentArea = contentArea;
            this.jCls = jCls;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (node == null) {
                return;
            }
            MainWindow mainWindow = contentPanel.getTabbedPane().getMainWindow();
            if (node instanceof JavaMethod) {
                JavaMethod meth = (JavaMethod) node;
                String hook;
                if (meth.getName().equalsIgnoreCase("<init>"))
                    hook = CONSTRUCTOR_HOOK;
                else
                    hook = METHOD_HOOK;
                hook = hook.replace("%className", meth.getDeclaringClass().getFullName());
                hook = hook.replace("%methodName", meth.getName());
                String params = generateParams(meth);
                if (!params.isEmpty()) params += ", ";
                hook = hook.replace("%params", params);
                StringSelection selection = new StringSelection(hook);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, hook + "\nCopied to clipboard!");
            }
            if (node instanceof JavaField) {
                JavaField meth = (JavaField) node;
                String hook;
                if (meth.getAccessFlags().isStatic())
                    hook = GET_STATIC_OBJECT.replace("%classObj", FIND_CLASS.replace("%className", jCls.getFullName()));
                else
                    hook = GET_OBJECT;
                hook = hook.replace("%fieldName", meth.getName());
                StringSelection selection = new StringSelection(hook);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, hook + "\nCopied to clipboard!");
            }
            if (node instanceof JavaClass) {
                JavaClass meth = (JavaClass) node;
                String hook = FIND_CLASS;
                hook = hook.replace("%className", meth.getFullName());
                StringSelection selection = new StringSelection(hook);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, hook + "\nCopied to clipboard!");
            }
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            node = null;
            Point pos = contentArea.getMousePosition();
            if (pos != null) {
                Token token = contentArea.viewToToken(pos);
                if (token != null) {
                    node = getJavaNodeAtOffset(jCls, contentArea, token.getOffset());
                }
            }
            if (node != null && (node instanceof JavaClass || node instanceof JavaField || node instanceof JavaMethod))
                setEnabled(true);
            else setEnabled(false);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    private class GenerateCallerAction extends AbstractAction implements PopupMenuListener {
        private static final long serialVersionUID = 4692546569977976385L;

        private final CodeArea contentArea;
        private final JClass jCls;

        private JavaNode node;

        public GenerateCallerAction(CodeArea contentArea, JClass jCls) {
            super("Generate Xposed setter/caller/constructor");
            this.contentArea = contentArea;
            this.jCls = jCls;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (node == null) {
                return;
            }
            MainWindow mainWindow = contentPanel.getTabbedPane().getMainWindow();
            if (node instanceof JavaMethod) {
                JavaMethod meth = (JavaMethod) node;
                String hook;
                if (meth.getName().equalsIgnoreCase("<init>"))
                    hook = NEW_INSTANCE.replace("%classObj", FIND_CLASS.replace("%className", jCls.getFullName()));
                else if (meth.getAccessFlags().isStatic())
                    hook = METHOD_STATIC_CALL.replace("%classObj", FIND_CLASS.replace("%className", jCls.getFullName()));
                else
                    hook = METHOD_CALL;
                hook = hook.replace("%className", meth.getDeclaringClass().getFullName());
                hook = hook.replace("%methodName", meth.getName());
                String params = generateParams(meth);
                hook = hook.replace("%params", params);
                StringSelection selection = new StringSelection(hook);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, hook + "\nCopied to clipboard!");
            }
            if (node instanceof JavaField) {
                JavaField meth = (JavaField) node;
                String hook;
                if (meth.getAccessFlags().isStatic())
                    hook = SET_STATIC_OBJECT.replace("%classObj", FIND_CLASS.replace("%className", jCls.getFullName()));
                else
                    hook = SET_OBJECT;
                hook = hook.replace("%fieldName", meth.getName());
                StringSelection selection = new StringSelection(hook);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, hook + "\nCopied to clipboard!");
            }
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            node = null;
            Point pos = contentArea.getMousePosition();
            if (pos != null) {
                Token token = contentArea.viewToToken(pos);
                if (token != null) {
                    node = getJavaNodeAtOffset(jCls, contentArea, token.getOffset());
                }
            }
            if (node != null && (node instanceof JavaField || node instanceof JavaMethod)) setEnabled(true);
            else setEnabled(false);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    private class GenerateJOBFString extends AbstractAction implements PopupMenuListener {
        private static final long serialVersionUID = 0L;

        private final CodeArea contentArea;
        private final JClass jCls;

        private JavaNode node;

        public GenerateJOBFString(CodeArea contentArea, JClass jCls) {
            super("Generate String For JOBF Override");
            this.contentArea = contentArea;
            this.jCls = jCls;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (node == null) {
                return;
            }
            MainWindow mainWindow = contentPanel.getTabbedPane().getMainWindow();
            String strOut = "Use the following in the .JOBF file: ";
            if (node instanceof JavaMethod) {
                JavaMethod meth = (JavaMethod) node;
                MethodInfo methInfo = meth.getMethodInfo();
                String strToAddToFile = "m " + methInfo.getFullId() + " = newName";
                StringSelection selection = new StringSelection(strToAddToFile);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, strOut + strToAddToFile + "\nCopied To Clipboard");
            }
            if (node instanceof JavaField) {
                JavaField fld = (JavaField) node;
                FieldInfo fldInfo = fld.getFieldInfo();
                String strToAddToFile = "f " + fldInfo.getFullId() + " = newName";
                StringSelection selection = new StringSelection(strToAddToFile);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, strOut + strToAddToFile + "\nCopied To Clipboard");
            }
            if (node instanceof JavaClass) {
                JavaClass cls = (JavaClass) node;
                ClassInfo clsInfo =  cls.getClassInfo();
                String strToAddToFile = "c " + clsInfo.getFullName() + " = newName";
                StringSelection selection = new StringSelection(strToAddToFile);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                JOptionPane.showMessageDialog(mainWindow, strOut + strToAddToFile + "\nCopied To Clipboard");
            }
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            node = null;
            Point pos = contentArea.getMousePosition();
            if (pos != null) {
                Token token = contentArea.viewToToken(pos);
                if (token != null) {
                    node = getJavaNodeAtOffset(jCls, contentArea, token.getOffset());
                }
            }
            if (node != null && (node instanceof JavaClass || node instanceof JavaField || node instanceof JavaMethod))
                setEnabled(true);
            else setEnabled(false);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }

    private class CodeLinkGenerator implements LinkGenerator, HyperlinkListener {
        private final JClass jCls;

        public CodeLinkGenerator(JClass cls) {
            this.jCls = cls;
        }

        @Override
        public LinkGeneratorResult isLinkAtOffset(RSyntaxTextArea textArea, int offset) {
            try {
                Token token = textArea.modelToToken(offset);
                if (token == null) {
                    return null;
                }
                final int sourceOffset = token.getOffset();
                final Position defPos = getDefPosition(jCls, textArea, sourceOffset);
                if (defPos == null) {
                    return null;
                }
                return new LinkGeneratorResult() {
                    @Override
                    public HyperlinkEvent execute() {
                        return new HyperlinkEvent(defPos, HyperlinkEvent.EventType.ACTIVATED, null,
                                defPos.getNode().makeLongString());
                    }

                    @Override
                    public int getSourceOffset() {
                        return sourceOffset;
                    }
                };
            } catch (Exception e) {
                LOG.error("isLinkAtOffset error", e);
                return null;
            }
        }

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            Object obj = e.getSource();
            if (obj instanceof Position) {
                contentPanel.getTabbedPane().codeJump((Position) obj);
            }
        }
    }
}
