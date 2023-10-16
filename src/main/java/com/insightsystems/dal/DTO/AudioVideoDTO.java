package com.insightsystems.dal.DTO;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class AudioVideoDTO {
    @Nullable
    public Integer autoPairingVolume;
    @Nullable
    public String cameraAntiBanding;
    @Nullable
    public Boolean deepNoiseSuppressionEnabled;
    @Nullable
    public Boolean musicModeEnabled;
    @Nullable
    public Boolean usbAudioDevicesEnabled;

    public AudioVideoDTO() {
    }

    public AudioVideoDTO(int autoPairingVolume) {
        this.autoPairingVolume = autoPairingVolume;
    }
    public AudioVideoDTO(@NonNull String cameraAntiBanding) {
        this.cameraAntiBanding = cameraAntiBanding;
    }
    public AudioVideoDTO(boolean musicModeEnabled) {
        this.musicModeEnabled = musicModeEnabled;
    }
}
