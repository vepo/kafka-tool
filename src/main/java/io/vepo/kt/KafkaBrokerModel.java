package io.vepo.kt;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import io.vepo.kt.settings.KafkaBroker;
import io.vepo.kt.settings.KafkaSettings;
import io.vepo.kt.settings.Settings;

public class KafkaBrokerModel implements ComboBoxModel<KafkaBroker> {

    private KafkaSettings kafkaSettings;
    private int selectedIndex;

    public KafkaBrokerModel() {
        kafkaSettings = Settings.kafka();
        selectedIndex = -1;
    }

    @Override
    public int getSize() {
        return kafkaSettings.getBrokers().size();
    }

    @Override
    public KafkaBroker getElementAt(int index) {
        return kafkaSettings.getBrokers().get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {

    }

    @Override
    public void removeListDataListener(ListDataListener l) {

    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedIndex = kafkaSettings.getBrokers().indexOf(anItem);
    }

    @Override
    public Object getSelectedItem() {
        if (selectedIndex >= 0) {
            return kafkaSettings.getBrokers().get(selectedIndex);
        } else {
            return null;
        }
    }

}
