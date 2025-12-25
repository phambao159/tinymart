package model.manager;

/**
 * Model class for Supplier table
 */
public class Supplier {

    private int supplierID;      // SupplierID int(10) UNSIGNED
    private String name;         // Name varchar(255)
    private String contactPerson; // ContactPerson varchar(255)
    private String phoneNumber;   // PhoneNumber varchar(20)
    private String address;       // Address text

    // Constructor mặc định
    public Supplier() {
    }

    // Constructor đầy đủ (dùng khi lấy dữ kết quả từ database)
    public Supplier(int supplierID, String name, String contactPerson, String phoneNumber, String address) {
        this.supplierID = supplierID;
        this.name = name;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    // Constructor không có ID (dùng khi thêm mới nhà cung cấp - ID tự tăng)
    public Supplier(String name, String contactPerson, String phoneNumber,  String address) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;

        this.address = address;
    }

    // Getter và Setter
    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "supplierID=" + supplierID +
                ", name='" + name + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}