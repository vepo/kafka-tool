package io.vepo.kafka.tool.controllers;

import static javafx.application.Platform.runLater;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.kafka.tool.inspect.ConsumerGroupMemberInfo;
import io.vepo.kafka.tool.inspect.ConsumerGroupSummary;
import io.vepo.kafka.tool.inspect.KafkaAdminService;
import io.vepo.kafka.tool.inspect.PartitionLagRow;
import io.vepo.kafka.tool.viewmodels.ViewMessageModel;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

public class ConsumerGroupsController {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerGroupsController.class);

    private final KafkaAdminService adminService;
    private final ObservableList<ConsumerGroupSummary> groups = observableArrayList();
    private final ObservableList<ConsumerGroupMemberInfo> members = observableArrayList();
    private final ObservableList<PartitionLagRow> lagRows = observableArrayList();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final ViewMessageModel viewMessage = new ViewMessageModel();
    private final BooleanProperty autoRefresh = new SimpleBooleanProperty(false);
    private ScheduledExecutorService refreshScheduler;
    private String selectedGroupId;

    public ConsumerGroupsController(KafkaAdminService adminService) {
        this.adminService = adminService;
    }

    public BooleanProperty autoRefreshProperty() {
        return autoRefresh;
    }

    public ObservableList<ConsumerGroupSummary> getGroups() {
        return groups;
    }

    public ObservableList<PartitionLagRow> getLagRows() {
        return lagRows;
    }

    public ObservableList<ConsumerGroupMemberInfo> getMembers() {
        return members;
    }

    public void loadGroupDetails(String groupId) {
        runLater(() -> loading.set(true));
        adminService.describeConsumerGroupMembers(groupId, memberList -> adminService.computeConsumerGroupLag(groupId,
                                                                                                              rows -> runLater(() -> {
                                                                                                                  members.setAll(memberList);
                                                                                                                  lagRows.setAll(rows);
                                                                                                                  long totalLag =
                                                                                                                          rows.stream()
                                                                                                                              .mapToLong(PartitionLagRow::lag)
                                                                                                                              .sum();
                                                                                                                  loading.set(false);
                                                                                                                  viewMessage.showInfo("Group " + groupId
                                                                                                                          + ": total lag " + totalLag);
                                                                                                              })));
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void refreshGroups() {
        runLater(() -> loading.set(true));
        adminService.listConsumerGroups(groupList -> runLater(() -> {
            groups.setAll(groupList);
            loading.set(false);
            viewMessage.showSuccess("Found " + groupList.size() + " consumer group(s).");
        }));
    }

    public void selectGroup(ConsumerGroupSummary group) {
        if (group == null) {
            selectedGroupId = null;
            runLater(() -> {
                members.clear();
                lagRows.clear();
            });
            return;
        }
        selectedGroupId = group.groupId();
        loadGroupDetails(group.groupId());
    }

    public void setAutoRefresh(boolean enabled) {
        autoRefresh.set(enabled);
        if (enabled) {
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    }

    public void shutdown() {
        stopAutoRefresh();
    }

    private void startAutoRefresh() {
        if (refreshScheduler == null) {
            refreshScheduler = Executors.newSingleThreadScheduledExecutor();
        }
        refreshScheduler.scheduleAtFixedRate(() -> {
            refreshGroups();
            if (selectedGroupId != null) {
                loadGroupDetails(selectedGroupId);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void stopAutoRefresh() {
        if (refreshScheduler != null) {
            refreshScheduler.shutdownNow();
            refreshScheduler = null;
        }
    }

    public ViewMessageModel viewMessage() {
        return viewMessage;
    }

}
