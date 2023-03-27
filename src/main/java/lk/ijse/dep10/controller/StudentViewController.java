package lk.ijse.dep10.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lk.ijse.dep10.db.DBConnection;
import lk.ijse.dep10.model.Student;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.*;

public class StudentViewController {

    public VBox vBoxImg;
    @FXML
    private Button btnBrowse;

    @FXML
    private Button btnClear;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnNewStudent;

    @FXML
    private Button btnSave;

    @FXML
    private ImageView imgPicture;

    @FXML
    private TableView<Student> tblStudents;

    @FXML
    private TextField txtStudentID;

    @FXML
    private TextField txtStudentName;

    @FXML
    private TextField txtStudentSearch;
    public void initialize(){
        Platform.runLater(btnNewStudent :: fire);

        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("defaultPicture"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudents.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));

        loadAllStudents();

        tblStudents.getSelectionModel().selectedItemProperty().addListener((value, previous, current) -> {
            if (current == null){
                return;
            }
            btnClear.setDisable(false);
            btnDelete.setDisable(false);
            txtStudentID.setText(current.getId());
            txtStudentName.setText(current.getName());

            if (current.getPicture() != null){
                try {
                    InputStream inputStream = current.getPicture().getBinaryStream();
                    imgPicture.setImage(new Image(inputStream));
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR,"Failed to load the image, try again").showAndWait();
                    throw new RuntimeException(e);
                }
            }
            if (current.getPicture() == null){
                imgPicture.setImage(new Image("/images/no_profile_picture.png"));
            }
        });
        txtStudentSearch.textProperty().addListener((value, current, previous) -> {
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement stm1 = connection.prepareStatement("SELECT * FROM Student WHERE (id LIKE ? OR name LIKE ?)");
                PreparedStatement stm2 = connection.prepareStatement("SELECT * FROM Picture WHERE (student_id = ?)");

                stm1.setString(1,"%" + current + "%");
                stm1.setString(2,"%" + current + "%");

                ResultSet rst1 = stm1.executeQuery();

                ObservableList<Student> studentList = tblStudents.getItems();
                studentList.clear();

                while (rst1.next()){
                    String id = rst1.getString("id");
                    String name = rst1.getString("name");
                    stm2.setString(1,id);
                    ResultSet rst2 = stm2.executeQuery();
                    if (rst2.next()){
                        Blob picture = rst2.getBlob("picture");
                        studentList.add(new Student(id,name,picture));
                    }
                    else if (!rst2.next()){
                        Image defaultPicture = new Image("/images/no_profile_picture.png");
                        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(defaultPicture, null);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
                            byte[] bytes = byteArrayOutputStream.toByteArray();
                            Blob picture = new SerialBlob(bytes);
                            studentList.add(new Student(id,name,picture));
                        } catch (IOException e) {
                            new Alert(Alert.AlertType.ERROR,"Cannot write the image, try again").showAndWait();
                            throw new RuntimeException(e);
                        }
                    }

                }
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR,"Cannot search students, try again...!").showAndWait();
                throw new RuntimeException(e);
            }
        });
    }
    private void loadAllStudents(){
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement stm1 = connection.createStatement();
            ResultSet rst1 = stm1.executeQuery("SELECT * FROM Student");
            PreparedStatement stm2 = connection.prepareStatement("SELECT * FROM Picture WHERE student_id = ?");
            while (rst1.next()){
                String id = rst1.getString("id");
                String name = rst1.getString("name");

                Blob picture = null;

                stm2.setString(1,id);
                ResultSet rst2 = stm2.executeQuery();

                if (rst2.next()){
                    picture = rst2.getBlob("picture");
                }
                else if (!rst2.next()){
                    Image defaultPicture = new Image("/images/no_profile_picture.png");
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(defaultPicture, null);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        picture = new SerialBlob(bytes);
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR,"Cannot write the image, try again").showAndWait();
                        throw new RuntimeException(e);
                    }
                }
                Student student = new Student(id, name, picture);
                tblStudents.getItems().add(student);
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to load students, try again..").showAndWait();
            throw new RuntimeException(e);
        }
    }

    @FXML
    void btnBrowseOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a profile picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files","*.jpg","*.jpeg","*.png","*.gif","*.pmp"));
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file != null){
            Image image = null;
            try {
                image = new Image(file.toURI().toURL().toString(), 226, 200, true, true);
            } catch (MalformedURLException e) {
                new Alert(Alert.AlertType.ERROR,"Failed to load profile picture, try again").showAndWait();
                throw new RuntimeException(e);
            }
            imgPicture.setImage(image);
            btnClear.setDisable(false);
        }
    }

    @FXML
    void btnClearOnAction(ActionEvent event) {
        imgPicture.setImage(null);
    }

    @FXML
    void btnDeleteOnAction(ActionEvent event) {
        Student selectedStudent = tblStudents.getSelectionModel().getSelectedItem();
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement stmPicture = connection.prepareStatement("DELETE FROM Picture WHERE student_id = ?");
            PreparedStatement stmStudent = connection.prepareStatement("DELETE FROM Student WHERE id = ?");

            stmPicture.setString(1,selectedStudent.getId());
            stmPicture.executeUpdate();

            stmStudent.setString(1, selectedStudent.getId());
            stmStudent.executeUpdate();

            connection.commit();

            tblStudents.getItems().remove(selectedStudent);
            if (tblStudents.getItems().isEmpty()) btnNewStudent.fire();

        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            new Alert(Alert.AlertType.ERROR,"Failed to delete data, try again").showAndWait();
            throw new RuntimeException(e);
        }
        finally {
            try {
                connection.setAutoCommit(true);
            }
            catch (SQLException e){
                throw new RuntimeException();
            }
        }
    }

    @FXML
    void btnNewStudentOnAction(ActionEvent event) {
        txtStudentID.setText(generateNewId());
        txtStudentName.clear();
        imgPicture.setImage(null);
        tblStudents.getSelectionModel().clearSelection();
    }
    private String generateNewId(){
        String id = "";
        ObservableList<Student> studentList = tblStudents.getItems();
        if (studentList.size() == 0){
            id = "DEP-10/S001";
        }
        else {
            String studentId = studentList.get(studentList.size() - 1).getId();
            int newId = Integer.parseInt(studentId.substring(8)) + 1;
            id = String.format("DEP-10/S%03d",newId);
        }
        return id;

    }

    @FXML
    void btnSaveOnAction(ActionEvent event) {
        if (!isDataValid()) return;

        Student selectedStudent = tblStudents.getSelectionModel().getSelectedItem();
        String studentId = txtStudentID.getText();
        String studentName = txtStudentName.getText();

        Connection connection = DBConnection.getInstance().getConnection();
        if (selectedStudent == null){
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmStudent = connection.prepareStatement("INSERT INTO Student (id, name) VALUES (?,?)");
                PreparedStatement stmPicture = connection.prepareStatement("INSERT INTO Picture (student_id, picture) VALUES (?,?)");

                stmStudent.setString(1, studentId);
                stmStudent.setString(2,studentName);
                stmStudent.executeUpdate();

                Student student = new Student(studentId, studentName, null);
                Image picture = imgPicture.getImage();
                Image defaultPicture = new Image("/images/no_profile_picture.png");

                if (picture != null){
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(picture, null);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        Blob image = new SerialBlob(bytes);
                        student.setPicture(image);

                        stmPicture.setString(1,studentId);
                        stmPicture.setBlob(2,image);
                        stmPicture.executeUpdate();
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR,"Cannot write the image, try again").showAndWait();
                        throw new RuntimeException(e);
                    }
                }
                else if (picture == null){
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(defaultPicture, null);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        Blob image = new SerialBlob(bytes);
                        student.setPicture(image);
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR,"Cannot write the image, try again").showAndWait();
                        throw new RuntimeException(e);
                    }
                }
                connection.commit();
                tblStudents.getItems().add(student);

            }
            catch (Throwable e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                new Alert(Alert.AlertType.ERROR,"Failed to save data, try again...!").showAndWait();
                throw new RuntimeException(e);
            }
            finally {
                try {
                    connection.setAutoCommit(true);
                }
                catch (SQLException e){
                    throw new RuntimeException();
                }
            }
        }
        if (selectedStudent != null){
            try {
                connection.setAutoCommit(false);
                PreparedStatement stmStudent = connection.prepareStatement("UPDATE Student SET name = ? WHERE id = ?");
                PreparedStatement stmPicture = connection.prepareStatement("UPDATE Picture SET picture = ? WHERE student_id = ?");
                PreparedStatement stmUpdatePicture = connection.prepareStatement("INSERT INTO Picture (student_id, picture) VALUES (?,?)");

                stmStudent.setString(1, txtStudentName.getText());
                stmStudent.setString(2,txtStudentID.getText());
                stmStudent.executeUpdate();

                selectedStudent.setId(txtStudentID.getText());
                selectedStudent.setName(txtStudentName.getText());

                Image picture = imgPicture.getImage();
                Image defaultPicture = new Image("/images/no_profile_picture.png");

                if (picture != null){
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(picture, null);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        Blob image = new SerialBlob(bytes);

                        stmPicture.setBlob(1,image);
                        stmPicture.setString(2,txtStudentID.getText());
                        stmPicture.executeUpdate();

                        if (stmPicture.executeUpdate() == 0){
                            stmUpdatePicture.setString(1, txtStudentID.getText());
                            stmUpdatePicture.setBlob(2,image);
                            stmUpdatePicture.executeUpdate();
                        }

                        selectedStudent.setPicture(image);
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR,"Cannot write the image, try again").showAndWait();
                        throw new RuntimeException(e);
                    }
                }
                else if (picture == null){
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(defaultPicture, null);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage,"png",byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        Blob image = new SerialBlob(bytes);
                        selectedStudent.setPicture(image);
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR,"Cannot write the image, try again").showAndWait();
                        throw new RuntimeException(e);
                    }
                }
                connection.commit();
                tblStudents.refresh();

            }
            catch (Throwable e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                new Alert(Alert.AlertType.ERROR,"Failed to update data, try again...!").showAndWait();
                throw new RuntimeException(e);
            }
            finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }
    private boolean isDataValid(){
        boolean dataValid = true;

        if (!txtStudentName.getText().matches("[A-z ]{3,}")){
            txtStudentName.requestFocus();
            txtStudentName.selectAll();
            new Alert(Alert.AlertType.ERROR,"Invalid name. Name should have at least 3 letters and other characters are not allowed").showAndWait();
            dataValid = false;
        }

        return dataValid;
    }

}

