package io.vepo.kt;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public interface ScreenBuilder {

    public class GridBagFormBuilder implements ScreenBuilder {
        private JPanel controls;
        private int currentColumn;
        private int currentRow;
        private GridBagLayout layout;
        private Insets defaultInsets;

        private GridBagFormBuilder() {
            controls = new JPanel();
            controls.setBackground(Color.GREEN);
            layout = new GridBagLayout();
            controls.setLayout(layout);
            currentRow = 0;
            currentColumn = 0;
            defaultInsets = new Insets(UiConstants.PADDING / 2,
                                       UiConstants.PADDING / 2,
                                       UiConstants.PADDING / 2,
                                       UiConstants.PADDING / 2);
            controls.setBorder(new EmptyBorder(new Insets(25, 25, 25, 25)));
        }

        @Override
        public Component build() {
            return controls;
        }

        public JButton newButton(String buttonLabel) {
            return newButton(buttonLabel, 1);
        }

        public JButton newButton(String buttonLabel, int colSpan) {
            return configureComponent(new JButton(buttonLabel), colSpan, GridBagConstraints.HORIZONTAL);
        }

        public <T> JComboBox<T> newComboBox(ComboBoxModel<T> model) {
            return configureComponent(new JComboBox<T>(model), 1, GridBagConstraints.HORIZONTAL);
        }

        public JLabel newLabel(String label) {
            return configureComponent(new JLabel(label), 1, GridBagConstraints.NONE);
        }

        public JTextField newTextField() {
            return configureComponent(new JTextField(), 1, GridBagConstraints.HORIZONTAL);
        }

        public <T extends JComponent> T hGrow(T component, double value) {
            GridBagConstraints constraint = layout.getConstraints(component);
            constraint.weightx = value;
            layout.setConstraints(component, constraint);
            return component;
        }

        private <T extends JComponent> T configureComponent(T component, int colSpan, int fill) {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = currentColumn++;
            constraints.gridy = currentRow;
            constraints.insets = defaultInsets;
            constraints.fill = fill;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.gridwidth = colSpan;
            layout.setConstraints(component, constraints);
            controls.add(component);
            return component;
        }

        public void newLine() {
            currentRow++;
            currentColumn = 0;
        }

        public void skipCell() {
            currentColumn++;
        }

    }

    public static GridBagFormBuilder grid() {
        return new GridBagFormBuilder();
    }

    public Component build();

}
