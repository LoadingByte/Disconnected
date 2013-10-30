/*
 * This file is part of Disconnected.
 * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
 *
 * Disconnected is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Disconnected is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
 */

package com.quartercode.disconnected.graphics.session;

import com.quartercode.disconnected.Disconnected;
import com.quartercode.disconnected.sim.comp.session.Shell;
import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.BoxLayout.Direction;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.EditField.Callback;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ScrollPane.Fixed;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;

/**
 * A shell widget can display the output of a shell.
 * You can also input commands using a shell widget.
 * 
 * @see Shell
 */
public class ShellWidget extends Widget {

    private Shell                   shell;

    private final TextArea          output;
    private final HTMLTextAreaModel outputModel;
    private final EditField         input;
    private final BoxLayout         layout;
    private final ScrollPane        scrollPane;

    /**
     * Creates a new shell widget and sets it up.
     * 
     * @param shell The shell to use for the widget.
     */
    public ShellWidget(Shell shell) {

        this.shell = shell;

        setTheme("");

        outputModel = new HTMLTextAreaModel();

        output = new TextArea(outputModel);
        output.setTheme("/shelloutput");

        input = new EditField();
        input.setTheme("/shellinput");
        input.addCallback(new Callback() {

            @Override
            public void callback(int key) {

                if (key == Event.KEY_RETURN) {
                    ShellWidget.this.shell.run(input.getText());
                    input.setText("");
                }
            }
        });

        layout = new BoxLayout(Direction.VERTICAL);
        layout.setTheme("");
        layout.setSpacing(0);
        layout.setAlignment(Alignment.FILL);
        layout.add(output);
        layout.add(input);

        scrollPane = new ScrollPane(layout);
        scrollPane.setTheme("/scrollpane");
        scrollPane.setFixed(Fixed.HORIZONTAL);
        add(scrollPane);
    }

    /**
     * Returns the shell this widget is rendering.
     * 
     * @return The shell this widget is rendering.
     */
    public Shell getShell() {

        return shell;
    }

    /**
     * Updates the text area content to the latest output of the set shell.
     */
    public void update() {

        final StringBuilder output = new StringBuilder();
        for (String line : shell.getOutput()) {
            output.append(line).append("<br/>");
        }

        outputModel.setHtml(output.toString());
        invalidateLayout();

        Disconnected.getGraphicsManager().invoke(new Runnable() {

            @Override
            public void run() {

                scrollPane.setScrollPositionY(scrollPane.getMaxScrollPosY());
            }
        });
    }

    @Override
    protected void layout() {

        input.setSize(getInnerWidth(), input.getMinHeight());
        layout.adjustSize();
        scrollPane.setSize(getInnerWidth(), getInnerHeight());
    }

    /**
     * Returns true if the shell widget already stopped displaying the session's content and is closed.
     * 
     * @return True if the shell widget is already closed.
     */
    public boolean isClosed() {

        return shell == null;
    }

    /**
     * Closes the shell widget and stops displaying the session's content.
     */
    public void close() {

        if (!isClosed()) {
            shell.getHost().closeWidget(this);
            shell = null;
        }
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {

        close();
    }

}
