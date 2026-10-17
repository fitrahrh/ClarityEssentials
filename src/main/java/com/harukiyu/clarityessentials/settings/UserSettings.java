package com.harukiyu.clarityessentials.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSettings {

    private final UUID uuid;

    // Chat Settings (image copy 10.png)
    private boolean publicChat = true;
    private String privateMessages = "Anyone"; // Anyone, Friends, Off
    private boolean serverChatMessages = true;
    private boolean serverHotbarMessages = true;
    private String deathMessages = "Anyone"; // Anyone, Friends, Off
    private String advancementMessages = "Friends"; // Anyone, Friends, Off
    private String joinLeaveMessages = "Friends"; // Anyone, Friends, Off

    // Privacy Settings (image copy 11.png)
    private String teleportRequests = "Anyone"; // Anyone, Friends, Off
    private String teleportHereRequests = "Anyone"; // Anyone, Friends, Off
    private String allowPayments = "Anyone"; // Anyone, Friends, Off
    private boolean randomizedCoords = true;
    private boolean privateTransactions = false;

    // Notification Settings (image copy 12.png)
    private boolean payAlerts = true;
    private boolean teleportAlerts = true;
    private boolean bountyAlerts = true;
    private boolean auctionAlerts = true;
    private boolean orderAlerts = true;
    private boolean serverSounds = true;
    private boolean followAlerts = true;

    // Teleport List (image copy 14.png)
    private final List<String> teleportList = new ArrayList<>();

    // Pay List
    private final List<String> payList = new ArrayList<>();

    // Friends and Following (image copy 16.png)
    private final List<String> friendsList = new ArrayList<>();
    private final List<String> followingList = new ArrayList<>();

    public UserSettings(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isPublicChat() {
        return publicChat;
    }

    public void setPublicChat(boolean publicChat) {
        this.publicChat = publicChat;
    }

    public String getPrivateMessages() {
        return privateMessages;
    }

    public void setPrivateMessages(String privateMessages) {
        this.privateMessages = privateMessages;
    }

    public boolean isServerChatMessages() {
        return serverChatMessages;
    }

    public void setServerChatMessages(boolean serverChatMessages) {
        this.serverChatMessages = serverChatMessages;
    }

    public boolean isServerHotbarMessages() {
        return serverHotbarMessages;
    }

    public void setServerHotbarMessages(boolean serverHotbarMessages) {
        this.serverHotbarMessages = serverHotbarMessages;
    }

    public String getDeathMessages() {
        return deathMessages;
    }

    public void setDeathMessages(String deathMessages) {
        this.deathMessages = deathMessages;
    }

    public String getAdvancementMessages() {
        return advancementMessages;
    }

    public void setAdvancementMessages(String advancementMessages) {
        this.advancementMessages = advancementMessages;
    }

    public String getJoinLeaveMessages() {
        return joinLeaveMessages;
    }

    public void setJoinLeaveMessages(String joinLeaveMessages) {
        this.joinLeaveMessages = joinLeaveMessages;
    }

    public String getTeleportRequests() {
        return teleportRequests;
    }

    public void setTeleportRequests(String teleportRequests) {
        this.teleportRequests = teleportRequests;
    }

    public String getTeleportHereRequests() {
        return teleportHereRequests;
    }

    public void setTeleportHereRequests(String teleportHereRequests) {
        this.teleportHereRequests = teleportHereRequests;
    }

    public String getAllowPayments() {
        return allowPayments;
    }

    public void setAllowPayments(String allowPayments) {
        this.allowPayments = allowPayments;
    }

    public boolean isRandomizedCoords() {
        return randomizedCoords;
    }

    public void setRandomizedCoords(boolean randomizedCoords) {
        this.randomizedCoords = randomizedCoords;
    }

    public boolean isPrivateTransactions() {
        return privateTransactions;
    }

    public void setPrivateTransactions(boolean privateTransactions) {
        this.privateTransactions = privateTransactions;
    }

    public boolean isPayAlerts() {
        return payAlerts;
    }

    public void setPayAlerts(boolean payAlerts) {
        this.payAlerts = payAlerts;
    }

    public boolean isTeleportAlerts() {
        return teleportAlerts;
    }

    public void setTeleportAlerts(boolean teleportAlerts) {
        this.teleportAlerts = teleportAlerts;
    }

    public boolean isBountyAlerts() {
        return bountyAlerts;
    }

    public void setBountyAlerts(boolean bountyAlerts) {
        this.bountyAlerts = bountyAlerts;
    }

    public boolean isAuctionAlerts() {
        return auctionAlerts;
    }

    public void setAuctionAlerts(boolean auctionAlerts) {
        this.auctionAlerts = auctionAlerts;
    }

    public boolean isOrderAlerts() {
        return orderAlerts;
    }

    public void setOrderAlerts(boolean orderAlerts) {
        this.orderAlerts = orderAlerts;
    }

    public boolean isServerSounds() {
        return serverSounds;
    }

    public void setServerSounds(boolean serverSounds) {
        this.serverSounds = serverSounds;
    }

    public boolean isFollowAlerts() {
        return followAlerts;
    }

    public void setFollowAlerts(boolean followAlerts) {
        this.followAlerts = followAlerts;
    }

    public List<String> getTeleportList() {
        return teleportList;
    }

    public void addTeleportPlayer(String name) {
        if (!teleportList.contains(name)) {
            teleportList.add(name);
        }
    }

    public void removeTeleportPlayer(String name) {
        teleportList.removeIf(p -> p.equalsIgnoreCase(name));
    }

    public List<String> getPayList() {
        return payList;
    }

    public void addPayPlayer(String name) {
        if (!payList.contains(name)) {
            payList.add(name);
        }
    }

    public void removePayPlayer(String name) {
        payList.removeIf(p -> p.equalsIgnoreCase(name));
    }

    public boolean isPayPlayer(String name) {
        return payList.stream().anyMatch(p -> p.equalsIgnoreCase(name));
    }

    public List<String> getFriendsList() {
        return friendsList;
    }

    public void addFriend(String name) {
        if (!friendsList.contains(name)) {
            friendsList.add(name);
        }
    }

    public void removeFriend(String name) {
        friendsList.removeIf(f -> f.equalsIgnoreCase(name));
    }

    public boolean isFriend(String name) {
        return friendsList.stream().anyMatch(f -> f.equalsIgnoreCase(name));
    }

    public List<String> getFollowingList() {
        return followingList;
    }

    public void addFollowing(String name) {
        if (!followingList.contains(name)) {
            followingList.add(name);
        }
    }

    public void removeFollowing(String name) {
        followingList.removeIf(f -> f.equalsIgnoreCase(name));
    }

    public boolean isFollowing(String name) {
        return followingList.stream().anyMatch(f -> f.equalsIgnoreCase(name));
    }
}
