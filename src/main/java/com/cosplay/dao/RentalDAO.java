package com.cosplay.dao;

import com.cosplay.model.Rental;
import com.cosplay.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {

    // Check availability using the given connection to keep checks + insert in single transaction if needed.
    private boolean isAvailable(Connection conn, int costumeId, LocalDate start, LocalDate end) throws SQLException {
        // Overlap if NOT (existing.end < new.start OR existing.start > new.end)
        String sql = "SELECT 1 FROM rentals WHERE costume_id = ? AND status IN ('Pending','Confirmed','Rented') " +
                     "AND NOT (end_date < ? OR start_date > ?) LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, costumeId);
            ps.setString(2, start.toString());
            ps.setString(3, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next(); // if result exists -> overlap -> not available
            }
        }
    }

    // Public wrapper
    public boolean isAvailable(int costumeId, LocalDate start, LocalDate end) {
        try (Connection conn = Database.connect()) {
            return isAvailable(conn, costumeId, start, end);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Create a rental safely: check availability inside a transaction and insert
    public boolean createRental(Rental r) {
        String insert = "INSERT INTO rentals(costume_id, customer_name, contact_number, address, facebook_link, start_date, end_date, payment_method, proof_of_payment, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false);
            // check availability with the same connection
            if (!isAvailable(conn, r.getCostumeId(), r.getStartDate(), r.getEndDate())) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, r.getCostumeId());
                ps.setString(2, r.getCustomerName());
                ps.setString(3, r.getContactNumber());
                ps.setString(4, r.getAddress());
                ps.setString(5, r.getFacebookLink());
                ps.setString(6, r.getStartDate().toString());
                ps.setString(7, r.getEndDate().toString());
                ps.setString(8, r.getPaymentMethod());
                ps.setString(9, r.getProofOfPayment());
                ps.setString(10, r.getStatus() == null ? "Pending" : r.getStatus());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) r.setId(rs.getInt(1));
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Rental> getAllRentals() {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT * FROM rentals ORDER BY start_date";
        try (Connection conn = Database.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Rental r = new Rental();
                r.setId(rs.getInt("rental_id"));
                r.setCostumeId(rs.getInt("costume_id"));
                r.setCustomerName(rs.getString("customer_name"));
                r.setContactNumber(rs.getString("contact_number"));
                r.setAddress(rs.getString("address"));
                r.setFacebookLink(rs.getString("facebook_link"));
                r.setStartDate(LocalDate.parse(rs.getString("start_date")));
                r.setEndDate(LocalDate.parse(rs.getString("end_date")));
                r.setPaymentMethod(rs.getString("payment_method"));
                r.setProofOfPayment(rs.getString("proof_of_payment"));
                r.setStatus(rs.getString("status"));
                list.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}
