package com.cosplay.dao;

import com.cosplay.model.Rental;
import com.cosplay.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {

    // Check availability using the given connection to keep checks + insert in single transaction if needed.
    private boolean isAvailable(Connection conn, int CosplayId, LocalDate start, LocalDate end) throws SQLException {
        // Overlap if NOT (existing.end < new.start OR existing.start > new.end)
        String sql = "SELECT 1 FROM rentals WHERE cosplay_id = ? AND status IN ('Pending','Confirmed','Rented') " +
                     "AND NOT (end_date < ? OR start_date > ?) LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, CosplayId);
            ps.setString(2, start.toString());
            ps.setString(3, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next(); // if result exists -> overlap -> not available
            }
        }
    }

    // Public wrapper
    public boolean isAvailable(int CosplayId, LocalDate start, LocalDate end) {
        try (Connection conn = Database.connect()) {
            return isAvailable(conn, CosplayId, start, end);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Create a rental safely: check availability inside a transaction and insert
    public boolean createRental(Rental r) {
        String insert = "INSERT INTO rentals(cosplay_id, customer_name, contact_number, address, facebook_link, start_date, end_date, rent_days, customer_addons, payment_method, proof_of_payment, selfie_photo, id_photo, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.connect()) {
            conn.setAutoCommit(false);
            // check availability with the same connection
            if (!isAvailable(conn, r.getCosplayId(), r.getStartDate(), r.getEndDate())) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, r.getCosplayId());
                ps.setString(2, r.getCustomerName());
                ps.setString(3, r.getContactNumber());
                ps.setString(4, r.getAddress());
                ps.setString(5, r.getFacebookLink());
                ps.setString(6, r.getStartDate().toString());
                ps.setString(7, r.getEndDate().toString());
                ps.setInt(8, r.getRentDays());
                ps.setString(9, r.getCustomerAddOns());
                ps.setString(10, r.getPaymentMethod());
                ps.setString(11, r.getProofOfPayment());
                ps.setString(12, r.getSelfiePhoto());
                ps.setString(13, r.getIdPhoto());
                ps.setString(14, r.getStatus() == null ? "Pending" : r.getStatus());
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
                r.setCosplayId(rs.getInt("cosplay_id"));
                r.setCustomerName(rs.getString("customer_name"));
                r.setContactNumber(rs.getString("contact_number"));
                r.setAddress(rs.getString("address"));
                r.setFacebookLink(rs.getString("facebook_link"));
                r.setStartDate(LocalDate.parse(rs.getString("start_date")));
                r.setEndDate(LocalDate.parse(rs.getString("end_date")));
                r.setRentDays(rs.getInt("rent_days"));
                r.setCustomerAddOns(rs.getString("customer_addons"));
                r.setPaymentMethod(rs.getString("payment_method"));
                r.setProofOfPayment(rs.getString("proof_of_payment"));
                r.setSelfiePhoto(rs.getString("selfie_photo"));
                r.setIdPhoto(rs.getString("id_photo"));
                r.setStatus(rs.getString("status"));
                list.add(r);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    
    public boolean updateRentalStatus(int rentalId, String newStatus) {
        String sql = "UPDATE rentals SET status = ? WHERE rental_id = ?";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, rentalId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Rental> getRentalsByCosplayId(int cosplayId) {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE cosplay_id = ? AND status IN ('Pending','Confirmed','Rented') ORDER BY start_date";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cosplayId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Rental r = new Rental();
                    r.setId(rs.getInt("rental_id"));
                    r.setCosplayId(rs.getInt("cosplay_id"));
                    r.setCustomerName(rs.getString("customer_name"));
                    r.setContactNumber(rs.getString("contact_number"));
                    r.setAddress(rs.getString("address"));
                    r.setFacebookLink(rs.getString("facebook_link"));
                    r.setStartDate(LocalDate.parse(rs.getString("start_date")));
                    r.setEndDate(LocalDate.parse(rs.getString("end_date")));
                    r.setRentDays(rs.getInt("rent_days"));
                    r.setCustomerAddOns(rs.getString("customer_addons"));
                    r.setPaymentMethod(rs.getString("payment_method"));
                    r.setProofOfPayment(rs.getString("proof_of_payment"));
                    r.setSelfiePhoto(rs.getString("selfie_photo"));
                    r.setIdPhoto(rs.getString("id_photo"));
                    r.setStatus(rs.getString("status"));
                    list.add(r);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}


