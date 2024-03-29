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
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.text.TextUtils;

import org.lineageos.eleven.model.Song;
import org.lineageos.eleven.provider.LocalizedStore;
import org.lineageos.eleven.provider.LocalizedStore.SortParameter;
import org.lineageos.eleven.sectionadapter.SectionCreator;
import org.lineageos.eleven.utils.Lists;
import org.lineageos.eleven.utils.MusicUtils;
import org.lineageos.eleven.utils.PreferenceUtils;
import org.lineageos.eleven.utils.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query MediaStore.Audio.Media.EXTERNAL_CONTENT_URI and return
 * the songs on a user's device.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class SongLoader extends SectionCreator.SimpleListLoader<Song> {

    /**
     * The result
     */
    protected final ArrayList<Song> mSongList = Lists.newArrayList();

    /**
     * Additional selection filter
     */
    protected final String mSelection;

    /**
     * @param context The {@link Context} to use
     */
    public SongLoader(final Context context) {
        this(context, null);
    }

    /**
     * @param context   The {@link Context} to use
     * @param selection Additional selection filter to apply to the loader
     */
    public SongLoader(final Context context, final String selection) {
        super(context);

        mSelection = selection;
    }

    @Override
    public List<Song> loadInBackground() {
        // Create the Cursor
        Cursor cursor = getCursor();

        // Gather the data
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Copy the song Id
                final long id = cursor.getLong(0);

                // Copy the song name
                final String songName = cursor.getString(1);

                // Copy the artist name
                final String artist = cursor.getString(2);

                // Copy the album id
                final long albumId = cursor.getLong(3);

                // Copy the album name
                final String album = cursor.getString(4);

                // Copy the duration
                final long duration = cursor.getLong(5);

                // Convert the duration into seconds
                final int durationInSecs = (int) duration / 1000;

                // Copy the Year
                final int year = cursor.getInt(6);

                // Create a new song
                final Song song = new Song(id, songName, artist, album, albumId,
                        durationInSecs, year);

                if (cursor instanceof SortedCursor) {
                    song.mBucketLabel = (String) ((SortedCursor) cursor).getExtraData();
                }

                mSongList.add(song);
            } while (cursor.moveToNext());
        }
        // Close the cursor
        if (cursor != null) {
            cursor.close();
        }

        return mSongList;
    }

    /**
     * Gets the cursor for the loader - can be overriden
     *
     * @return cursor to load
     */
    protected Cursor getCursor() {
        return makeSongCursor(mContext.get(), mSelection);
    }

    /**
     * For string-based sorts, return the localized store sort parameter, otherwise return null
     *
     * @param sortOrder the song ordering preference selected by the user
     */
    private static LocalizedStore.SortParameter getSortParameter(String sortOrder) {
        switch (sortOrder) {
            case SortOrder.SongSortOrder.SONG_A_Z:
            case SortOrder.SongSortOrder.SONG_Z_A:
                return SortParameter.Song;
            case SortOrder.SongSortOrder.SONG_ALBUM:
                return SortParameter.Album;
            case SortOrder.SongSortOrder.SONG_ARTIST:
                return SortParameter.Artist;
        }

        return null;
    }

    /**
     * Creates the {@link Cursor} used to run the query.
     *
     * @param context   The {@link Context} to use.
     * @param selection Additional selection statement to use
     * @return The {@link Cursor} used to run the song query.
     */
    public static Cursor makeSongCursor(final Context context, final String selection) {
        return makeSongCursor(context, selection, true);
    }

    /**
     * Creates the {@link Cursor} used to run the query.
     *
     * @param context   The {@link Context} to use.
     * @param selection Additional selection statement to use
     * @param runSort   For localized sorts this can enable/disable the logic for running the
     *                  additional localization sort.  Queries that apply their own sorts can pass
     *                  in false for a boost in perf
     * @return The {@link Cursor} used to run the song query.
     */
    public static Cursor makeSongCursor(final Context context, final String selection,
                                        final boolean runSort) {
        String selectionStatement = MusicUtils.MUSIC_ONLY_SELECTION;
        if (!TextUtils.isEmpty(selection)) {
            selectionStatement += " AND " + selection;
        }

        final String songSortOrder = PreferenceUtils.getInstance(context).getSongSortOrder();

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                new String[]{
                        /* 0 */
                        Audio.Media._ID,
                        /* 1 */
                        Audio.Media.TITLE,
                        /* 2 */
                        Audio.Media.ARTIST,
                        /* 3 */
                        Audio.Media.ALBUM_ID,
                        /* 4 */
                        Audio.Media.ALBUM,
                        /* 5 */
                        Audio.Media.DURATION,
                        /* 6 */
                        Audio.Media.YEAR,
                }, selectionStatement, null, songSortOrder);

        // if our sort is a localized-based sort, grab localized data from the store
        final SortParameter sortParameter = getSortParameter(songSortOrder);
        if (runSort && sortParameter != null && cursor != null) {
            final boolean descending = MusicUtils.isSortOrderDesending(songSortOrder);
            return LocalizedStore.getInstance(context).getLocalizedSort(cursor, Audio.Media._ID,
                    SortParameter.Song, sortParameter, descending, TextUtils.isEmpty(selection));
        }

        return cursor;
    }
}
