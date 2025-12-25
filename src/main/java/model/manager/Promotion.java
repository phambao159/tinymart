package model.manager;

import java.time.LocalDate;


public class Promotion {
    

    private int PromotionID; 
    private String Name;    
    private String Description;
    private String Type;      
    private double Value;      
    private LocalDate StartDate; 
    private LocalDate EndDate;
    private String Status; 

  
    public Promotion(int PromotionID, String Name, String Description, String Type, double Value, LocalDate StartDate, LocalDate EndDate, String Status) {
        this.PromotionID = PromotionID;
        this.Name = Name;
        this.Description = Description;
        this.Type = Type;
        this.Value = Value;
        this.StartDate = StartDate;
        this.EndDate = EndDate;
        this.Status = Status;
    }

  
    public Promotion(String Name, String Description, String Type, double Value, LocalDate StartDate, LocalDate EndDate, String Status) {
        this.Name = Name;
        this.Description = Description;
        this.Type = Type;
        this.Value = Value;
        this.StartDate = StartDate;
        this.EndDate = EndDate;
        this.Status = Status;
    }

   
    public Promotion() {
    }


    public int getPromotionID() {
        return PromotionID;
    }

    public String getName() {
        return Name;
    }
    
    public String getDescription() { // 🌟 Getter mới
        return Description;
    }

    public String getType() { // 🌟 Getter mới
        return Type;
    }

    public double getValue() { // 🌟 Getter mới
        return Value;
    }

    public LocalDate getStartDate() {
        return StartDate;
    }

    public LocalDate getEndDate() {
        return EndDate;
    }
    
    public String getStatus() {
        return Status;
    }
    
    public void setPromotionID(int PromotionID) {
        this.PromotionID = PromotionID;
    }
    public void setName(String Name) {
        this.Name = Name;
    }
    public void setDescription(String Description) {
        this.Description = Description;
    }
    public void setType(String Type) {
        this.Type = Type;
    }
    public void setValue(double Value) {
        this.Value = Value;
    }
    public void setStartDate(LocalDate StartDate) {
        this.StartDate = StartDate;
    }
    public void setEndDate(LocalDate EndDate) {
        this.EndDate = EndDate;
    }
    public void setStatus(String Status) {
        this.Status = Status;
    }
}