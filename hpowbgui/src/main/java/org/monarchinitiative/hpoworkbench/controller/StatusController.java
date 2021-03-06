package org.monarchinitiative.hpoworkbench.controller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.monarchinitiative.hpoworkbench.resources.OptionalResources;

import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * This class is a controller of the bottom part of the main dialog window. Status messages are displayed here, as
 * well as the copyright.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.1.10
 * @since 0.1
 */
public final class StatusController {

    private static final int MAX_MESSAGES = 1;

    private final OptionalResources optionalResources;

    private final ResourceBundle resourceBundle;

    @FXML
    public Label copyrightLabel;

    @FXML
    public HBox statusHBox;

    @Inject
    StatusController(OptionalResources optionalResources, ResourceBundle resourceBundle) {
        this.optionalResources = optionalResources;
        this.resourceBundle = resourceBundle;
    }

    /**
     * This method is run after FXMLLoader injected all FXML elements.
     */
    public void initialize() {
        String ver = MainController.getVersion();
        copyrightLabel.setText("HPO Workbench, v. " + ver + ", \u00A9 Monarch Initiative 2018");

        ChangeListener<? super Object> listener = (obs, oldval, newval) -> checkAll();

        optionalResources.ontologyProperty().addListener(listener);
        optionalResources.indirectAnnotMapProperty().addListener(listener);
        optionalResources.directAnnotMapProperty().addListener(listener);

        checkAll();
    }

    /**
     * Check availability of tracked resources and publish an appropriate message.
     */
    private void checkAll() {
        if (optionalResources.getOntology() == null) { // hpo obo file is missing
            publishMessage(resourceBundle.getString("status.download.hpo"), MessageType.ERROR);
        } else if (optionalResources.getDirectAnnotMap() == null || // annotations file is missing
                optionalResources.getIndirectAnnotMap() == null) {
            publishMessage(resourceBundle.getString("status.download.annotations"), MessageType.ERROR);
        } else { // since we check only 2 resources, we should be fine here
            publishMessage(resourceBundle.getString("status.all.set"), MessageType.INFO);
        }
    }

    /**
     * Post information message to the status bar.
     *
     * @param msg String with message to be displayed
     */
    void publishMessage(String msg) {
        publishMessage(msg, MessageType.INFO);
    }

    /**
     * Post the message to the status bar. Color of the text is determined by the message <code>type</code>.
     *
     * @param msg  String with message to be displayed
     * @param type message type
     */
    void publishMessage(String msg, MessageType type) {
        if (statusHBox.getChildren().size() == MAX_MESSAGES) {
            statusHBox.getChildren().remove(MAX_MESSAGES - 1);
        }
        Label label = prepareContainer(type);
        label.setText(msg);
        statusHBox.getChildren().add(0, label);
    }

    /**
     * Make label for displaying message in the {@link #statusHBox}. The style of the text depends on given
     * <code>type</code>
     *
     * @param type of the message to be displayed
     *
     * @return {@link Label} styled according to the message type
     */
    private Label prepareContainer(MessageType type) {
        Label label = new Label();
        label.setPrefHeight(30);
        HBox.setHgrow(label, Priority.ALWAYS);
        label.setPadding(new Insets(5));
        switch (type) {
            case WARNING:
                label.setStyle("-fx-text-fill: orange; -fx-font-weight: bolder");
                break;
            case ERROR:
                label.setStyle("-fx-text-fill: red; -fx-font-weight: bolder");
                break;
            case INFO:
            default:
                label.setStyle("-fx-text-fill: black; -fx-font-weight: bolder");
                break;
        }


        return label;
    }

    enum MessageType {INFO, WARNING, ERROR}
}
