package com.msfinance.pbalancer.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Function;

import com.gluonhq.charm.glisten.animation.MobileTransition;
import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.msfinance.pbalancer.App;

import javafx.fxml.FXML;
import javafx.scene.layout.Region;

public abstract class BaseController<IN,OUT>
{
    private App app;
    private View root;

    private IN in;
    private SuccessCallback<OUT> successCallback;
    private FailureCallback failureCallback;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private View view;

    private final Function<View,MobileTransition> transitionFactory;

    protected BaseController(final Function<View,MobileTransition> transitionFactory)
    {
        this.transitionFactory = transitionFactory;
    }

    public void initializeApp(final App app, final View root)
    {
        this.app = app;
        this.root = root;

        root.onHidingProperty(); // TODO
     // TODO: Is there another way to get "back" other than our app bar nav icon?
     // setOnCloseRequest ??
//             view.setOnHiding(e -> {
//                 savePortfolio();
//                 e.consume();
//             });

        // necessary for screens when coming "back" from somewhere else
        root.setOnShowing(e -> {
            updateAppBar(root.getAppManager().getAppBar());
        });

        if(transitionFactory != null)
        {
            root.setShowTransitionFactory(transitionFactory);
        }
        root.getStylesheets().add(location.toExternalForm().replace(".fxml", ".css"));
    }

    public App getApp()
    {
        return app;
    }

    public View getRoot()
    {
        return root;
    }

    public IN getIn()
    {
        return in;
    }

    public void call(final IN in, final SuccessCallback<OUT> successCallback, final FailureCallback failureCallback)
    {
        this.in = in;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;

        doSizing();
        updateAppBar(root.getAppManager().getAppBar());
        populateData(in);
    }

    protected void doSizing()
    {
        // critical to get proper scrollbar behavior on some screens - beware scrolling/resize behavior sucks
        root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }

    protected abstract void populateData(IN in);
    protected abstract void updateAppBar(AppBar appBar);


    protected void returnSuccess(final OUT out)
    {
        if(successCallback != null)
        {
            successCallback.accept(out);
        }
        app.mySwitchToPreviousView();
    }

    protected void returnFailure()
    {
        if(failureCallback != null)
        {
            failureCallback.call();
        }
        app.mySwitchToPreviousView();
    }

    @FunctionalInterface
    public static interface SuccessCallback<OUT> extends Consumer<OUT>
    {
        @Override
        public abstract void accept(OUT out);
    }
    @FunctionalInterface
    public static interface FailureCallback
    {
        public abstract void call();
    }
}
