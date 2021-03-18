package io.vepo.kt;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import io.vepo.kt.ScreenBuilder.ButtonGropBuilder;
import io.vepo.kt.ScreenBuilder.GridBagFormBuilder;
import io.vepo.kt.components.KafkaToolDialog;
import io.vepo.kt.settings.KafkaBroker;
import io.vepo.kt.settings.Settings;

@SuppressWarnings("serial")
public class BrokerConfigurationDialog extends KafkaToolDialog {
    private static final String[] COLUMN_NAMES = new String[] {
        "Name",
        "Bootstrap Servers",
        "Schema Registry URL" };

    public BrokerConfigurationDialog(Frame owner) {
        super("configureBrokersDialog", owner, true);
    }

    private class KafkaBrokersTableModel implements TableModel {
        private List<KafkaBroker> brokers;
        private List<TableModelListener> listeners;

        public KafkaBrokersTableModel() {
            brokers = Settings.kafka().getBrokers();
            listeners = new ArrayList<>();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            updateColumn(brokers.get(rowIndex), columnIndex, (String) aValue);
        }

        @Override
        public void removeTableModelListener(TableModelListener listener) {
            listeners.remove(listener);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return resolveColumn(brokers.get(rowIndex), columnIndex);
        }

        private void updateColumn(KafkaBroker kafkaBroker, int columnIndex, String value) {
            switch (columnIndex) {
                case 0:
                    kafkaBroker.setName(value);
                case 1:
                    kafkaBroker.setBootStrapServers(value);
                case 2:
                    kafkaBroker.setSchemaRegistryUrl(value);
                default:
                    throw new RuntimeException("What?!");
            }
        }

        private Object resolveColumn(KafkaBroker kafkaBroker, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return kafkaBroker.getName();
                case 1:
                    return kafkaBroker.getBootStrapServers();
                case 2:
                    return kafkaBroker.getSchemaRegistryUrl();
                default:
                    throw new RuntimeException("What?!");
            }
        }

        @Override
        public int getRowCount() {
            return brokers.size();
        }

        @Override
        public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public void addTableModelListener(TableModelListener listener) {
            listeners.add(listener);
        }

        public void addBroker(KafkaBroker kafkaBroker) {
            this.brokers.add(kafkaBroker);
            this.listeners.forEach(listener -> listener.tableChanged(new TableModelEvent(this)));
        }
    }

    @Override
    protected JComponent build() {
        GridBagFormBuilder builder = ScreenBuilder.grid();
        builder.newLabel("Name");
        JTextField txtName = builder.hGrow(builder.newTextField(), 1.0);
        builder.newLine();
        builder.newLabel("Bootstap Servers");
        JTextField txtBootstrapServers = builder.hGrow(builder.newTextField(), 1.0);
        builder.newLine();
        builder.newLabel("Schema Registry URL");
        JTextField txtSchemaRegistry = builder.hGrow(builder.newTextField(), 1.0);
        builder.newLine();
        KafkaBrokersTableModel dm = new KafkaBrokersTableModel();
        ButtonGropBuilder buttonGroupBuilder = builder.hGrow(builder.newButtonGroup(2), 1.0);
        JButton btnAdd = buttonGroupBuilder.newButton("Add");
        builder.newLine();
        JTable brokerTable = builder.vGrow(builder.newTable(dm, 2), 1.0);
        brokerTable.getSelectionModel().addListSelectionListener(e-> {
            System.out.println(e);
        });
        btnAdd.addActionListener(e -> {
            dm.addBroker(new KafkaBroker(txtName.getText().toString(),
                                         txtBootstrapServers.getText().trim(),
                                         txtSchemaRegistry.getText().trim()));
        });
        builder.newLine();
        return builder.build();
    }

}
