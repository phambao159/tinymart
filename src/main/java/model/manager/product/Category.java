package model.manager.product;

public class Category {

    private int CategoryID;
    private String Name;
    private String Description;
    private String Status;

    public Category(int CategoryID, String Name, String Description,String Status) {
        this.CategoryID = CategoryID;
        this.Name = Name;
        this.Description = Description;
        this.Status = Status;
    }

    public Category(String Name, String Description,String Status) {
        this.Name = Name;
        this.Description = Description;
        this.Status = Status;
    }

    public Category() {
    }

    public int getCategoryID() {
        return CategoryID;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    // Setters
    public void setCategoryID(int CategoryID) {
        this.CategoryID = CategoryID;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setDescription(String Description) {
        this.Description = Description;
    }

    @Override
    public String toString() {
        return "Category{"
                + "CategoryID=" + CategoryID
                + ", Name='" + Name + '\''
                + ", Description='" + Description + '\''
                + '}';
    }
    // Trong model.manager.product.Category

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Category category = (Category) o;
        return CategoryID == category.CategoryID;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(CategoryID);
    }
}
