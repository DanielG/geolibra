package org.geogebra.web.html5.gui.util;

import org.geogebra.common.euclidian.event.PointerEventType;

import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.user.client.ui.Widget;

public abstract class ClickEndHandler {

	/**
	 * Attaches a handler for MouseUpEvent and a TouchEndEvent to the widget.
	 * CancelEventTimer is used to prevent duplication of events.
	 * 
	 * @param w
	 *            Widget that the handlers are attached to
	 * @param handler
	 *            EventHandler (instance of this class)
	 */
	public static void init(Widget w, final ClickEndHandler handler) {
		w.addDomHandler(new MouseUpHandler() {
			public void onMouseUp(MouseUpEvent event) {
				if (handler.preventDefault) {
					event.preventDefault();
				}
				if (handler.stopPropagation) {
					event.stopPropagation();
				}
				if (!CancelEventTimer.cancelMouseEvent()) {
					handler.onClickEnd(event.getX(), event.getY(),
					        PointerEventType.MOUSE);
				}
			}
		}, MouseUpEvent.getType());

		w.addDomHandler(new TouchEndHandler() {
			public void onTouchEnd(TouchEndEvent event) {
				if (handler.preventDefault) {
					event.preventDefault();
				}
				if (handler.stopPropagation) {
					event.stopPropagation();
				}
				handler.onClickEnd(event.getTouches().get(0).getClientX(),
				        event.getTouches().get(0).getClientY(),
				        PointerEventType.TOUCH);
				CancelEventTimer.touchEventOccured();
			}
		}, TouchEndEvent.getType());
	}

	boolean preventDefault = false;
	boolean stopPropagation = false;

	/**
	 * creates the base version of a ClickEventHandler.
	 */
	public ClickEndHandler() {
	}

	/**
	 * {@link ClickEndHandler} with preventDefault and stopPropagation set
	 * explicitly. event.preventDefault() and event.stopPropagation() will also
	 * be called, if the handling-method is canceled for the event.
	 * 
	 * @param preventDefault
	 *            whether or not event.preventDefault() should be called for
	 *            MouseUpEvents and TouchEndEvents
	 * @param stopPropagation
	 *            whether or not event.stopPropagation() should be called for
	 *            MouseUpEvents and TouchEndEvents
	 */
	public ClickEndHandler(boolean preventDefault, boolean stopPropagation) {
		this.preventDefault = preventDefault;
		this.stopPropagation = stopPropagation;
	}

	/**
	 * Actual handler-method, needs to be overwritten in the instances.
	 * 
	 * @param x
	 *            x-coordinate of the event
	 * @param y
	 *            y-coordinate of the event
	 * @param type
	 *            type of the event
	 */
	public abstract void onClickEnd(int x, int y, PointerEventType type);

	/**
	 * Set preventDefault explicitly. event.preventDefault() will also be
	 * called, if the handling-method is canceled for the event.
	 * 
	 * @param preventDefault
	 *            whether or not event.preventDefault() should be called for
	 *            MouseUpEvents and TouchEndEvents
	 */
	public void setPreventDefault(boolean preventDefault) {
		this.preventDefault = preventDefault;
	}

	/**
	 * Set stopPropagation explicitly. event.stopPropagation() will also be
	 * called, if the handling-method is canceled for the event.
	 * 
	 * @param stopPropagation
	 *            whether or not event.stopPropagation() should be called for
	 *            MouseUpEvents and TouchEndEvents
	 */
	public void setStopPropagation(boolean stopPropagation) {
		this.stopPropagation = stopPropagation;
	}
}