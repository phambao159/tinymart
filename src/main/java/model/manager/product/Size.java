/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.manager.product;

/**
 *
 * @author user
 */
public class Size {
    private int SizeID;
    private String Type;
    private String Status;

    public Size(int SizeID, String Type,String Status) {
        this.SizeID = SizeID;
        this.Type = Type;
        this.Status = Status;
    }

    public Size(String Type,String Status) {
        this.Type = Type;
        this.Status = Status;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }
    
    
    public Size(){
        
    }

    public int getSizeID() {
        return SizeID;
    }

    public void setSizeID(int SizeID) {
        this.SizeID = SizeID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }
    
}
