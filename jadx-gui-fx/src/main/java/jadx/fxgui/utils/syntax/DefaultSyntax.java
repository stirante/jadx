package jadx.fxgui.utils.syntax;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by stirante
 */
public class DefaultSyntax extends BaseSyntax {

    static final BaseSyntax instance = new DefaultSyntax();

    @Override
    public void computeHighlighting(CodeArea text) {
        text.setStyleSpans(0, new StyleSpansBuilder<Collection<String>>().add(Collections.emptyList(), text.getText().length()).create());
    }
}
