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

    @FXML private TableView<OrderDetail> tbDetail;
    @FXML private TableColumn<OrderDetail, String> colProduct;
    @FXML private TableColumn<OrderDetail, Double> colPrice; 
    @FXML private TableColumn<OrderDetail, Integer> colQuantity;
    @FXML private TableColumn<OrderDetail, Double> colSubTotal;

    // Hai nhãn giảm giá bạn đã khai báo
    @FXML private Label prDiscount; // Promotion Discount (Giảm giá tiền mặt/khuyến mãi)
    @FXML private Label pDiscount;  // Point Discount (Giảm giá bằng điểm)

    private final OrderDetailDAO detailDAO = new OrderDetailDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
    }

    private void setupTableColumns() {
        // 1. Cột sản phẩm
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

        // 2. Cột Đơn giá (sellingPrice)
        colPrice.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));

        // 3. Cột Số lượng
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        // 4. Cột Thành tiền (subTotal)
        colSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        formatCurrencyColumn(colPrice);
        formatCurrencyColumn(colSubTotal);
        colQuantity.setStyle("-fx-alignment: CENTER;");
    }

    public void setData(Order order) {
        // Gán dữ liệu Header
        lbOrderID.setText("#" + order.getOrderID());
        
        if (order.getOrderDateTime() != null) {
            lbDate.setText(order.getOrderDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        
        lbCustomer.setText(order.getCustomerName() != null ? order.getCustomerName() : "Guest");
        lbEmployee.setText(order.getEmployeeName());
        
        // Hiển thị tổng tiền
        lbTotal.setText(String.format("%,.0f VNĐ", order.getTotalAmount()));

        // SỬA LỖI TẠI ĐÂY: Sử dụng đúng tên biến đã khai báo @FXML
        if (prDiscount != null) {
            prDiscount.setText(String.format("-%,.0f ", order.getDiscountAmount()));
        }
        
        if (pDiscount != null) {
            pDiscount.setText(String.format("-%,.0f ", order.getPointDiscount()));
        }

        // Lấy danh sách chi tiết
        List<OrderDetail> details = detailDAO.getDetailsByOrderId(order.getOrderID());
        tbDetail.setItems(FXCollections.observableArrayList(details));
    }

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