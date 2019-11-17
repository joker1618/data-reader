package xxx.joker.apps.reporeader.jfx.view.util;

import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.List;

import static xxx.joker.libs.core.util.JkConsole.display;
import static xxx.joker.libs.core.util.JkConvert.toList;

/**
 * FIXME works properly only with Side == LEFT
 *
 * @author federico barbano
 * 
 */
public class DragResizer {

    /**
     * The margin around the control that a user can click in to start resizing
     * the region.
     */
    private static final int RESIZE_MARGIN = 5;

    private final Region region;

    private double x;
    private double y;
    private List<Side> sides;
    private boolean initMinHeight;
    private Side draggingSide;


    private DragResizer(Region region, Side... sides) {
        this.region = region;
        this.sides = toList(sides);
    }

    public static void makeResizable(Region region, Side... sides) {
        final DragResizer resizer = new DragResizer(region, sides);
        region.setOnMousePressed(resizer::mousePressed);
        region.setOnMouseDragged(resizer::mouseDragged);
        region.setOnMouseMoved(resizer::mouseOver);
        region.setOnMouseReleased(resizer::mouseReleased);
        if(region instanceof Pane) {
            ((Pane) region).getChildren().forEach(ch -> ch.setOnMouseMoved(e -> ch.setCursor(Cursor.DEFAULT)));
        }
    }

    protected void mouseReleased(MouseEvent event) {
        draggingSide = null;
        region.setCursor(Cursor.DEFAULT);
    }

    protected void mouseOver(MouseEvent event) {
        if(!isDragging()) {
            Side side = getCurrentDraggableSide(event);
            Cursor cursor;
            if (side == null) {
                cursor = Cursor.DEFAULT;
            } else if (side == Side.TOP) {
                cursor = Cursor.N_RESIZE;
            } else if (side == Side.BOTTOM) {
                cursor = Cursor.S_RESIZE;
            } else if (side == Side.LEFT) {
                cursor = Cursor.W_RESIZE;
            } else {
                cursor = Cursor.E_RESIZE;
            }
            region.setCursor(cursor);
        }
    }

    private boolean isDragging() {
        return draggingSide != null;
    }

    protected boolean isInDraggableZone(MouseEvent event) {
        return getCurrentDraggableSide(event) != null;
    }

    protected Side getCurrentDraggableSide(MouseEvent event) {
        if(sides.contains(Side.LEFT) && event.getX() >= 0 && event.getX() < RESIZE_MARGIN)
            return Side.LEFT;
        if(sides.contains(Side.RIGHT) && event.getX() <= region.getWidth() && event.getX() > (region.getWidth() - RESIZE_MARGIN))
            return Side.RIGHT;
        if(sides.contains(Side.TOP) && event.getY() >= 0 && event.getY() < RESIZE_MARGIN)
            return Side.TOP;
        if(sides.contains(Side.BOTTOM) && event.getX() <= region.getHeight() && event.getY() > (region.getHeight() - RESIZE_MARGIN))
            return Side.BOTTOM;
        return null;
    }

    protected void mouseDragged(MouseEvent event) {
        if(!isDragging()) {
            return;
        }

        double mousex = event.getX();
        double mousey = event.getY();
        if(draggingSide.isVertical() && mousex >= 0 && mousex != x) {
            if(draggingSide == Side.LEFT) {
                region.setPrefWidth(region.getWidth() + (x - mousex));
                region.setLayoutX(mousex);
                display("%4.0f  %4.0f  %4.0f", region.getWidth(), mousex, x);
            } else {
                region.setPrefWidth(region.getWidth() + (mousex - x));

            }
            x = mousex;

        } else if(draggingSide.isHorizontal() && mousey >= 0 && mousey != y) {
            if(draggingSide == Side.TOP) {
                region.setPrefHeight(region.getHeight() + (y - mousey));
                region.setLayoutY(mousey);
            } else {
                region.setPrefHeight(region.getHeight() + (mousey - y));
            }
            y = mousey;
        }
    }

    protected void mousePressed(MouseEvent event) {
        // ignore clicks outside of the draggable margin
        Side side = getCurrentDraggableSide(event);
        if(side == null) {
            return;
        }

        draggingSide = side;

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
//        if (!initMinHeight) {
//            region.setMinHeight(region.getHeight());
//            initMinHeight = true;
//        }

        x = event.getX();
        y = event.getY();
    }
//    protected void mousePressed(MouseEvent event) {
//
//        // ignore clicks outside of the draggable margin
//        if(!isInDraggableZone(event)) {
//            return;
//        }
//
//        dragging = true;
//
//        // make sure that the minimum height is set to the current height once,
//        // setting a min height that is smaller than the current height will
//        // have no effect
//        if (!initMinHeight) {
//            region.setMinHeight(region.getHeight());
//            initMinHeight = true;
//        }
//
//        y = event.getY();
//    }
}