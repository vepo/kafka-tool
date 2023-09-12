package dev.vepo.kafka.tool.controls;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class TopicConsumerStatusBar extends HBox {
    public enum Status {
        IDLE("Idle"),
        CONSUMING("Consuming..."),
        FINISHED("Finished!"),
        STOPPED("Stopped"), 
        ERROR("Error");

        private String text;

        private Status(String text) {
            this.text = text;
        }
    }

    private Text txtStatus;
    private Text txtCurrenOffset;

    public TopicConsumerStatusBar(double spacing) {
        super(spacing);
        txtStatus = new Text(Status.IDLE.text);
        txtCurrenOffset = new Text("0");
        this.getChildren().add(txtStatus);
        this.getChildren().add(txtCurrenOffset);
    }

    public void status(Status status) {
        txtStatus.setText(status.text);
    }

    public void offset(long offset) {
        txtCurrenOffset.setText(Long.toString(offset));
    }

}
