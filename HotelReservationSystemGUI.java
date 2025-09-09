import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

// Room class
class Room {
    String roomNumber;
    String category;
    boolean isBooked;

    public Room(String roomNumber, String category, boolean isBooked) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.isBooked = isBooked;
    }

    @Override
    public String toString() {
        return roomNumber + " | " + category + " | " + (isBooked ? "Booked" : "Available");
    }

    public String toFileString() {
        return roomNumber + "," + category + "," + isBooked;
    }
}

// Reservation class
class Reservation {
    String roomNumber;
    String customerName;
    String category;
    String paymentStatus;

    public Reservation(String roomNumber, String customerName, String category, String paymentStatus) {
        this.roomNumber = roomNumber;
        this.customerName = customerName;
        this.category = category;
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "Room: " + roomNumber + " | Name: " + customerName + " | Category: " + category + " | Payment: " + paymentStatus;
    }

    public String toFileString() {
        return roomNumber + "," + customerName + "," + category + "," + paymentStatus;
    }
}

// Main System class
public class HotelReservationSystemGUI extends JFrame {
    private java.util.List<Room> rooms = new ArrayList<>();
    private java.util.List<Reservation> reservations = new ArrayList<>();
    private final String roomsFile = "rooms.txt";
    private final String reservationsFile = "reservations.txt";

    private JTextArea displayArea = new JTextArea(20, 50);
    private JComboBox<String> roomDropdown = new JComboBox<>();
    private JTextField nameField = new JTextField(20);

    public HotelReservationSystemGUI() {
        setTitle("🏨 Hotel Reservation System");
        setSize(900, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(5, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton loadRoomsBtn = new JButton("Load Rooms");
        JButton searchRoomsBtn = new JButton("Search Available Rooms");
        JButton bookBtn = new JButton("Book Room");
        JButton cancelBtn = new JButton("Cancel Booking");
        JButton viewReservationsBtn = new JButton("View Reservations");
        JButton saveDataBtn = new JButton("Save Data");

        controlPanel.add(new JLabel("Select Room:"));
        controlPanel.add(roomDropdown);

        controlPanel.add(new JLabel("Customer Name:"));
        controlPanel.add(nameField);

        controlPanel.add(loadRoomsBtn);
        controlPanel.add(searchRoomsBtn);
        controlPanel.add(bookBtn);
        controlPanel.add(cancelBtn);
        controlPanel.add(viewReservationsBtn);
        controlPanel.add(saveDataBtn);

        add(controlPanel, BorderLayout.NORTH);

        loadRoomsBtn.addActionListener(e -> loadRooms());
        searchRoomsBtn.addActionListener(e -> searchRooms());
        bookBtn.addActionListener(e -> bookRoom());
        cancelBtn.addActionListener(e -> cancelBooking());
        viewReservationsBtn.addActionListener(e -> viewReservations());
        saveDataBtn.addActionListener(e -> saveData());

        loadRooms();  // Load data automatically
    }

    private void loadRooms() {
        rooms.clear();
        roomDropdown.removeAllItems();
        File file = new File(roomsFile);

        try {
            if (!file.exists()) {
                String[] categories = {"Standard", "Deluxe", "Suite"};
                for (int i = 1; i <= 10; i++) {
                    rooms.add(new Room("R" + i, categories[i % 3], false));
                }
                saveRoomsToFile();
            } else {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    rooms.add(new Room(parts[0], parts[1], Boolean.parseBoolean(parts[2])));
                }
                reader.close();
            }

            for (Room room : rooms) {
                roomDropdown.addItem(room.roomNumber);
            }

            displayArea.setText("Rooms loaded successfully.\n");
        } catch (IOException ex) {
            displayArea.setText("Error loading rooms: " + ex.getMessage());
        }
    }

    private void saveRoomsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(roomsFile))) {
            for (Room room : rooms) {
                writer.println(room.toFileString());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving rooms.");
        }
    }

    private void saveReservationsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(reservationsFile))) {
            for (Reservation res : reservations) {
                writer.println(res.toFileString());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving reservations.");
        }
    }

    private void saveData() {
        saveRoomsToFile();
        saveReservationsToFile();
        JOptionPane.showMessageDialog(this, "Data saved successfully.");
    }

    private void searchRooms() {
        StringBuilder sb = new StringBuilder("Available Rooms:\n");
        for (Room room : rooms) {
            if (!room.isBooked) {
                sb.append(room).append("\n");
            }
        }
        displayArea.setText(sb.toString());
    }

    private void bookRoom() {
        String roomNumber = (String) roomDropdown.getSelectedItem();
        String customerName = nameField.getText();

        if (roomNumber == null || customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a room and enter your name.");
            return;
        }

        Room selectedRoom = null;
        for (Room room : rooms) {
            if (room.roomNumber.equalsIgnoreCase(roomNumber)) {
                selectedRoom = room;
                break;
            }
        }

        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Room not found.");
            return;
        }

        if (selectedRoom.isBooked) {
            JOptionPane.showMessageDialog(this, "Room already booked.");
            return;
        }

        selectedRoom.isBooked = true;
        reservations.add(new Reservation(selectedRoom.roomNumber, customerName, selectedRoom.category, "Paid"));
        displayArea.setText("Room " + selectedRoom.roomNumber + " booked successfully by " + customerName + ".\n");
    }

    private void cancelBooking() {
        String roomNumber = (String) roomDropdown.getSelectedItem();
        if (roomNumber == null) {
            JOptionPane.showMessageDialog(this, "Select a room to cancel.");
            return;
        }

        Reservation toRemove = null;
        for (Reservation res : reservations) {
            if (res.roomNumber.equalsIgnoreCase(roomNumber)) {
                toRemove = res;
                break;
            }
        }

        if (toRemove != null) {
            reservations.remove(toRemove);

            for (Room room : rooms) {
                if (room.roomNumber.equalsIgnoreCase(roomNumber)) {
                    room.isBooked = false;
                    break;
                }
            }

            displayArea.setText("Booking cancelled for room " + roomNumber + ".\n");
        } else {
            JOptionPane.showMessageDialog(this, "No reservation found for selected room.");
        }
    }

    private void viewReservations() {
        StringBuilder sb = new StringBuilder("Reservations:\n");
        if (reservations.isEmpty()) {
            sb.append("No reservations found.\n");
        } else {
            for (Reservation res : reservations) {
                sb.append(res).append("\n");
            }
        }
        displayArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HotelReservationSystemGUI app = new HotelReservationSystemGUI();
            app.setVisible(true);
        });
    }
}
