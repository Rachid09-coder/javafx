package com.edusmart.dao.jdbc;

import com.edusmart.dao.OrderDao;
import com.edusmart.model.Order;
import com.edusmart.model.OrderItem;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

public class JdbcOrderDao implements OrderDao {

    @Override
    public int createOrderWithItems(Order order, List<OrderItem> items) {
        if (order == null) throw new IllegalArgumentException("order is required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("items is required");

        String insertOrder = """
            INSERT INTO orders
              (student_id, promo_code_id, total_before_discount, discount_amount, total_after_discount, status, stripe_session_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        String insertItem = """
            INSERT INTO order_items
              (order_id, product_id, product_name, quantity, unit_price, total_price)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection c = DbConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps = c.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, order.getStudentId());
                    if (order.getPromoCodeId() != null) ps.setInt(2, order.getPromoCodeId());
                    else ps.setNull(2, Types.INTEGER);
                    ps.setDouble(3, order.getTotalBeforeDiscount());
                    ps.setDouble(4, order.getDiscountAmount());
                    ps.setDouble(5, order.getTotalAfterDiscount());
                    ps.setString(6, order.getStatus() == null ? "PENDING" : order.getStatus());
                    if (order.getStripeSessionId() != null && !order.getStripeSessionId().isBlank()) ps.setString(7, order.getStripeSessionId());
                    else ps.setNull(7, Types.VARCHAR);

                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("No order id generated");
                        orderId = keys.getInt(1);
                    }
                }

                try (PreparedStatement psItem = c.prepareStatement(insertItem)) {
                    for (OrderItem it : items) {
                        psItem.setInt(1, orderId);
                        psItem.setInt(2, it.getProductId());
                        psItem.setString(3, it.getProductName());
                        psItem.setInt(4, it.getQuantity());
                        psItem.setDouble(5, it.getUnitPrice());
                        psItem.setDouble(6, it.getTotalPrice());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                c.commit();
                return orderId;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create order", ex);
        }
    }

    @Override
    public void updateStripeSessionId(int orderId, String stripeSessionId) {
        String sql = "UPDATE orders SET stripe_session_id = ? WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (stripeSessionId != null && !stripeSessionId.isBlank()) ps.setString(1, stripeSessionId.trim());
            else ps.setNull(1, Types.VARCHAR);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update stripe session id", ex);
        }
    }
}

