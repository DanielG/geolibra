package org.geogebra.web.web.gui.app;

import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.main.App;
import org.geogebra.web.html5.gui.util.ClickStartHandler;
import org.geogebra.web.html5.gui.view.algebra.MathKeyboardListener;
import org.geogebra.web.web.css.GuiResources;
import org.geogebra.web.web.gui.NoDragImage;
import org.geogebra.web.web.gui.layout.DockPanelW;
import org.geogebra.web.web.util.keyboard.OnScreenKeyBoard;
import org.geogebra.web.web.util.keyboard.UpdateKeyBoardListener;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A PopupPanel in the bottom left corner of the application which represents a
 * button to open the {@link OnScreenKeyBoard}
 */
public class ShowKeyboardButton extends SimplePanel {
	
	private final int HEIGHT = 33;
	private Widget parent;

	/**
	 * @param listener
	 *            {@link UpdateKeyBoardListener}
	 * @param textField
	 *            {@link Widget}
	 * @param parent
	 *            {@link Element}
	 */
	public ShowKeyboardButton(final UpdateKeyBoardListener listener,
	        final MathKeyboardListener textField, Widget parent) {

		this.parent = parent;
		this.addStyleName("openKeyboardButton");
		NoDragImage showKeyboard = new NoDragImage(GuiResources.INSTANCE
		        .keyboard_show().getSafeUri().asString());
		this.add(showKeyboard);

		((DockPanelW) parent).addSouth(this);
		ClickStartHandler.init(ShowKeyboardButton.this, new ClickStartHandler(
		        true, true) {

			@Override
			public void onClickStart(int x, int y,
 PointerEventType type) {
				App.debug("show keyboard");
				listener.doShowKeyBoard(true, textField);

				Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

					@Override
					public boolean execute() {
						textField.ensureEditing();
						textField.setFocus(true);
						return false;
					}
				}, 0);

			}
		});
	}

	/**
	 * 
	 * @param show
	 *            {@code true} to show the button to open the OnScreenKeyboard
	 * @param textField
	 *            {@link Widget} to set as AutoHidePartner
	 */
	public void show(boolean show, MathKeyboardListener textField) {


		if (show && parent.isVisible()) {
			setVisible(true);
		} else {
			App.printStacktrace("");
			setVisible(false);
		}

	}

	public void hide() {
		setVisible(false);
	}

}