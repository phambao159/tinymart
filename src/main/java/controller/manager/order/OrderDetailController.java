package controller.manager.order;

import dao.manager.order.OrderDetailDAO;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.manager.order.Order;
import model.manager.order.OrderDetail;

public class OrderDetailController implements Initializable {

    @FXML private Label lbOrderID;
    @FXML private Label lbDate;
    @FXML private Label lbCustomer;
    @FXML private Label lbEmployee;
    @FXML private Label lbTotal;
    @FXML private Label lbDiscount;

    @FXML private TableView<OrderDetail> tbDetail;
    @FXML private TableColumn<OrderDetail, String> colProduct;
    @FXML private TableColumn<OrderDetail, Double> colPrice;
    @FXML private TableColumn<OrderDetail, Integer> colQuantity;
    @FXML private TableColumn<OrderDetail, Double> colSubTotal;

    // Sử dụng OrderDetailDAO cung cấp quyền truy cập dữ liệu chi tiết hóa đơn
    private final OrderDetailDAO detailDAO = new OrderDetailDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
    }

    /**
     * Cấu hình các cột cho TableView chi tiết hóa đơn
     */
    private void setupTableColumns() {
        // Hiển thị kết hợp ProductName + TypeName (Ví dụ: Coca Cola (Size L))
        colProduct.setCellFactory(column -> new TableCell<OrderDetail, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    OrderDetail detail = getTableRow().getItem();
                    setText(detail.getProductName() + " (" + detail.getTypeName() + ")");
                }
            }
        });

        colPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        // Cột SubTotal sử dụng thuộc tính tự tính toán trong Model OrderDetail
        colSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        // Định dạng số và căn lề
        formatCurrencyColumn(colPrice);
        formatCurrencyColumn(colSubTotal);
        colQuantity.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Phương thức chính để đổ dữ liệu từ trang danh sách Order sang trang chi tiết
     * @param order Đối tượng Order được chọn từ TableView chính
     */
    public void setData(Order order) {
        // 1. Gán dữ liệu Header từ object Order
        lbOrderID.setText("#" + order.getOrderID());
        lbDate.setText(order.getOrderDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lbCustomer.setText(order.getCustomerName());
        lbEmployee.setText(order.getEmployeeName());
        
        // Hiển thị Tổng tiền và Giảm giá (giữ từ bản HEAD)
        lbTotal.setText(String.format("%,.0f", order.getTotalAmount()));
        if (lbDiscount != null) {
            lbDiscount.setText(String.format("-%,.0f", order.getDiscountAmount()));
        }

        // 2. Gọi OrderDetailDAO để lấy danh sách các mặt hàng trong hóa đơn
        List<OrderDetail> details = detailDAO.getDetailsByOrderId(order.getOrderID());
        tbDetail.setItems(FXCollections.observableArrayList(details));
    }

    /**
     * Định dạng cột tiền tệ: Căn phải, thêm dấu phân cách hàng nghìn
     */
    private void formatCurrencyColumn(TableColumn<OrderDetail, Double> column) {
        column.setCellFactory(tc -> new TableCell<OrderDetail, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", price)); 
                }
            }
        });
        column.setStyle("-fx-alignment: CENTER-RIGHT; -fx-padding: 0 10 0 0;");
    }
}