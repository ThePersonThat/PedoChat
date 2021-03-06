package org.example.chat.client.graphics.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.example.chat.client.graphics.ChatLayout;
import org.example.chat.client.message.*;

// TODO: 1) Scroll bar on hover;
// TODO: 2) Correct displaying time, sendMessageSelf;
// TODO: 3) It might be better if change label with image;
// TODO: 4) Proportional size of image;
// TODO: 5) Add loading data during scrolling


public class ControllerChat extends AbstractController {

    private volatile Message message;
    private volatile boolean isSendMessage = false;

    public ResourceBundle resources;
    public URL location;
    public TextField input;
    public VBox chatBox;
    public ScrollPane scrollPane;
    public VBox vboxChooser;

    public void setMessageSelf(Message message) {
        ChatLayout.setLayoutMessage(message, chatBox, true);
    }

    public void setMessageOther(Message message) {
        ChatLayout.setLayoutMessage(message, chatBox, false);
    }

    public void initialize() {
        scrollPane.vvalueProperty().bind(chatBox.heightProperty());
    }


    public void checkKey(KeyEvent event) {
        if(event.getCode() == KeyCode.ENTER) {
            send(TypeMessage.TEXT);
        }
    }

    public Message waitMessage() {
        while (!isSendMessage) {
            Thread.onSpinWait();
        }
        
        isSendMessage = false;
        return message;
    }

    public void SendMessage(MouseEvent event) {
        send(TypeMessage.TEXT);
    }

    public void sendFile(MouseEvent event) {
        vboxChooser.setVisible(true);
    }

    public void mouseExit(MouseEvent event) {
        vboxChooser.setVisible(false);
    }


    public void sendFileMessage(MouseEvent event) {
        send(TypeMessage.FILE);
    }

    public void sendImage(MouseEvent event) {
        send(TypeMessage.IMAGE);
    }

    private void send(TypeMessage type) {
        switch (type) {
            case TEXT: {
                if (input.getText().isEmpty())
                    return;

                message = new MessageText();
                String msg = input.getText();
                input.clear();
                message.setContent(msg.getBytes());
                break;
            }
            case IMAGE: {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select image");
                chooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("jpg images", "*.jpg"),
                        new FileChooser.ExtensionFilter("png images", "*.png")
                );

                File file = chooser.showOpenDialog(chatBox.getScene().getWindow());

                if (file == null) {
                    return;
                }

                try {
                    message = new MessageImage();
                    ((AbstractMessageFile)message).setFilename(file.getName());
                    byte[] array = Files.readAllBytes(file.toPath());
                    message.setContent(array);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

                break;
            }
            case FILE: {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Select file");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Any files", "*.*"));
                File file = chooser.showOpenDialog(chatBox.getScene().getWindow());

                if(file == null) {
                    return;
                }

                try {
                    message = new MessageFile();
                    byte[] array = Files.readAllBytes(file.toPath());
                    ((AbstractMessageFile)message).setFilename(file.getName());
                    message.setContent(array);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }

                break;
            }
        }

        setMessageSelf(message);
        isSendMessage = true;
    }

}
