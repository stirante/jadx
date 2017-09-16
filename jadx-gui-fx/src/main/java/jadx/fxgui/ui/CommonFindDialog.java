package jadx.fxgui.ui;

import com.sun.javafx.collections.ObservableListWrapper;
import jadx.fxgui.treemodel.CodeNode;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public abstract class CommonFindDialog {

    private ObservableList<CodeNode> result = new ObservableListWrapper<>(new ArrayList<>());

    public void show() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.setWidth(600);
        dialog.setTitle(getTitle());
        dialog.setHeaderText(null);
        dialog.getDialogPane().getStylesheets().add(CommonFindDialog.class.getResource("/style.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        TableView<CodeNode> table = new TableView<>(result);
        table.setRowFactory(tv -> {
            TableRow<CodeNode> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    onRowClicked(row.getItem());
                }
            });
            return row;
        });
        TableColumn<CodeNode, String> classCol = new TableColumn<>("Class");
        classCol.setCellValueFactory(param -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return param.getValue().getRootClass().getName() + ":" + param.getValue().getLine();
            }
        });
        TableColumn<CodeNode, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(param -> new ObservableValueBase<String>() {
            @Override
            public String getValue() {
                return param.getValue().getLineString();
            }
        });
        table.getColumns().addAll(classCol, codeCol);
        VBox root = new VBox(getNode(), table);
        dialog.getDialogPane().setContent(root);
        dialog.show();
    }

    protected abstract String getTitle();

    protected abstract Node getNode();

    public ObservableList<CodeNode> getResult() {
        return result;
    }

    protected abstract void onRowClicked(CodeNode node);

}
