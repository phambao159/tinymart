package controller.manager;

import dao.manager.ShiftDAO;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import model.manager.Shift;

/**
 * FXML Controller class
 */
public class ShiftController implements Initializable {

    @FXML
    private TextField txtSearch;
    
    // Lưu ý: Tên TableView trong FXML của bạn đang là tbPromotion, 
    // bạn nên đổi lại thành tbShift cho đồng nhất.
    @FXML
    private TableView<Shift> tbShift; 
    
    @FXML
    private TableColumn<Shift, Integer> colID; // Đổi String thành Integer cho khớp với Model
    
    @FXML
    private TableColumn<Shift, String> colName;
    
    @FXML
    private TableColumn<Shift, LocalTime> colStart;
    
    @FXML
    private TableColumn<Shift, LocalTime> colEnd;

    private final ShiftDAO shiftDAO = new ShiftDAO();
    private final ObservableList<Shift> shiftList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadShiftData();
    }

    /**
     * Thiết lập cách hiển thị dữ liệu cho các cột trong bảng
     */
    private void setupColumns() {
        // Các chuỗi ký tự trong PropertyValueFactory phải khớp chính xác với 
        // tên biến (field) khai báo trong class model.manager.Shift
        colID.setCellValueFactory(new PropertyValueFactory<>("shiftID"));
        colName.setCellValueFactory(new PropertyValueFactory<>("shiftName"));
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
    }

    /**
     * Tải toàn bộ danh sách ca trực từ Database lên TableView
     */
    private void loadShiftData() {
        shiftList.clear();
        shiftList.addAll(shiftDAO.getAllShifts());
        tbShift.setItems(shiftList);
    }

    @FXML
    private void onSearch(ActionEvent event) {

    }

    @FXML
    private void onAdd(ActionEvent event) {

    }
}