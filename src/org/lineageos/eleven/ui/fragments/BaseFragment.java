/*
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2021 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lineageos.eleven.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.lineageos.eleven.MusicStateListener;
import org.lineageos.eleven.R;
import org.lineageos.eleven.ui.activities.HomeActivity;

public abstract class BaseFragment extends Fragment
        implements MusicStateListener, ISetupActionBar {

    protected ViewGroup mRootView;

    protected abstract String getTitle();

    protected abstract int getLayoutToInflate();

    protected boolean needsElevatedActionBar() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void setupActionBar() {
        final HomeActivity activity = getContainingActivity();
        activity.setupActionBar(getTitle());
        activity.setFragmentPadding(true);
        activity.setActionBarElevation(needsElevatedActionBar());
    }

    protected HomeActivity getContainingActivity() {
        return (HomeActivity) getActivity();
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        // The View for the fragment's UI
        mRootView = (ViewGroup) inflater.inflate(getLayoutToInflate(), null);
        // eat any touches that fall through to the root so they aren't
        // passed on to fragments "behind" the current one.
        mRootView.setOnTouchListener((v, me) -> true);

        setupActionBar();
        onViewCreated();

        return mRootView;
    }

    protected void onViewCreated() {
        getContainingActivity().setMusicStateListenerListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContainingActivity().removeMusicStateListenerListener(this);
    }

    @Override
    public void onMetaChanged() {
    }

    @Override
    public void onPlaylistChanged() {
    }
}
