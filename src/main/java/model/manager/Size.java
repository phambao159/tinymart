/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.manager;

/**
 *
 * @author user
 */
public class Size {
    private int SizeID;
    private String Type;

    public Size(int SizeID, String Type) {
        this.SizeID = SizeID;
        this.Type = Type;
    }

    public Size(String Type) {
        this.Type = Type;
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
