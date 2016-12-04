package jadx.fxgui.ui;

import jadx.core.xmlgen.ResContainer;
import jadx.fxgui.treemodel.JNode;
import jadx.fxgui.treemodel.JResource;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Created by stirante
 */
public class DrawableView extends Tab {

    @FXML
    public StackPane content;
    private JNode node;

    public DrawableView(JNode node) {
        this.node = node;
        ResContainer content = ((JResource) node).getResFile().loadContent();
        Image fxImage = content.getFxImage();
        ImageView img = new ImageView(fxImage);
        FXMLLoader loader = new FXMLLoader(DrawableView.class.getResource("/Tab.fxml"));
        loader.setController(this);
        try {
            AnchorPane pane = loader.load();
            ScrollPane e = new ScrollPane(img);
            img.translateXProperty()
                    .bind(e.widthProperty().subtract(img.fitWidthProperty())
                            .divide(2D));

            img.translateYProperty()
                    .bind(e.heightProperty().subtract(img.fitHeightProperty())
                            .divide(2D));
            this.content.getChildren().add(e);
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

        DrawableView codeView = (DrawableView) o;

        return node != null && node.equals(codeView.node);

    }

    @Override
    public int hashCode() {
        return node != null ? node.hashCode() : 0;
    }
}
