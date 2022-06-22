package com.msfinance.pbalancer.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.animation.BounceInRightTransition;
import com.gluonhq.charm.glisten.control.Alert;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import com.msfinance.pbalancer.App;
import com.msfinance.pbalancer.PersistManager;
import com.msfinance.pbalancer.controllers.cells.ProfileListCell;
import com.msfinance.pbalancer.model.Profile;
import com.msfinance.pbalancer.model.ProfileSettings;
import com.msfinance.pbalancer.service.DataFactory;
import com.msfinance.pbalancer.service.ProfileDataCache;
import com.msfinance.pbalancer.util.NumberFormatHelper;
import com.msfinance.pbalancer.util.Validation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class SettingsController extends BaseController<Void,Void>
{
    private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

    public static final String APP_BAR_TITLE = "Settings";

    @FXML
    private Button addProfileBtn;

    @FXML
    private Button deleteProfileBtn;

    @FXML
    private ComboBox<Profile> profileComboBox;

    @FXML
    private Slider rebalBandAbsoluteSlider;

    @FXML
    private Slider rebalBandRelativeSlider;

    @FXML
    private ComboBox<Integer> rebalCheckDaysComboBox;

    @FXML
    private TextField rebalMinimumDollarsText;

    @FXML
    private ComboBox<Integer> assetPricingAgeWarningDaysComboBox;


    public SettingsController()
    {
        super(BounceInRightTransition::new);
    }

    @FXML
    void initialize() throws IOException
    {
        Validation.assertNonNull(addProfileBtn);
        Validation.assertNonNull(deleteProfileBtn);
        Validation.assertNonNull(profileComboBox);
        Validation.assertNonNull(rebalBandAbsoluteSlider);
        Validation.assertNonNull(rebalBandRelativeSlider);
        Validation.assertNonNull(rebalCheckDaysComboBox);
        Validation.assertNonNull(rebalMinimumDollarsText);
        Validation.assertNonNull(assetPricingAgeWarningDaysComboBox);

        profileComboBox.setOnAction(e -> onProfileChange());

        rebalCheckDaysComboBox.getItems().setAll(1, 30, 90, 365);
        assetPricingAgeWarningDaysComboBox.getItems().setAll(7, 30, 60, 90, 180, 365);

        addProfileBtn.setOnAction(e -> onAddProfile());
        deleteProfileBtn.setOnAction(e -> onDeleteProfile());
        addProfileBtn.setGraphic(MaterialDesignIcon.ADD.graphic());
        deleteProfileBtn.setGraphic(MaterialDesignIcon.DELETE.graphic());
    }

    @Override
    public void initializeApp(final App app, final View root)
    {
        super.initializeApp(app, root);

        // UGGG: major flaw, this never gets called from drawer, or on first init
        getRoot().setOnShowing(e -> {
            call(null, null, null);
        });
        getRoot().setOnHiding(e -> {
            save();
        });
    }

//    @Override
//    protected void doSizing()
//    {
//        super.doSizing();
//        // critical to get proper scrollbar behavior
//        getRoot().setMinSize(100, 100);
//        getRoot().setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
//        ((AnchorPane)t.getParent()).setPrefSize(20, 20);
//        t.setPrefSize(20, 20);
//    }

    @Override
    protected void populateData(final Void voidObj)
    {
        Profile p = ProfileDataCache.get().getProfile();
        try
        {
            List<Profile> profiles = DataFactory.get().listProfiles();
            profileComboBox.getItems().setAll(profiles);
            profileComboBox.setButtonCell(new ProfileListCell());
            profileComboBox.setCellFactory(new ProfileListCell.Factory());

            // select the appropriate item - may be different object instances
            for(int i=0; i<profileComboBox.getItems().size(); i++)
            {
                if(profileComboBox.getItems().get(i).getId().equals(p.getId()))
                {
                    profileComboBox.getSelectionModel().select(i);
                }
            }
        }
        catch (IOException e)
        {
            profileComboBox.getItems().clear();
            getApp().showMessage("Unable to list profiles");
            LOG.error("Unable to list profiles", e);
        }

        populateSettings(p.getSettings());
    }

    private void populateSettings(final ProfileSettings profileSettings)
    {
        ProfileSettings settings = ProfileDataCache.get().getProfile().getSettings();
        rebalCheckDaysComboBox.getSelectionModel().select((Integer)settings.getRebalanceCheckIntervalDays());
        rebalBandAbsoluteSlider.setValue(100.0*settings.getRebalanceToleranceBandAbsolute());
        rebalBandRelativeSlider.setValue(100.0*settings.getRebalanceToleranceBandRelative());
        rebalMinimumDollarsText.setText( NumberFormatHelper.formatWith2Decimals(settings.getRebalanceMinimumDollars()) );
        assetPricingAgeWarningDaysComboBox.getSelectionModel().select((Integer)settings.getAssetPricingAgeWarningDays());
    }

    private boolean save()
    {
        Profile profile = ProfileDataCache.get().getProfile();
        ProfileSettings settings = profile.getSettings();
        settings.setRebalanceCheckIntervalDays(rebalCheckDaysComboBox.getSelectionModel().getSelectedItem());
        settings.setRebalanceToleranceBandAbsolute(rebalBandAbsoluteSlider.getValue()/100.0);
        settings.setRebalanceToleranceBandRelative(rebalBandRelativeSlider.getValue()/100.0);
        settings.setRebalanceMinimumDollars( NumberFormatHelper.parseNumber2(rebalMinimumDollarsText.getText()) );
        settings.setAssetPricingAgeWarningDays(assetPricingAgeWarningDaysComboBox.getSelectionModel().getSelectedItem());
        settings.markDirty();

        try
        {
            boolean saved = PersistManager.persistAll(profile);
            if(saved)
            {
                getApp().showMessage("Saved profile settings");
            }
        }
        catch (IOException e)
        {
            LOG.error("Error updating profile settings: " + profile.getId(), e);
            getApp().showMessage("Error updating profile settings");
            return false;
        }
        return true;
    }


    @Override
    protected void updateAppBar(final AppBar appBar)
    {
        appBar.setNavIcon(MaterialDesignIcon.MENU.button(e -> getApp().showDrawer()));
        appBar.getActionItems().clear();
        appBar.setTitleText(APP_BAR_TITLE);
    }

    private void onProfileChange()
    {
        save();

        Profile profile = profileComboBox.getSelectionModel().getSelectedItem();
        try
        {
            ProfileDataCache.switchProfile(profile.getId());
            // update settings based on new profile
            populateSettings(ProfileDataCache.get().getProfile().getSettings());
        }
        catch (IOException e)
        {
            getApp().showMessage("Unable to switch profile to: " + profile.getName());
            LOG.error("Unable to switch profile to: " + profile.getName(), e);
        }
    }

    private void onAddProfile()
    {
        // TODO: implement
        getApp().showMessage("Profile creation not yet implemented");
    }

    private void onDeleteProfile()
    {
        Profile profile = profileComboBox.getSelectionModel().getSelectedItem();
        if(profile.getId().equals(Profile.SAMPLE))
        {
            getApp().showMessage("Cannot delete SAMPLE profile");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this profile?\nProfile Name = " + profile.getName());
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK)
        {
            // TODO: implement
            getApp().showMessage("Profile deletion not yet implemented");
        }
    }

}
