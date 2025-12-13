package com.cosplay;

import com.cosplay.dao.CosplayDAO;
import com.cosplay.dao.RentalDAO;
import com.cosplay.model.Cosplay;
import com.cosplay.model.Rental;
import com.cosplay.util.Database;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class AppMain {
    public static void main(String[] args) {
        // Init DB & tables
        Database.init();

        CosplayDAO CosplayDAO = new CosplayDAO();
        RentalDAO rentalDAO = new RentalDAO();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Cosplay Rental Backend ---");
            System.out.println("1) Add Cosplay");
            System.out.println("2) List Cosplays");
            System.out.println("3) Check availability");
            System.out.println("4) Create rental");
            System.out.println("5) List rentals");
            System.out.println("0) Exit");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> {
                        System.out.print("Name: "); String name = sc.nextLine();
                        System.out.print("Category: "); String cat = sc.nextLine();
                        System.out.print("Size: "); String size = sc.nextLine();
                        System.out.print("Description: "); String desc = sc.nextLine();
                        System.out.print("Image path: "); String img = sc.nextLine();
                        Cosplay c = new Cosplay(name, cat, size, desc, img);
                        CosplayDAO.addCosplay(c);
                        System.out.println("Added: " + c);
                    }
                    case "2" -> {
                        List<Cosplay> all = CosplayDAO.getAll();
                        if (all.isEmpty()) System.out.println("No Cosplays.");
                        else all.forEach(System.out::println);
                    }
                    case "3" -> {
                        System.out.print("Cosplay ID: "); int id = Integer.parseInt(sc.nextLine());
                        System.out.print("Start (yyyy-MM-dd): "); LocalDate s = LocalDate.parse(sc.nextLine());
                        System.out.print("End (yyyy-MM-dd): "); LocalDate e = LocalDate.parse(sc.nextLine());
                        boolean ok = rentalDAO.isAvailable(id, s, e);
                        System.out.println(ok ? "Available" : "Not available");
                    }
                    case "4" -> {
                        Rental r = new Rental();
                        System.out.print("Cosplay ID: "); r.setCosplayId(Integer.parseInt(sc.nextLine()));
                        System.out.print("Your name: "); r.setCustomerName(sc.nextLine());
                        System.out.print("Contact number: "); r.setContactNumber(sc.nextLine());
                        System.out.print("Address: "); r.setAddress(sc.nextLine());
                        System.out.print("Facebook link: "); r.setFacebookLink(sc.nextLine());
                        System.out.print("Start (yyyy-MM-dd): "); r.setStartDate(LocalDate.parse(sc.nextLine()));
                        System.out.print("End (yyyy-MM-dd): "); r.setEndDate(LocalDate.parse(sc.nextLine()));
                        System.out.print("Payment method: "); r.setPaymentMethod(sc.nextLine());
                        r.setProofOfPayment(""); r.setStatus("Pending");
                        boolean success = rentalDAO.createRental(r);
                        System.out.println(success ? "Rental created: " + r : "Failed: Cosplay not available for those dates.");
                    }
                    case "5" -> {
                        List<com.cosplay.model.Rental> all = rentalDAO.getAllRentals();
                        all.forEach(System.out::println);
                    }
                    case "0" -> { System.out.println("Bye"); sc.close(); return; }
                    default -> System.out.println("Invalid choice");
                }
            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}

