package com.doable.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.Preferences;

public class NotificationUtil {

    private static boolean traySupported = SystemTray.isSupported();
    private static TrayIcon trayIcon;
    private static final Preferences prefs = Preferences.userNodeForPackage(NotificationUtil.class);
    private static AtomicReference<Clip> currentClip = new AtomicReference<>();
    private static Thread audioThread = null;

    static {
        if (traySupported) {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                // Create a simple empty image for icon (required on some platforms)
                BufferedImage img = new BufferedImage(1,1, BufferedImage.TYPE_INT_ARGB);
                trayIcon = new TrayIcon(img, "Doable");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            } catch (Exception e) {
                traySupported = false;
                e.printStackTrace();
            }
        }
    }

    public static void displayNotification(String title, String message) {
        playRingtone();
        
        // Show Windows system tray notification first
        if (traySupported && trayIcon != null) {
            try {
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            } catch (Exception e) {
                System.err.println("Tray notification failed: " + e.getMessage());
            }
        }
        
        // Also show JavaFX dialog on app window
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.setOnCloseRequest(e -> stopRingtone());
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    stopRingtone();
                }
            });
        });
    }
    
    public static void stopRingtone() {
        System.out.println("Stopping ringtone...");
        // Stop current clip if playing
        Clip clip = currentClip.getAndSet(null);
        if (clip != null && clip.isOpen()) {
            try {
                clip.stop();
                clip.close();
                System.out.println("Clip stopped and closed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void playRingtone() {
        String ringtone = prefs.get("ringtone", "Beep");
        System.out.println("Playing ringtone: " + ringtone);
        
        if ("None".equals(ringtone)) {
            return;
        }
        
        // Stop any currently playing ringtone
        stopRingtone();
        
        // Play sound on a separate thread to avoid blocking
        audioThread = new Thread(() -> {
            try {
                if ("Beep".equals(ringtone)) {
                    System.out.println("Playing Beep");
                    playSystemSound("Beep");
                } else if ("Chime".equals(ringtone)) {
                    System.out.println("Playing Chime");
                    playSystemSound("Chime");
                } else if ("Ding".equals(ringtone)) {
                    System.out.println("Playing Ding");
                    playSystemSound("Ding");
                } else if ("Ping".equals(ringtone)) {
                    System.out.println("Playing Ping");
                    playSystemSound("Ping");
                } else if ("Custom".equals(ringtone)) {
                    String customFile = prefs.get("customRingtoneFile", null);
                    System.out.println("Custom ringtone file: " + customFile);
                    if (customFile != null && !customFile.isEmpty()) {
                        java.io.File f = new java.io.File(customFile);
                        System.out.println("File exists: " + f.exists());
                        if (f.exists()) {
                            playAudioFile(f);
                        } else {
                            System.out.println("Custom file not found, using Beep");
                            playSystemSound("Beep");
                        }
                    } else {
                        System.out.println("No custom file set, using Beep");
                        playSystemSound("Beep"); // Fallback to beep
                    }
                }
            } catch (Exception e) {
                System.err.println("Error playing ringtone: " + e.getMessage());
                e.printStackTrace();
            }
        });
        audioThread.setName("RingtoneThread");
        audioThread.setDaemon(true);
        audioThread.start();
    }
    
    private static void playSystemSound(String soundName) {
        try {
            // Use Windows native sounds
            String soundPath;
            if ("Beep".equals(soundName)) {
                // Play using Toolkit beep - most reliable
                Toolkit.getDefaultToolkit().beep();
                Thread.sleep(100);
                Toolkit.getDefaultToolkit().beep();
                return;
            } else if ("Chime".equals(soundName)) {
                soundPath = "C:\\Windows\\Media\\chime.wav";
            } else if ("Ding".equals(soundName)) {
                soundPath = "C:\\Windows\\Media\\ding.wav";
            } else if ("Ping".equals(soundName)) {
                soundPath = "C:\\Windows\\Media\\notify.wav";
            } else {
                return;
            }
            
            // Try to play the Windows system sound file
            java.io.File audioFile = new java.io.File(soundPath);
            if (audioFile.exists()) {
                playAudioFile(audioFile);
            } else {
                // Fallback to beep
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception e) {
            // Fallback to beep if anything fails
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static void playAudioFile(java.io.File audioFile) {
        try {
            String filename = audioFile.getName().toLowerCase();
            
            if (filename.endsWith(".mp3")) {
                playMP3File(audioFile);
            } else if (filename.endsWith(".wav")) {
                playWAVFile(audioFile);
            } else {
                // Try as WAV first, then fallback
                try {
                    playWAVFile(audioFile);
                } catch (Exception e) {
                    playMP3File(audioFile);
                }
            }
        } catch (Exception e) {
            // Fallback to beep
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    private static void playWAVFile(java.io.File audioFile) {
        try {
            Clip clip = AudioSystem.getClip();
            currentClip.set(clip);
            clip.open(AudioSystem.getAudioInputStream(audioFile));
            clip.start();
            // Wait for sound to finish playing
            long duration = clip.getMicrosecondLength() / 1000;
            Thread.sleep(duration + 100);
            if (clip.isOpen()) {
                clip.close();
            }
            currentClip.set(null);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static void playMP3File(java.io.File audioFile) {
        try {
            // Try to use jLayer library for MP3 playback
            javazoom.jl.player.Player player = new javazoom.jl.player.Player(new java.io.FileInputStream(audioFile));
            player.play();
        } catch (NoClassDefFoundError e) {
            // jLayer not available, try WAV format or fallback
            try {
                playWAVFile(audioFile);
            } catch (Exception ex) {
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to beep
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
