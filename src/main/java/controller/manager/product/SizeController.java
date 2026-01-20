package controller.manager.product;

import dao.manager.product.SizeDAO;
import model.manager.product.Size;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SizeController implements Initializable {

    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Size> tbSize;

    @FXML
    private TableColumn<Size, Integer> colID;
    @FXML
    private TableColumn<Size, String> colType;

    private ObservableList<Size> sizeData = FXCollections.observableArrayList();
    private final SizeDAO sizeDAO = new SizeDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        loadData();

        // Xử lý Double Click vào dòng trong bảng để sửa
        tbSize.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2 && tbSize.getSelectionModel().getSelectedItem() != null) {
                openEditForm(tbSize.getSelectionModel().getSelectedItem());
            }
        });
    }

    private void setupTableColumns() {
        // Lưu ý: "sizeID" và "type" phải khớp với tên thuộc tính trong class model Size
        colID.setCellValueFactory(new PropertyValueFactory<>("sizeID"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
    }

    private void loadData() {
        sizeData.clear();
        List<Size> dataFromDB = sizeDAO.getData();
        sizeData.addAll(dataFromDB);
        tbSize.setItems(sizeData);
    }

    @FXML
    private void onSearch(ActionEvent event) {
        String keyword = txtSearch.getText().trim();
        sizeData.clear();

        // Giả sử SizeDAO có hàm searchByType hoặc tương tự
        List<Size> result = keyword.isEmpty() 
                ? sizeDAO.getData() 
                : sizeDAO.searchByType(keyword); 

        sizeData.addAll(result);
        tbSize.setItems(sizeData);
    }

    @FXML
    private void onAdd(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/addSize.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Size");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadData()); // Refresh sau khi đóng
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi load addSize.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onEdit(ActionEvent event) {
        Size selected = tbSize.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditForm(selected);
        } else {
            showAlert("Thông báo", "Vui lòng chọn một kích thước để sửa!");
        }
    }

    private void openEditForm(Size selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manager/product/EditSize.fxml"));
            Parent root = loader.load();

            // Giả sử bạn có class EditSizeController
            EditSizeController controller = loader.getController();
            controller.initData(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Size: " + selected.getType());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHiding(e -> loadData()); // Tự động load lại bảng khi đóng form
            stage.show();
        } catch (IOException e) {
            System.err.println("Lỗi load EditSize.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}