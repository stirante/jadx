package jadx.fxgui.utils.syntax;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by stirante
 */
public class DefaultSyntax extends BaseSyntax {

    static final BaseSyntax instance = new DefaultSyntax();

    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        return new StyleSpansBuilder<Collection<String>>().add(Collections.emptyList(), text.length()).create();
    }
}
