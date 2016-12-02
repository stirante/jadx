package jadx.fxgui.ui.syntax;

import org.fxmisc.richtext.CodeArea;

/**
 * Created by stirante
 */
public abstract class BaseSyntax {
    public static BaseSyntax getFor(String str) {
        switch (str.toLowerCase()) {
            case "java":
                return JavaSyntax.instance;
            case "xml":
            case "manifest":
            case "html":
                return XmlSyntax.instance;
            default:
                return DefaultSyntax.instance;
        }
    }

    public abstract void computeHighlighting(CodeArea text);

}
