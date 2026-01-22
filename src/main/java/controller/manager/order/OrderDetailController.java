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
    
    @FXML private Label lbTotal;        // Tổng tiền hàng (Gốc)
    @FXML private Label prDiscount;     // Giảm giá Voucher/Promotion
    @FXML private Label pDiscount;      // Giảm giá Điểm (Point)
    @FXML private Label lbActualTotal;  // Thực trả (Thêm mới nếu FXML có, hoặc dùng lbTotal tùy bạn)

    @FXML private TableView<OrderDetail> tbDetail;
    @FXML private TableColumn<OrderDetail, String> colProduct;
    @FXML private TableColumn<OrderDetail, Double> colPrice;
    @FXML private TableColumn<OrderDetail, Integer> colQuantity;
    @FXML private TableColumn<OrderDetail, Double> colSubTotal;

    private final OrderDetailDAO detailDAO = new OrderDetailDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
    }

    private void setupTableColumns() {
        // Hiển thị ProductName (TypeName)
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
        colSubTotal.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        formatCurrencyColumn(colPrice);
        formatCurrencyColumn(colSubTotal);
        colQuantity.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Đổ dữ liệu từ đơn hàng vào các nhãn hiển thị
     */
    public void setData(Order order) {
        // 1. Thông tin chung
        lbOrderID.setText("#" + order.getOrderID());
        lbDate.setText(order.getOrderDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lbCustomer.setText(order.getCustomerName() != null ? order.getCustomerName() : "Guest");
        lbEmployee.setText(order.getEmployeeName());
        
        // 2. Thông tin thanh toán chi tiết
        // lbTotal hiển thị tổng tiền hàng chưa trừ giảm giá
        lbTotal.setText(String.format("%,.0f", order.getTotalAmount()));
        
        // prDiscount hiển thị giảm giá khuyến mãi (Voucher)
        if (prDiscount != null) {
            prDiscount.setText(String.format("-%,.0f", order.getDiscountAmount()));
        }
        
        // pDiscount hiển thị giảm giá bằng điểm tích lũy
        if (pDiscount != null) {
            pDiscount.setText(String.format("-%,.0f", order.getPointDiscount()));
        }

        // Nếu bạn có label hiển thị con số cuối cùng khách phải trả
        if (lbActualTotal != null) {
            double actualPay = order.getTotalAmount() - order.getDiscountAmount() - order.getPointDiscount();
            lbActualTotal.setText(String.format("%,.0f", actualPay));
        }

        // 3. Tải danh sách sản phẩm chi tiết
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