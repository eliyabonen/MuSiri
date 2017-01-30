package com.musiri.musiri;

public class MainController
{
    private GuiButtons guiButtons;
    private AudioController audioController;

    public MainController(GuiButtons guiButtons, AudioController audioController)
    {
        this.guiButtons = guiButtons;
        this.audioController = audioController;
    }
}
