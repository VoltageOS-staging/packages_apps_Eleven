/*
 * Copyright (C) 2012 Andrew Neal
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
package org.lineageos.eleven.loaders;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;

import org.lineageos.eleven.Config.SmartPlaylistType;
import org.lineageos.eleven.model.Playlist;
import org.lineageos.eleven.utils.Lists;
import org.lineageos.eleven.utils.MusicUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI and
 * return the playlists on a user's device.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class PlaylistLoader extends WrappedAsyncTaskLoader<List<Playlist>> {

    /**
     * The result
     */
    private final ArrayList<Playlist> mPlaylistList = Lists.newArrayList();

    /**
     * Constructor of <code>PlaylistLoader</code>
     *
     * @param context The {@link Context} to use
     */
    public PlaylistLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Playlist> loadInBackground() {
        // Add the default playlist to the adapter
        makeDefaultPlaylists();

        // Create the Cursor
        Cursor cursor = makePlaylistCursor(getContext());
        // Gather the data
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Copy the playlist id
                final long id = cursor.getLong(0);

                // Copy the playlist name
                final String name = cursor.getString(1);

                final int songCount = MusicUtils.getSongCountForPlaylist(getContext(), id);

                // Create a new playlist
                final Playlist playlist = new Playlist(id, name, songCount);

                // Add everything up
                mPlaylistList.add(playlist);
            } while (cursor.moveToNext());
        }
        // Close the cursor
        if (cursor != null) {
            cursor.close();
        }
        return mPlaylistList;
    }

    /* Adds the favorites and last added playlists */
    private void makeDefaultPlaylists() {
        final Resources resources = getContext().getResources();

        /* Last added list */
        final Playlist lastAdded = new Playlist(SmartPlaylistType.LastAdded.mId,
                resources.getString(SmartPlaylistType.LastAdded.mTitleId), -1);
        mPlaylistList.add(lastAdded);

        /* Recently Played */
        final Playlist recentlyPlayed = new Playlist(SmartPlaylistType.RecentlyPlayed.mId,
                resources.getString(SmartPlaylistType.RecentlyPlayed.mTitleId), -1);
        mPlaylistList.add(recentlyPlayed);

        /* Top Tracks */
        final Playlist topTracks = new Playlist(SmartPlaylistType.TopTracks.mId,
                resources.getString(SmartPlaylistType.TopTracks.mTitleId), -1);
        mPlaylistList.add(topTracks);
    }

    /**
     * Creates the {@link Cursor} used to run the query.
     *
     * @param context The {@link Context} to use.
     * @return The {@link Cursor} used to run the playlist query.
     */
    public static Cursor makePlaylistCursor(final Context context) {
        return context.getContentResolver().query(
                MediaStore.Audio.Playlists.getContentUri(MediaStore.VOLUME_EXTERNAL),
                new String[]{
                        /* 0 */
                        BaseColumns._ID,
                        /* 1 */
                        PlaylistsColumns.NAME
                }, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }
}
