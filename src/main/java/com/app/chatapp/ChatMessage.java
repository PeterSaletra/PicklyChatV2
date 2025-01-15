package com.app.chatapp;

import javafx.scene.control.Label;

public record ChatMessage(Label message, boolean isReceived) { }