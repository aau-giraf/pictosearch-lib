/*
 * Copyright 2014 Alex Curran
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTICE: This file has been modified in order to enable custom size of the showcase and
 * custom positioning of text.
 */

package com.github.amlcurran.showcaseview.targets;

import android.app.Activity;
import android.graphics.Point;
import android.view.ViewParent;

public class ActionViewTarget extends Target {

    private final Activity mActivity;
    private final Type mType;

    ActionBarViewWrapper mActionBarWrapper;
    Reflector mReflector;

    public ActionViewTarget(Activity activity, Type type) {
        super(1.0f);
        mActivity = activity;
        mType = type;
    }

    protected void setUp() {
        mReflector = ReflectorFactory.getReflectorForActivity(mActivity);
        ViewParent p = mReflector.getActionBarView(); //ActionBarView
        mActionBarWrapper = new ActionBarViewWrapper(p);
    }

    public Target getInternalTarget()
    {
        Target internal = null;
        setUp();
        switch (mType) {

            case SPINNER:
                internal = new ViewTarget(mActionBarWrapper.getSpinnerView());
                break;

            case HOME:
                internal = new ViewTarget(mReflector.getHomeButton());
                break;

            case OVERFLOW:
                internal = new ViewTarget(mActionBarWrapper.getOverflowView());
                break;

            case TITLE:
                internal = new ViewTarget(mActionBarWrapper.getTitleView());
                break;

            case MEDIA_ROUTE_BUTTON:
                internal = new ViewTarget(mActionBarWrapper.getMediaRouterButtonView());
                break;

        }

        return internal;
    }

    @Override
    public Point getPoint() {
        Target internal = getInternalTarget();
        return internal.getPoint();
    }

    @Override
    public float getRadius() {
        Target internal = getInternalTarget();
        return internal.getRadius();
    }

    public enum Type {
        SPINNER, HOME, TITLE, OVERFLOW, MEDIA_ROUTE_BUTTON
    }
}
