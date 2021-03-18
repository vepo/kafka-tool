package io.vepo.kt;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableModel;

public interface ScreenBuilder {

    public class ButtonGropBuilder implements ScreenBuilder {
        private JPanel panel;

        public ButtonGropBuilder(Consumer<JComponent> buildCallback) {
            panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            buildCallback.accept(panel);
        }

        @Override
        public JComponent build() {
            return panel;
        }

        public JButton newButton(String buttonLabel) {
            JButton btn = new JButton(buttonLabel);
            panel.add(btn);
            return btn;
        }

    }

    public class GridBagFormBuilder implements ScreenBuilder {
        private JPanel controls;
        private int currentColumn;
        private int currentRow;
        private Insets defaultInsets;
        private GridBagLayout layout;

        private GridBagFormBuilder() {
            controls = new JPanel();
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
        public JComponent build() {
            return controls;
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

        public <T extends JComponent> T hGrow(T component, double value) {
            GridBagConstraints constraint = layout.getConstraints(component);
            constraint.weightx = value;
            layout.setConstraints(component, constraint);
            return component;
        }

        public <T extends ScreenBuilder> T hGrow(T builder, double value) {
            hGrow(builder.build(), value);
            return builder;
        }

        public JButton newButton(String buttonLabel) {
            return newButton(buttonLabel, 1);
        }

        public JButton newButton(String buttonLabel, int colSpan) {
            return configureComponent(new JButton(buttonLabel), colSpan, GridBagConstraints.HORIZONTAL);
        }

        public ButtonGropBuilder newButtonGroup() {
            return newButtonGroup(1);
        }

        public ButtonGropBuilder newButtonGroup(int colSpan) {
            return new ButtonGropBuilder(component -> configureComponent(component, colSpan,
                                                                         GridBagConstraints.HORIZONTAL));
        }

        public <T> JComboBox<T> newComboBox(ComboBoxModel<T> model) {
            return configureComponent(new JComboBox<T>(model), 1, GridBagConstraints.HORIZONTAL);
        }

        public JLabel newLabel(String label) {
            return configureComponent(new JLabel(label), 1, GridBagConstraints.NONE);
        }

        public void newLine() {
            currentRow++;
            currentColumn = 0;
        }

        public JTable newTable(TableModel dm) {
            return newTable(dm, 1);
        }

        public JTable newTable(TableModel dm, int colSpan) {
            JTable table = new JTable(dm);
            configureComponent(new JScrollPane(table), colSpan, GridBagConstraints.HORIZONTAL);
            return table;
        }

        public JTextField newTextField() {
            return configureComponent(new JTextField(), 1, GridBagConstraints.HORIZONTAL);
        }

        public void skipCell() {
            currentColumn++;
        }

        public <T extends JComponent> T vGrow(T component, double value) {
            GridBagConstraints constraint = layout.getConstraints(component);
            constraint.weighty = value;
            layout.setConstraints(component, constraint);
            return component;
        }

    }

    public static GridBagFormBuilder grid() {
        return new GridBagFormBuilder();
    }

    public JComponent build();

}
