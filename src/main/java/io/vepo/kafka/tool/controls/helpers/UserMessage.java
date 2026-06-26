package io.vepo.kafka.tool.controls.helpers;

import javafx.stage.Stage;

/**
 * @deprecated Use {@link ViewHeader} /
 *             {@link io.vepo.kafka.tool.viewmodels.ViewMessageModel} for
 *             status. Use {@link UserConfirmation} for destructive-action
 *             prompts.
 */
@Deprecated
public final class UserMessage {

    public static boolean confirm(Stage owner, String title, String message) {
        return UserConfirmation.confirm(owner, title, message);
    }

    private UserMessage() {}

}
