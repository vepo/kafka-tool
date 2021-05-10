package io.vepo.kafka.tool.controls.helpers;

import static javafx.scene.Cursor.DEFAULT;
import static javafx.scene.Cursor.E_RESIZE;
import static javafx.scene.Cursor.HAND;
import static javafx.scene.Cursor.NE_RESIZE;
import static javafx.scene.Cursor.NW_RESIZE;
import static javafx.scene.Cursor.N_RESIZE;
import static javafx.scene.Cursor.SE_RESIZE;
import static javafx.scene.Cursor.SW_RESIZE;
import static javafx.scene.Cursor.S_RESIZE;
import static javafx.scene.Cursor.W_RESIZE;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_EXITED;
import static javafx.scene.input.MouseEvent.MOUSE_EXITED_TARGET;
import static javafx.scene.input.MouseEvent.MOUSE_MOVED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

import java.util.Objects;

import io.vepo.kafka.tool.controls.WindowHead;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Util class to handle window resizing when a stage style set to
 * StageStyle.UNDECORATED. Created on 8/15/17.
 *
 * @author Evgenii Kanivets
 */
public class ResizeHelper {

    public static void addResizeListener(Stage stage) {
	addResizeListener(stage, 0, 0, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static void addResizeListener(Stage stage, double minWidth, double minHeight, double maxWidth,
	    double maxHeight) {
	var resizeListener = new ResizeListener(stage);
	resizeListener.setMinWidth(minWidth);
	resizeListener.setMinHeight(minHeight);
	resizeListener.setMaxWidth(maxWidth);
	resizeListener.setMaxHeight(maxHeight);
	if (Objects.nonNull(stage.getScene())) {
	    addResizeListener(stage.getScene(), resizeListener);
	}
	stage.sceneProperty()
		.addListener((observable, oldScene, newScene) -> addResizeListener(newScene, resizeListener));
    }

    private static void addResizeListener(Scene scene, ResizeListener resizeListener) {
	scene.addEventHandler(MOUSE_MOVED, resizeListener);
	scene.addEventHandler(MOUSE_PRESSED, resizeListener);
	scene.addEventHandler(MOUSE_DRAGGED, resizeListener);
	scene.addEventHandler(MOUSE_EXITED, resizeListener);
	scene.addEventHandler(MOUSE_EXITED_TARGET, resizeListener);
	scene.addEventHandler(MOUSE_RELEASED, resizeListener);

	ObservableList<Node> children = scene.getRoot().getChildrenUnmodifiable();
	children.addListener(
		(ListChangeListener<Node>) c -> c.getList().forEach(child -> addListenerDeeply(child, resizeListener)));
	for (Node child : children) {
	    addListenerDeeply(child, resizeListener);
	}
    }

    private static void addListenerDeeply(Node node, EventHandler<MouseEvent> listener) {
	node.addEventHandler(MOUSE_MOVED, listener);
	node.addEventHandler(MOUSE_PRESSED, listener);
	node.addEventHandler(MOUSE_DRAGGED, listener);
	node.addEventHandler(MOUSE_EXITED, listener);
	node.addEventHandler(MOUSE_EXITED_TARGET, listener);
	node.addEventHandler(MOUSE_RELEASED, listener);

	if (node instanceof Parent) {
	    Parent parent = (Parent) node;
	    ObservableList<Node> children = parent.getChildrenUnmodifiable();
	    children.addListener(
		    (ListChangeListener<Node>) c -> c.getList().forEach(child -> addListenerDeeply(child, listener)));
	    for (Node child : children) {
		addListenerDeeply(child, listener);
	    }
	}
    }

    static class ResizeListener implements EventHandler<MouseEvent> {
	private Stage stage;
	private Cursor cursorEvent = Cursor.DEFAULT;
	private int border = 6;
	private double startX = 0;
	private double startY = 0;

	// Max and min sizes for controlled stage
	private double minWidth;
	private double maxWidth;
	private double minHeight;
	private double maxHeight;
	private double deltaX = 0;
	private double deltaY = 0;

	public ResizeListener(Stage stage) {
	    this.stage = stage;
	}

	public void setMinWidth(double minWidth) {
	    this.minWidth = minWidth;
	}

	public void setMaxWidth(double maxWidth) {
	    this.maxWidth = maxWidth;
	}

	public void setMinHeight(double minHeight) {
	    this.minHeight = minHeight;
	}

	public void setMaxHeight(double maxHeight) {
	    this.maxHeight = maxHeight;
	}

	@Override
	public void handle(MouseEvent mouseEvent) {
	    var mouseEventType = mouseEvent.getEventType();
	    var target = mouseEvent.getTarget();
	    var scene = stage.getScene();

	    double mouseEventX = mouseEvent.getSceneX(), mouseEventY = mouseEvent.getSceneY(),
		    sceneWidth = scene.getWidth(), sceneHeight = scene.getHeight();

	    if (MOUSE_MOVED.equals(mouseEventType)) {
		if (mouseEventX < border && mouseEventY < border) {
		    cursorEvent = NW_RESIZE;
		} else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
		    cursorEvent = SW_RESIZE;
		} else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
		    cursorEvent = NE_RESIZE;
		} else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
		    cursorEvent = SE_RESIZE;
		} else if (mouseEventX < border) {
		    cursorEvent = W_RESIZE;
		} else if (mouseEventX > sceneWidth - border) {
		    cursorEvent = E_RESIZE;
		} else if (mouseEventY < border) {
		    cursorEvent = N_RESIZE;
		} else if (mouseEventY > sceneHeight - border) {
		    cursorEvent = S_RESIZE;
		} else {
		    cursorEvent = DEFAULT;
		}
		scene.setCursor(cursorEvent);
	    } else if (MOUSE_RELEASED.equals(mouseEventType)) {
		cursorEvent = DEFAULT;
		scene.setCursor(DEFAULT);
	    } else if (MOUSE_PRESSED.equals(mouseEventType)) {
		startX = stage.getWidth() - mouseEventX;
		startY = stage.getHeight() - mouseEventY;
		if (target instanceof WindowHead
			|| (target instanceof Node && ((Node) target).getStyleClass().contains("movable"))) {
		    cursorEvent = HAND;
		    scene.setCursor(cursorEvent);
		    deltaX = stage.getX() - mouseEvent.getScreenX();
		    deltaY = stage.getY() - mouseEvent.getScreenY();
		}
	    } else if (MOUSE_DRAGGED.equals(mouseEventType)) {
		if (HAND.equals(cursorEvent)) {
		    stage.setX(mouseEvent.getScreenX() + deltaX);
		    stage.setY(mouseEvent.getScreenY() + deltaY);
		} else if (!DEFAULT.equals(cursorEvent)) {
		    if (!W_RESIZE.equals(cursorEvent) && !E_RESIZE.equals(cursorEvent)) {
			double minHeight = stage.getMinHeight() > (border * 2) ? stage.getMinHeight() : (border * 2);
			if (NW_RESIZE.equals(cursorEvent) || N_RESIZE.equals(cursorEvent)
				|| NE_RESIZE.equals(cursorEvent)) {
			    if (stage.getHeight() > minHeight || mouseEventY < 0) {
				setStageHeight(stage.getY() - mouseEvent.getScreenY() + stage.getHeight());
				stage.setY(mouseEvent.getScreenY());
			    }
			} else {
			    if (stage.getHeight() > minHeight || mouseEventY + startY - stage.getHeight() > 0) {
				setStageHeight(mouseEventY + startY);
			    }
			}
		    }

		    if (!N_RESIZE.equals(cursorEvent) && !S_RESIZE.equals(cursorEvent)) {
			double minWidth = stage.getMinWidth() > (border * 2) ? stage.getMinWidth() : (border * 2);
			if (NW_RESIZE.equals(cursorEvent) || W_RESIZE.equals(cursorEvent)
				|| SW_RESIZE.equals(cursorEvent)) {
			    if (stage.getWidth() > minWidth || mouseEventX < 0) {
				setStageWidth(stage.getX() - mouseEvent.getScreenX() + stage.getWidth());
				stage.setX(mouseEvent.getScreenX());
			    }
			} else {
			    if (stage.getWidth() > minWidth || mouseEventX + startX - stage.getWidth() > 0) {
				setStageWidth(mouseEventX + startX);
			    }
			}
		    }
		}
	    }
	}

	private void setStageWidth(double width) {
	    width = Math.min(width, maxWidth);
	    width = Math.max(width, minWidth);
	    stage.setWidth(width);
	}

	private void setStageHeight(double height) {
	    height = Math.min(height, maxHeight);
	    height = Math.max(height, minHeight);
	    stage.setHeight(height);
	}

    }
}
