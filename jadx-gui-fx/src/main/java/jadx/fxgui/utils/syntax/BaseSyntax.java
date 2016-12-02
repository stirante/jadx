package jadx.fxgui.utils.syntax;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

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

    public abstract StyleSpans<Collection<String>> computeHighlighting(String text);

}
