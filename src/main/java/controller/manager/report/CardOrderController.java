package controller.manager.report;

import dao.manager.order.OrderDetailDAO;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import model.manager.order.Order;
import model.manager.order.OrderDetail;

public class CardOrderController implements Initializable {

    @FXML
    private Label lblProductNames;
    @FXML
    private Label lblDateTime;
    @FXML
    private Label lblPaymentMethod;
    @FXML
    private Label lblTotalAmount;

    private final OrderDetailDAO detailDAO = new OrderDetailDAO();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Khởi tạo các giá trị mặc định nếu cần
    }

    /**
     * Đổ dữ liệu từ Object Order vào các Label của Card
     */
    public void setData(Order order) {
        // 1. Đổ dữ liệu cơ bản
        lblDateTime.setText(order.getOrderDateTime().format(formatter));
        lblPaymentMethod.setText(order.getPaymentMethod());
        lblTotalAmount.setText(String.format("$%.2f", order.getTotalAmount()));

        // 2. Lấy danh sách chi tiết từ OrderDetailDAO
        List<OrderDetail> details = detailDAO.getDetailsByOrderId(order.getOrderID());

        if (details != null && !details.isEmpty()) {
            // Nối Tên + Loại (Ví dụ: "Cafe (S), Bánh mì (L)")
            String names = details.stream()
                    .map(d -> d.getProductName() + " (" + d.getTypeName() + ")")
                    .distinct()
                    .collect(Collectors.joining(", "));

            lblProductNames.setText(names);
        } else {
            lblProductNames.setText("No products");
        }
    }
}
