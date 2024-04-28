package Jdbc1;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    // CREATE DATABASE mydb;
    static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/mydb?serverTimezone=Europe/Kiev";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "27051995m";

    static Connection conn;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            try {
                // create connection
                conn = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
                initDB();

                while (true) {
                    System.out.println("1: add apartment");
                    System.out.println("2: add random apartments");
                    System.out.println("3: delete apartment");
                    System.out.println("4: change apartment");
                    System.out.println("5: view apartments");
                    System.out.println("6: select apartment by district name");
                    System.out.println("7: view apartment by price");
                    System.out.println("8: view apartment by district and price");
                    System.out.print("-> ");

                    String s = sc.nextLine();
                    switch (s) {
                        case "1":
                            addApartment(sc);
                            break;
                        case "2":
                            insertRandomApartments(sc);
                            break;
                        case "3":
                            deleteApartment(sc);
                            break;
                        case "4":
                            changeApartment(sc);
                            break;
                        case "5":
                            viewApartments();
                            break;
                        case "6":
                            selectApartmentsByDistrict(sc);
                            break;
                        case "7":
                            selectApartmentsByPrice(sc);
                            break;
                        case "8":
                            selectApartmentsByDistrictAndPrice(sc);
                            break;
                        default:
                            return;
                    }
                }
            } finally {
                sc.close();
                if (conn != null) conn.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
    }

    private static void initDB() throws SQLException {
        Statement st = conn.createStatement();
        try {
            st.execute("DROP TABLE IF EXISTS Apartments");
            st.execute("CREATE TABLE Apartments (id INT NOT NULL " +
                    "AUTO_INCREMENT PRIMARY KEY, " +
                    "district VARCHAR(255) NOT NULL, " +
                    "address VARCHAR(255) NOT NULL, " +
                    "area FLOAT NOT NULL, " +
                    "rooms INT NOT NULL, " +
                    "price FLOAT NOT NULL)");
        } finally {
            st.close();
        }

        /*
        try (Statement st1 = conn.createStatement()) {
            st1.execute("DROP TABLE IF EXISTS Clients");
            st1.execute("CREATE TABLE Clients (id INT NOT NULL " +
                    "AUTO_INCREMENT PRIMARY KEY, name VARCHAR(20) " +
                    "NOT NULL, age INT)");
        }
         */
    }

    private static void addApartment(Scanner sc) throws SQLException {
        System.out.print("Enter apartment district: ");
        String district = sc.nextLine();
        System.out.print("Enter apartment address: ");
        String address = sc.nextLine();
        System.out.print("Enter apartment area: ");
        String sArea = sc.nextLine();
        float area = Float.parseFloat(sArea);
        System.out.print("Enter apartment rooms: ");
        String sRooms = sc.nextLine();
        int rooms = Integer.parseInt(sRooms);
        System.out.print("Enter apartment price: ");
        String sPrice = sc.nextLine();
        float price = Float.parseFloat(sPrice);

        //String sql = "INSERT INTO Clients (name, age) " +
          //      "VALUES(" + name + ", " + age + ")";

        PreparedStatement ps = conn.prepareStatement("INSERT INTO Apartments (district, address, area, rooms, price) VALUES(?, ?, ?, ?, ?)");
        try {
            ps.setString(1, district);
            ps.setString(2, address);
            ps.setFloat(3, area);
            ps.setInt(4, rooms);
            ps.setFloat(5, price);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE

        } finally {
            ps.close();
        }
    }

    private static void deleteApartment(Scanner sc) throws SQLException {
        System.out.print("Enter apartment address: ");
        String address = sc.nextLine();

        PreparedStatement ps = conn.prepareStatement("DELETE FROM Apartments WHERE address = ?");
        try {
            ps.setString(1, address);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        } finally {
            ps.close();
        }
    }

    private static void changeApartment(Scanner sc) throws SQLException {
        System.out.print("Enter apartment address: ");
        String address = sc.nextLine();
        System.out.print("Enter new price: ");
        String sPrice = sc.nextLine();
        float price = Float.parseFloat(sPrice);

        PreparedStatement ps = conn.prepareStatement("UPDATE Apartments SET price = ? WHERE address = ?");
        try {
            ps.setFloat(1, price);
            ps.setString(2, address);
            ps.executeUpdate(); // for INSERT, UPDATE & DELETE
        } finally {
            ps.close();
        }
    }

    private static void insertRandomApartments(Scanner sc) throws SQLException {
        System.out.print("Enter apartments count: ");
        String sCount = sc.nextLine();
        int count = Integer.parseInt(sCount);
        Random rnd = new Random();

        conn.setAutoCommit(false); // enable transactions
        try {
            try {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO Apartments (district, address, area, rooms, price) VALUES(?, ?, ?, ?, ?)");
                try {
                    for (int i = 0; i < count; i++) {
                        ps.setString(1, "District" + i);
                        ps.setString(2, "Street" + i + ", Building " + (i+1) + ", Apartment "+(100+i));
                        ps.setFloat(3, rnd.nextFloat(300));
                        ps.setInt(4, rnd.nextInt(10));
                        ps.setFloat(5, rnd.nextFloat(1000000)+10000);
                        ps.executeUpdate();
                    }
                    conn.commit();
                } finally {
                    ps.close();
                }
            } catch (Exception ex) {
                conn.rollback();
            }
        } finally {
            conn.setAutoCommit(true); // return to default mode
        }
    }



    private static void viewApartments() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Apartments")) {
            try (ResultSet rs = ps.executeQuery()) {
                // can be used to get information about the types and properties of the columns in a ResultSet object
                ResultSetMetaData md = rs.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.print(md.getColumnName(i) + "\t\t");
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.print(rs.getString(i) + "\t\t");
                    }
                    System.out.println();
                }
            }
        }
    }

    private static void selectApartmentsByDistrict(Scanner sc) throws SQLException {
        System.out.print("Enter apartment district: ");
        String district = sc.nextLine();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Apartments WHERE district = ?")) {
            ps.setString(1, district);
            try (ResultSet rs = ps.executeQuery()) {
                // can be used to get information about the types and properties of the columns in a ResultSet object
                ResultSetMetaData md = rs.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.print(md.getColumnName(i) + "\t\t");
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.print(rs.getString(i) + "\t\t");
                    }
                    System.out.println();
                }
            }
        }
    }

    private static void selectApartmentsByPrice(Scanner sc) throws SQLException {
        System.out.print("Enter apartment price: ");
        String sPrice = sc.nextLine();
        float price = Float.parseFloat(sPrice);
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Apartments WHERE price = ?")) {
            ps.setFloat(1, price);
            try (ResultSet rs = ps.executeQuery()) {
                // can be used to get information about the types and properties of the columns in a ResultSet object
                ResultSetMetaData md = rs.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.print(md.getColumnName(i) + "\t\t");
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.print(rs.getString(i) + "\t\t");
                    }
                    System.out.println();
                }
            }
        }
    }

    private static void selectApartmentsByDistrictAndPrice(Scanner sc) throws SQLException {
        System.out.print("Enter apartment district: ");
        String district = sc.nextLine();
        System.out.print("Enter apartment price: ");
        String sPrice = sc.nextLine();
        float price = Float.parseFloat(sPrice);
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM Apartments WHERE district = ? AND price = ?")) {
            ps.setString(1, district);
            ps.setFloat(2, price);
            try (ResultSet rs = ps.executeQuery()) {
                // can be used to get information about the types and properties of the columns in a ResultSet object
                ResultSetMetaData md = rs.getMetaData();

                for (int i = 1; i <= md.getColumnCount(); i++)
                    System.out.print(md.getColumnName(i) + "\t\t");
                System.out.println();

                while (rs.next()) {
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        System.out.print(rs.getString(i) + "\t\t");
                    }
                    System.out.println();
                }
            }
        }
    }
}


