package agivdel.laksia;

import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;


public class Drawer extends View{

    public void drawAllFaces(MatOfRect faces) {
        facesPane.getChildren().clear();
        for (Rect r : faces.toList()) {
            drawSingleFace(r);
        }
    }

    public void drawSingleFace(Rect r) {
        double topLeftX = (r.x /** widthScaleFactor*/ + imageView.getLayoutX());// widthScaleFactor;
        double topLeftY = (r.y /** heightScaleFactor*/ + imageView.getLayoutY());// heightScaleFactor;
        Shape faceRectangle = new Rectangle(topLeftX, topLeftY, r.width, r.height);

        Text text = new Text(topLeftX, topLeftY, "another shchi");
        text.setTextOrigin(VPos.TOP);//начало координат в левом верхнем углу узла text. Доступен только для класса Text
        text.setFill(Color.WHITE);
        text.setFont(Font.font(16.0));
        text.setOpacity(1.0);
        if (!facess.get(r).equals(String.valueOf(0))) {
            text.setText(facess.get(r));
        }

        text.setOnMousePressed(me -> {
            if (me.isSecondaryButtonDown()) {
                TextField textField = new TextField();
                facesPane.getChildren().add(textField);
                textField.setLayoutX(topLeftX);
                textField.setLayoutY(topLeftY + 20);
                textField.setPromptText("уточните имя");
                textField.setOnKeyReleased(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        String newName = textField.getText().trim();
                        if (!newName.equals("") & newName.length() > 0) {
                            text.setText(newName);
                            facess.put(r, newName);
                        }
                        facesPane.getChildren().remove(textField);
                    }
                });
            }
        });

        NodeEffects.makeDraggable(faceRectangle);
        NodeEffects.makeBindUp(text, faceRectangle);
        NodeEffects.makeHighlighted(faceRectangle);
        facesPane.getChildren().addAll(faceRectangle, text);
    }
}