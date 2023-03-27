package lk.ijse.dep10.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

public class Student implements Serializable {
    private String id;
    private String name;
    private Blob picture;

    public Student() {
    }

    public Student(String id, String name, Blob picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getPicture() {
        return picture;
    }
    public ImageView getDefaultPicture(){
        try {
            return new ImageView(new Image(picture.getBinaryStream(),75,75,true,true));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPicture(Blob picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", picture=" + picture +
                '}';
    }
}
