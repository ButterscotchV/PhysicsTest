package net.Dankrushen.PhysicsTest;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class CustomResizer extends MouseAdapter
{
    protected final static Map<Component, CustomResizer> instance = new HashMap<Component, CustomResizer>();
    protected final static Map<Integer, Integer> cursors = new HashMap<Integer, Integer>();
    {
        cursors.put(1, Cursor.N_RESIZE_CURSOR);
        cursors.put(2, Cursor.W_RESIZE_CURSOR);
        cursors.put(4, Cursor.S_RESIZE_CURSOR);
        cursors.put(8, Cursor.E_RESIZE_CURSOR);
        cursors.put(3, Cursor.NW_RESIZE_CURSOR);
        cursors.put(9, Cursor.NE_RESIZE_CURSOR);
        cursors.put(6, Cursor.SW_RESIZE_CURSOR);
        cursors.put(12, Cursor.SE_RESIZE_CURSOR);
    }

    PhysicsTest parentInstance;
    public static CustomResizer install(Component component, PhysicsTest parent)
    {
        if(instance.containsKey(component))
        {
            CustomResizer.uninstall(component);
        }
        CustomResizer crInstance = new CustomResizer();
        crInstance.parentInstance = parent;
        component.addMouseMotionListener(crInstance);
        component.addMouseListener(crInstance);
        instance.put(component, crInstance);
        return crInstance;
    }

    public static void uninstall(Component component)
    {
        CustomResizer crInstance = instance.get(component);
        instance.remove(component);
        component.removeMouseListener(crInstance);
        component.removeMouseMotionListener(crInstance);
    }

    public static CustomResizer getInstance(Component component)
    {
        return instance.get(component);
    }

    protected final static int NORTH = 1;
    protected final static int WEST = 2;
    protected final static int SOUTH = 4;
    protected final static int EAST = 8;

    private int zone = 7 / 2;
    private int offset = 0;

    private Point pressed;
    private int direction;
    private Rectangle bounds;
    private boolean resizing;
    private boolean autoscrolls;
    private Cursor originalCursor;
    
    private Point initialClick;

    @Override
    public void mouseEntered(MouseEvent e)
    {
        originalCursor = !(resizing) ? e.getComponent().getCursor() : originalCursor;
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        Component source = e.getComponent();
        source.setCursor(!(resizing) ? originalCursor : source.getCursor());
    }
    
    private boolean doPop(MouseEvent e) {
		if (e.isPopupTrigger() && instance.containsKey(e.getSource())) {
			PopUp menu = new PopUp(instance.get(e.getSource()).parentInstance);
			menu.show(e.getComponent(), e.getX(), e.getY());
			
			return true;
		}
		
		return false;
	}

    @Override
    public void mousePressed(MouseEvent e)
    {
    	if(doPop(e))
    		return;
    	
    	initialClick = e.getPoint();
		
        if(direction != 0)
        {
            resizing = true;

            pressed = e.getPoint();
            Component source = e.getComponent();
            SwingUtilities.convertPointToScreen(pressed, source);
            bounds = source.getBounds();

            if(source instanceof JComponent)
            {
                JComponent jc = (JComponent) source;
                autoscrolls = jc.getAutoscrolls();
                jc.setAutoscrolls(false);
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if(resizing && e.getModifiers() != 4)
        {
            Component source = e.getComponent();
            Point dragged = e.getPoint();
            SwingUtilities.convertPointToScreen(dragged, source);

            int x = bounds.x;
            int y = bounds.y;
            int width = bounds.width;
            int height = bounds.height;
            Dimension maximumSize = source.getMaximumSize();
            Dimension minimumSize = source.getMinimumSize();

            if(WEST == (direction & WEST))
            {
                int drag = getDragDistance(pressed.x, dragged.x);
                drag = getDragBounded(drag, width, minimumSize.width, Math.min(width + x - offset, maximumSize.width));

                x -= drag;
                width += drag;
            }
            if(NORTH == (direction & NORTH))
            {
                int drag = getDragDistance(pressed.y, dragged.y);
                drag = getDragBounded(drag, height, minimumSize.height, Math.min(height + y - offset, maximumSize.height));

                y -= drag;
                height += drag;
            }
            if(EAST == (direction & EAST))
            {
                int drag = getDragDistance(dragged.x, pressed.x);
                drag = getDragBounded(drag, width, minimumSize.width, Math.min(getBoundingSize(source).width - x, maximumSize.width));

                width += drag;
            }
            if(SOUTH == (direction & SOUTH))
            {
                int drag = getDragDistance(dragged.y, pressed.y);
                drag = getDragBounded(drag, height, minimumSize.height, Math.min(getBoundingSize(source).height - y, maximumSize.height));

                height += drag;
            }

            source.setBounds(x, y, width, height);
        }
        else if(e.getModifiers() != 4)
        {
        	Component source = e.getComponent();
        	
        	// get location of Window
			int thisX = source.getLocation().x;
			int thisY = source.getLocation().y;

			// Determine how much the mouse moved since the initial click
			int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
			int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

			// Move window to this position
			int X = thisX + xMoved;
			int Y = thisY + yMoved;
			source.setLocation(X, Y);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        Component source = e.getComponent();
        direction = 0;

        Point location = e.getPoint();
        int x = location.x;
        int y = location.y;
        int widthOffset = source.getWidth() + offset;
        int heightOffset = source.getHeight() + offset;

        if(x < -offset + zone && x > -offset - zone) direction += WEST;
        if(x > widthOffset - zone && x < widthOffset + zone) direction += EAST;
        if(y < -offset + zone && y > -offset - zone) direction += NORTH;
        if(y > heightOffset - zone && y < heightOffset + zone) direction += SOUTH;

        source.setCursor(direction == 0 ? originalCursor : Cursor.getPredefinedCursor(cursors.get(direction)));
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    	if(doPop(e))
    		return;
    	
        resizing = false;

        Component source = e.getComponent();
        source.setCursor(originalCursor);

        if(source instanceof JComponent)
        {
            ((JComponent) source).setAutoscrolls(autoscrolls);
        }
    }

    private int getDragDistance(int larger, int smaller)
    {
        int drag = larger - smaller;
        drag += (drag < 0) ? -1 : 1;
        return drag;
    }

    private int getDragBounded(int drag, int dimension, int minimum, int maximum)
    {
        while(dimension + drag < minimum) drag += 1;
        while(dimension + drag > maximum) drag -= 1;
        return drag;
    }

    private Dimension getBoundingSize(Component source)
    {
        if(source instanceof Window)
        {
            Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            return new Dimension(bounds.width - offset, bounds.height - offset);
        }
        else
        {
            Dimension size = source.getParent().getSize();
            return new Dimension(size.width - offset, size.height - offset);
        }
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setZone(int zone)
    {
        this.zone = zone / 2;
    }

    public int getZone()
    {
        return zone;
    }
}