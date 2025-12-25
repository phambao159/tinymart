package model.manager;


public class Category {
    

    private int CategoryID; 
    private String Name;    
    private String Description;


    public Category(int CategoryID, String Name, String Description) {
        this.CategoryID = CategoryID;
        this.Name = Name;
        this.Description = Description;
    }

    public Category(String Name, String Description) {
        this.Name = Name;
        this.Description = Description;
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
        return "Category{" +
               "CategoryID=" + CategoryID +
               ", Name='" + Name + '\'' +
               ", Description='" + Description + '\'' +
               '}';
    }
}