package io.vepo.kt;

import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import io.vepo.kt.ScreenBuilder.ButtonGropBuilder;
import io.vepo.kt.ScreenBuilder.GridBagFormBuilder;
import io.vepo.kt.components.ButtonColumn;
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

    private class KafkaBrokersTableModel extends AbstractTableModel {
        private List<KafkaBroker> brokers;

        public KafkaBrokersTableModel(JTable table) {
            brokers = Settings.kafka().getBrokers();
            ButtonColumn buttonColumn = new ButtonColumn(table, e -> {
                int row = Integer.valueOf(e.getActionCommand());
                brokers.remove(row);
                fireTableRowsDeleted(row, row);

            }, 3);
            buttonColumn.setMnemonic(KeyEvent.VK_D);

            table.setModel(this);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            updateColumn(brokers.get(rowIndex), columnIndex, (String) aValue);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return resolveColumn(brokers.get(rowIndex), columnIndex);
        }

        private void updateColumn(KafkaBroker kafkaBroker, int columnIndex, String value) {
            switch (columnIndex) {
                case 0:
                    kafkaBroker.setName(value);
                    break;
                case 1:
                    kafkaBroker.setBootStrapServers(value);
                    break;
                case 2:
                    kafkaBroker.setSchemaRegistryUrl(value);
                    break;
                case 3:
                    break;
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
                case 3:
                    return "Delete";
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
            if (columnIndex == COLUMN_NAMES.length) {
                return "Actions";
            } else {
                return COLUMN_NAMES[columnIndex];
            }
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length + 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == COLUMN_NAMES.length) {
                return ActionListener.class;
            } else {
                return String.class;
            }
        }

        public void addBroker(KafkaBroker kafkaBroker) {
            brokers.add(kafkaBroker);
            fireTableChanged(new TableModelEvent(this));
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
        ButtonGropBuilder buttonGroupBuilder = builder.hGrow(builder.newButtonGroup(2), 1.0);
        JButton btnAdd = buttonGroupBuilder.newButton("Add");
        builder.newLine();
        JTable brokerTable = builder.vGrow(builder.newTable(2), 1.0);
        KafkaBrokersTableModel dm = new KafkaBrokersTableModel(brokerTable);
        brokerTable.getSelectionModel().addListSelectionListener(e -> {
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
