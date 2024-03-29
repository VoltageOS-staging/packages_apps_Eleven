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
package org.lineageos.eleven.ui.fragments.phone;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.lineageos.eleven.R;
import org.lineageos.eleven.adapters.PagerAdapter;
import org.lineageos.eleven.adapters.PagerAdapter.MusicFragments;
import org.lineageos.eleven.menu.CreateNewPlaylist;
import org.lineageos.eleven.slidinguppanel.SlidingUpPanelLayout;
import org.lineageos.eleven.ui.activities.SlidingPanelActivity;
import org.lineageos.eleven.ui.fragments.AlbumFragment;
import org.lineageos.eleven.ui.fragments.ArtistFragment;
import org.lineageos.eleven.ui.fragments.BaseFragment;
import org.lineageos.eleven.ui.fragments.SongFragment;
import org.lineageos.eleven.utils.MusicUtils;
import org.lineageos.eleven.utils.PreferenceUtils;
import org.lineageos.eleven.utils.SortOrder;

/**
 * This class is used to hold the {@link ViewPager} used for swiping between the
 * playlists, recent, artists, albums, songs, and genre {@link Fragment}
 * s for phones.
 * <p>
 * NOTE: The reason the sort orders are taken care of in this fragment rather
 * than the individual fragments is to keep from showing all of the menu
 * items on tablet interfaces.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class MusicBrowserPhoneFragment extends BaseFragment {
    public static final int INVALID_PAGE_INDEX = -1;

    /**
     * Pager
     */
    private ViewPager mViewPager;

    /**
     * Navigation bar
     */
    private BottomNavigationView mBottomNavigation;

    /**
     * VP's adapter
     */
    private PagerAdapter mPagerAdapter;

    private PreferenceUtils mPreferences;

    /**
     * A pre-defined page index to navigate to
     */
    private int mDefaultPageIdx = INVALID_PAGE_INDEX;

    /**
     * Empty constructor as per the {@link Fragment} documentation
     */
    public MusicBrowserPhoneFragment() {
    }

    @Override
    protected int getLayoutToInflate() {
        return R.layout.fragment_music_browser_phone;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.app_name);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the preferences
        mPreferences = PreferenceUtils.getInstance(getActivity());
    }

    @Override
    protected void onViewCreated() {
        super.onViewCreated();

        // create the adapter - on rotation the view gets created again and we need to recreate
        // the child fragments (fragments of fragments cannot be retained)
        mPagerAdapter = new PagerAdapter(getActivity(), getChildFragmentManager());
        final MusicFragments[] mFragments = MusicFragments.values();
        for (final MusicFragments mFragment : mFragments) {
            mPagerAdapter.add(mFragment.getFragmentClass(), null);
        }

        // Initialize the ViewPager
        mViewPager = mRootView.findViewById(R.id.fragment_home_phone_pager);
        // Attach the adapter
        mViewPager.setAdapter(mPagerAdapter);
        // Offscreen pager loading limit
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount() - 1);

        // Initialize the navigation bar
        mBottomNavigation = getContainingActivity().findViewById(R.id.fragment_home_phone_pager_titles);
        mBottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Pop all stacks if we navigate elsewhere and reset the panel
            for (int i = 0; i < getParentFragmentManager().getBackStackEntryCount(); i++) {
                getParentFragmentManager().popBackStack();
            }
            ((SlidingPanelActivity) getContainingActivity())
                    .showPanel(SlidingPanelActivity.Panel.Browse);


            if (id == R.id.nav_artist) {
                mViewPager.setCurrentItem(0);
            }
            else if (id == R.id.nav_album) {
                mViewPager.setCurrentItem(1);
            }
            else if (id == R.id.nav_songs) {
                mViewPager.setCurrentItem(2);
            }
            else if (id == R.id.nav_playist) {
                mViewPager.setCurrentItem(3);
            }
            return true;
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mBottomNavigation.getMenu().findItem(R.id.nav_artist).setChecked(true);
                        break;
                    case 1:
                        mBottomNavigation.getMenu().findItem(R.id.nav_album).setChecked(true);
                        break;
                    case 2:
                        mBottomNavigation.getMenu().findItem(R.id.nav_songs).setChecked(true);
                        break;
                    case 3:
                        mBottomNavigation.getMenu().findItem(R.id.nav_playist).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (mDefaultPageIdx != INVALID_PAGE_INDEX) {
            navigateToPage(mDefaultPageIdx);
        } else {
            // Start on the last page the user was on
            navigateToPage(mPreferences.getStartPage());
        }

        // Enable the options menu
        setHasOptionsMenu(true);
    }

    public void setDefaultPageIdx(final int pageIdx) {
        mDefaultPageIdx = pageIdx;
        navigateToPage(mDefaultPageIdx);
    }

    private void navigateToPage(final int idx) {
        // this may be called before the view is created, so do a check for mViewPager
        if (idx != INVALID_PAGE_INDEX && mViewPager != null) {
            mViewPager.setCurrentItem(idx);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Save the last page the use was on
        mPreferences.setStartPage(mViewPager.getCurrentItem());
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu,
                                    @NonNull final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.shuffle_all, menu);
        if (isArtistPage()) {
            inflater.inflate(R.menu.artist_sort_by, menu);
        } else if (isAlbumPage()) {
            inflater.inflate(R.menu.album_sort_by, menu);
        } else if (isSongPage()) {
            inflater.inflate(R.menu.song_sort_by, menu);
        } else if (isPlaylistPage()) {
            inflater.inflate(R.menu.new_playlist, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.menu_shuffle_all) {
            // Shuffle all the songs
            MusicUtils.shuffleAll(getActivity());
        } else if (id == R.id.menu_sort_by_az) {
            if (isArtistPage()) {
                mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_A_Z);
                getArtistFragment().refresh();
            } else if (isAlbumPage()) {
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                getAlbumFragment().refresh();
            } else if (isSongPage()) {
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                getSongFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_za) {
            if (isArtistPage()) {
                mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_Z_A);
                getArtistFragment().refresh();
            } else if (isAlbumPage()) {
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_Z_A);
                getAlbumFragment().refresh();
            } else if (isSongPage()) {
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_Z_A);
                getSongFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_artist) {
            if (isAlbumPage()) {
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_ARTIST);
                getAlbumFragment().refresh();
            } else if (isSongPage()) {
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                getSongFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_album) {
            if (isSongPage()) {
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                getSongFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_year) {
            if (isAlbumPage()) {
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_YEAR);
                getAlbumFragment().refresh();
            } else if (isSongPage()) {
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
                getSongFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_duration) {
            if (isSongPage()) {
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
                getSongFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_number_of_songs) {
            if (isArtistPage()) {
                mPreferences
                        .setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS);
                getArtistFragment().refresh();
            } else if (isAlbumPage()) {
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                getAlbumFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_number_of_albums) {
            if (isArtistPage()) {
                mPreferences.setArtistSortOrder(SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS);
                getArtistFragment().refresh();
            }
        } else if (id == R.id.menu_sort_by_filename) {
            if (isSongPage()) {
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_FILENAME);
                getSongFragment().refresh();
            }
        } else if (id == R.id.menu_new_playlist) {
            if (isPlaylistPage()) {
                CreateNewPlaylist.getInstance(new long[0])
                        .show(getChildFragmentManager(), "CreatePlaylist");
            }
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected boolean needsElevatedActionBar() {
        // our view pager already has elevation
        return false;
    }

    private boolean isArtistPage() {
        return mViewPager.getCurrentItem() == MusicFragments.ARTIST.ordinal();
    }

    public ArtistFragment getArtistFragment() {
        return (ArtistFragment) mPagerAdapter.getFragment(MusicFragments.ARTIST.ordinal());
    }

    private boolean isAlbumPage() {
        return mViewPager.getCurrentItem() == MusicFragments.ALBUM.ordinal();
    }

    public AlbumFragment getAlbumFragment() {
        return (AlbumFragment) mPagerAdapter.getFragment(MusicFragments.ALBUM.ordinal());
    }

    private boolean isSongPage() {
        return mViewPager.getCurrentItem() == MusicFragments.SONG.ordinal();
    }

    public SongFragment getSongFragment() {
        return (SongFragment) mPagerAdapter.getFragment(MusicFragments.SONG.ordinal());
    }

    @Override
    public void restartLoader() {
        // do nothing
    }

    private boolean isPlaylistPage() {
        return mViewPager.getCurrentItem() == MusicFragments.PLAYLIST.ordinal();
    }
}
