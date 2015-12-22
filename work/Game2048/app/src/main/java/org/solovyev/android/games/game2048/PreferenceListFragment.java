/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.games.game2048;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ListView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class PreferenceListFragment extends ListFragment {

	public static final int NO_THEME = -1;
	public static final int NO_LAYOUT = -1;

	public static final String FRAGMENT_TAG = "preferences";

	private static final String ARG_PREFERENCES_RES_ID = "preferences_res_id";
	private static final String ARG_LAYOUT_RES_ID = "layout_res_id";
	private static final String ARG_THEME_RES_ID = "theme_res_id";

	private PreferenceManagerCompat preferenceManager;

	private View root;

	private int preferencesResId;

	private int layoutResId = NO_LAYOUT;

	private int themeResId = NO_THEME;

	private Context themeContext;

	//must be provided
	public PreferenceListFragment() {
	}

	@Nonnull
	public static Bundle newPreferencesArguments(int preferencesResId) {
		return newPreferencesArguments(preferencesResId, NO_LAYOUT);
	}

	@Nonnull
	public static Bundle newPreferencesArguments(int preferencesResId, int layoutResId) {
		return newPreferencesArguments(preferencesResId, layoutResId, NO_THEME);
	}

	@Nonnull
	public static Bundle newPreferencesArguments(int preferencesResId, int layoutResId, int themeResId) {
		final Bundle args = new Bundle();

		args.putInt(ARG_PREFERENCES_RES_ID, preferencesResId);
		args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
		args.putInt(ARG_THEME_RES_ID, themeResId);

		return args;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferenceManager = new PreferenceManagerCompat(this);

		final Bundle arguments = getArguments();
		preferencesResId = arguments.getInt(ARG_PREFERENCES_RES_ID);
		layoutResId = arguments.getInt(ARG_LAYOUT_RES_ID);
		themeResId = arguments.getInt(ARG_THEME_RES_ID);
	}

	protected void prepareListView(@Nonnull ListView lv) {
		lv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final LayoutInflater themeInflater;

		if (themeResId == NO_THEME) {
			themeContext = getActivity();
			themeInflater = inflater;
		} else {
			themeContext = new ContextThemeWrapper(getActivity(), themeResId);
			themeInflater = LayoutInflater.from(themeContext);
		}

		if (layoutResId == NO_LAYOUT) {
			final FrameLayout frameLayout = new FrameLayout(themeContext);
			final ListView listView = new ListView(themeContext);
			listView.setId(android.R.id.list);
			frameLayout.addView(listView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
			root = frameLayout;
		} else {
			root = themeInflater.inflate(layoutResId, null);
		}

		final ListView lv = (ListView) root.findViewById(android.R.id.list);
		prepareListView(lv);

		onCreateView(themeContext, themeInflater, root, container, savedInstanceState);

		return root;
	}

	protected void onCreateView(@Nonnull Context context, @Nonnull LayoutInflater inflater, @Nonnull View root, @Nullable ViewGroup container, @Nullable Bundle b) {
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		preferenceManager.addPreferencesFromResource(preferencesResId, themeContext);
		preferenceManager.postBindPreferences();

		final Activity activity = getActivity();
		if (activity instanceof OnPreferenceAttachedListener) {
			final PreferenceScreen preferenceScreen = preferenceManager.getPreferenceScreen();
			if (preferenceScreen != null) {
				((OnPreferenceAttachedListener) activity).onPreferenceAttached(preferenceScreen, preferencesResId);
			}
		}
	}

	@Nullable
	public PreferenceScreen getPreferenceScreen() {
		return preferenceManager.getPreferenceScreen();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		final ViewParent rootParent = root.getParent();
		if (rootParent != null) {
			((ViewGroup) rootParent).removeView(root);
		}
	}

	@Override
	public void onPause() {
		preferenceManager.onPause();
		super.onPause();
	}

	@Override
	public void onStop() {
		preferenceManager.onStop();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		root = null;
		preferenceManager.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		preferenceManager.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Adds preferences from activities that match the given {@link android.content.Intent}.
	 *
	 * @param intent The {@link android.content.Intent} to query activities.
	 */
	public void addPreferencesFromIntent(Intent intent) {
		throw new RuntimeException("too lazy to include this bs");
	}


	/**
	 * Finds a {@link android.preference.Preference} based on its key.
	 *
	 * @param key The key of the preference to retrieve.
	 * @return The {@link android.preference.Preference} with the key, or null.
	 * @see android.preference.PreferenceGroup#findPreference(CharSequence)
	 */
	public Preference findPreference(CharSequence key) {
		return preferenceManager.findPreference(key);
	}

	public interface OnPreferenceAttachedListener {
		public void onPreferenceAttached(PreferenceScreen preferenceScreen, int preferenceResId);
	}

	public int getPreferencesResId() {
		return preferencesResId;
	}
}