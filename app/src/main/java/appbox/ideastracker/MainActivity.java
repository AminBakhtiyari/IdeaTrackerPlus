package appbox.ideastracker;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.thebluealliance.spectrum.SpectrumDialog;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.ArrayList;
import java.util.List;

import appbox.ideastracker.customviews.AnimatedExpandableListView;
import appbox.ideastracker.customviews.NonSwipeableViewPager;
import appbox.ideastracker.customviews.ToolbarColorizeHelper;
import appbox.ideastracker.database.DataEntry;
import appbox.ideastracker.database.DatabaseHelper;
import appbox.ideastracker.database.Project;
import appbox.ideastracker.database.TinyDB;
import appbox.ideastracker.listadapters.MyExandableListAdapter;
import appbox.ideastracker.listadapters.MyListAdapter;
import appbox.ideastracker.recycler.HorizontalAdapter;
import appbox.ideastracker.recycler.RecyclerOnClickListener;
import appbox.ideastracker.recycler.RecyclerOnLongClickListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper mDbHelper;

    // Drawers items
    private Drawer leftDrawer = null;
    private Drawer rightDrawer = null;
    private AccountHeader header = null;
    private SwitchDrawerItem doneSwitch;
    private SwitchDrawerItem cheerSwitch;
    private SwitchDrawerItem bigTextSwitch;
    private PrimaryDrawerItem mColorItem1;
    private PrimaryDrawerItem mColorItem2;
    private PrimaryDrawerItem mColorItem3;

    // UI elements
    private Toolbar mToolbar;
    private FloatingActionButton mFab;
    private FragmentManager mFragmentManager;
    private NonSwipeableViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;

    // Dialogs
    private Dialog mMoveDialog;
    private Dialog mNewIdeaDialog;

    // Dialogs views
    private RadioGroup mRadioGroup;
    private TextView mIdeaError;
    private TextView mMoveError;
    private EditText mIdeaField;
    private Spinner mFromSpinner, mToSpinner;

    // Preferences
    private TinyDB mTinyDB;
    private static final String PREF_KEY = "MyPrefKey";
    private int mPrimaryColor;
    private int mSecondaryColor;
    private int mTextColor;
    private ArrayList<Object> mProjects;
    private List<IProfile> mProfiles;
    private int mSelectedProfileIndex;
    private boolean mNoProject = false;

    // Color preferences
    private int defaultPrimaryColor;
    private int defaultSecondaryColor;
    private int defaultTextColor;

    // Tutorial element
    private ShowcaseView mFirstIdeaguide;


    // STATIC METHODS //

    public static MainActivity getActivity(View v) {

        Context context = v.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof MainActivity) {
                return (MainActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public static MainActivity getActivity(Context context) {

        while (context instanceof ContextWrapper) {
            if (context instanceof MainActivity) {
                return (MainActivity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }


    // OVERRODE METHODS //

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatabaseHelper.setMainActivity(this);

        // Databases
        mTinyDB = new TinyDB(this);
        mDbHelper = DatabaseHelper.getInstance(this);

        introOnFirstStart();

        //Default colors
        defaultPrimaryColor = ContextCompat.getColor(this, R.color.md_blue_grey_800);
        defaultSecondaryColor = ContextCompat.getColor(this, R.color.md_green_a400);
        defaultTextColor = ContextCompat.getColor(this, R.color.md_white);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Fragments manager to populate the tabs
        mFragmentManager = getSupportFragmentManager();
        mSectionsPagerAdapter = new SectionsPagerAdapter(mFragmentManager);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (NonSwipeableViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set up the tab layout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorHeight(10);

        // Wire the floating button
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFirstIdeaguide != null) {
                    mFirstIdeaguide.hide();
                    mFirstIdeaguide = null;
                }
                newIdeaDialog();
            }
        });

        //TABLES
        loadProjects();

        // Set up the drawers and their items
        setUpDrawers(savedInstanceState);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = leftDrawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (leftDrawer != null && leftDrawer.isDrawerOpen()) {
            leftDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mNoProject) rightDrawer.openDrawer();
        return super.onOptionsItemSelected(item);
    }


    // ON CREATE SET UP METHODS //

    // Creates and fill the right and left drawers
    private void setUpDrawers(Bundle savedInstanceState) {

        //HEADER
        header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withProfiles(mProfiles)
                .withProfileImagesVisible(false)
                .withSavedInstance(savedInstanceState)
                .build();

        //SWITCHES
        setUpSwitches();

        //LEFT DRAWER
        leftDrawer = new DrawerBuilder(this)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)
                .withAccountHeader(header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.rename_pro).withIcon(FontAwesome.Icon.faw_i_cursor).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.delete_pro).withIcon(FontAwesome.Icon.faw_trash).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(4).withName(R.string.all_pro).withIcon(GoogleMaterial.Icon.gmd_inbox).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(3).withName(R.string.new_pro).withIcon(FontAwesome.Icon.faw_plus).withSelectable(false),
                        new DividerDrawerItem(),
                        new ExpandableDrawerItem().withName(R.string.settings).withIcon(FontAwesome.Icon.faw_gear).withSelectable(false).withSubItems(
                                doneSwitch, cheerSwitch, bigTextSwitch),
                        new ExpandableDrawerItem().withName(R.string.help_feedback).withIcon(FontAwesome.Icon.faw_question_circle).withSelectable(false).withSubItems(
                                new SecondaryDrawerItem().withName(R.string.see_app_intro).withLevel(2).withIcon(GoogleMaterial.Icon.gmd_camera_rear).withIdentifier(8).withSelectable(false),
                                new SecondaryDrawerItem().withName(R.string.activate_tuto).withLevel(2).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(9).withSelectable(false),
                                new SecondaryDrawerItem().withName(getString(R.string.rate) + getString(R.string.app_name)).withLevel(2).withIcon(GoogleMaterial.Icon.gmd_star).withIdentifier(11).withSelectable(false),
                                new SecondaryDrawerItem().withName(R.string.feedback).withLevel(2).withIcon(GoogleMaterial.Icon.gmd_bug).withIdentifier(10).withSelectable(false),
                                new SecondaryDrawerItem().withName(R.string.source_code).withLevel(2).withIcon(GoogleMaterial.Icon.gmd_github).withIdentifier(12).withSelectable(false))


                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            int id = (int) drawerItem.getIdentifier();
                            switch (id) {
                                case 1:
                                    if (!mNoProject) {
                                        renameProjectDialog();
                                    } else {
                                        noProjectSnack();
                                    }
                                    break;

                                case 2:
                                    if (!mNoProject) {
                                        deleteProjectDialog();
                                    } else {
                                        noProjectSnack();
                                    }
                                    break;

                                case 3:
                                    newProjectDialog();
                                    break;

                                case 4:
                                    if (!mNoProject) {
                                        header.toggleSelectionList(getApplicationContext());
                                    } else {
                                        noProjectSnack();
                                    }
                                    break;

                                case 8:
                                    forceIntro();
                                    break;

                                case 9:
                                    leftDrawer.closeDrawer();
                                    Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content), R.string.tuto_mode, Snackbar.LENGTH_SHORT)
                                            .setCallback(new Snackbar.Callback() {
                                                @Override
                                                public void onDismissed(Snackbar snackbar, int event) {
                                                    mTinyDB.putBoolean(getString(R.string.handle_idea_pref), true);
                                                    mTinyDB.putBoolean(getString(R.string.first_project_pref), true);
                                                    firstIdeaGuide();
                                                }
                                            });
                                    snackbar.show();
                                    break;

                                case 10:
                                    // Open browser to github issues section
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nserguier/IdeasTracker/issues"));
                                    startActivity(browserIntent);
                                    break;

                                case 11:
                                    // Rate
                                    break;

                                case 12:
                                    // Open browser to github source code
                                    Intent browserSource = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/nserguier/IdeasTracker"));
                                    startActivity(browserSource);
                                    break;

                            }
                        }
                        return true;
                    }
                })
                .withOnDrawerListener(new MyDrawerListener())
                .withSavedInstance(savedInstanceState)
                .build();

        mColorItem1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.primary_col).withIcon(FontAwesome.Icon.faw_paint_brush).withIconColor(mPrimaryColor).withSelectable(false);
        mColorItem2 = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.secondary_col).withIcon(FontAwesome.Icon.faw_paint_brush).withIconColor(mSecondaryColor).withSelectable(false);
        mColorItem3 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.text_col).withIcon(FontAwesome.Icon.faw_paint_brush).withIconColor(mTextColor).withSelectable(false);

        //RIGHT DRAWER
        rightDrawer = new DrawerBuilder(this)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        new SectionDrawerItem().withName(R.string.color_prefs),
                        mColorItem1,
                        mColorItem2,
                        mColorItem3,
                        new PrimaryDrawerItem().withIdentifier(6).withName(R.string.reset_color_prefs).withIcon(FontAwesome.Icon.faw_tint).withSelectable(false),
                        new SectionDrawerItem().withName(R.string.functions),
                        new PrimaryDrawerItem().withIdentifier(4).withName(R.string.move_all_ideas).withIcon(FontAwesome.Icon.faw_exchange).withSelectable(false),
                        new PrimaryDrawerItem().withIdentifier(5).withName(R.string.expand_collapse).withIcon(FontAwesome.Icon.faw_arrows_v).withSelectable(false)
                )
                .withDrawerGravity(Gravity.END)
                .withStickyFooter(R.layout.footer)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null && !mNoProject) {
                            int id = (int) drawerItem.getIdentifier();
                            switch (id) {
                                case 1:
                                    new SpectrumDialog.Builder(getApplicationContext())
                                            .setTitle(R.string.select_prim_col)
                                            .setColors(R.array.colors)
                                            .setSelectedColor(mPrimaryColor)
                                            .setDismissOnColorSelected(false)
                                            .setFixedColumnCount(4)
                                            .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                                                @Override
                                                public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                                                    if (positiveResult) {
                                                        //update selected color
                                                        mPrimaryColor = color;
                                                        changePrimaryColor();
                                                        saveProjectColors();

                                                        //change project icon
                                                        Drawable disk = ContextCompat.getDrawable(getApplicationContext(), R.drawable.disk);
                                                        disk.setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);
                                                        IProfile p = header.getActiveProfile();
                                                        p.withIcon(disk);
                                                        header.updateProfile(p);
                                                    }
                                                }
                                            }).build().show(mFragmentManager, "dialog_spectrum");

                                    break;

                                case 2:
                                    new SpectrumDialog.Builder(getApplicationContext())
                                            .setTitle(R.string.select_sec_col)
                                            .setColors(R.array.accent_colors)
                                            .setSelectedColor(mSecondaryColor)
                                            .setDismissOnColorSelected(false)
                                            .setFixedColumnCount(4)
                                            .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                                                @Override
                                                public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                                                    if (positiveResult) {
                                                        //update selected color
                                                        mSecondaryColor = color;
                                                        changeSecondaryColor();
                                                        saveProjectColors();
                                                    }
                                                }
                                            }).build().show(mFragmentManager, "dialog_spectrum");
                                    break;

                                case 3:
                                    new SpectrumDialog.Builder(getApplicationContext())
                                            .setTitle(R.string.select_text_col)
                                            .setColors(R.array.textColors)
                                            .setSelectedColor(mTextColor)
                                            .setDismissOnColorSelected(false)
                                            .setFixedColumnCount(4)
                                            .setOutlineWidth(2)
                                            .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                                                @Override
                                                public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                                                    if (positiveResult) {
                                                        //update selected color
                                                        mTextColor = color;
                                                        changeTextColor();
                                                        saveProjectColors();
                                                    }
                                                }
                                            }).build().show(mFragmentManager, "dialog_spectrum");
                                    break;

                                case 4:
                                    newMoveDialog();
                                    break;

                                case 5:
                                    AnimatedExpandableListView.getInstance().collapseExpandAll();
                                    break;

                                case 6:
                                    resetColorsDialog();
                                    break;
                            }
                        } else {
                            noProjectSnack();
                        }
                        return true;
                    }
                })
                .withOnDrawerListener(new MyDrawerListener())
                .withSavedInstance(savedInstanceState)
                .append(leftDrawer);

        //Select first project if there is any
        if (!mNoProject) {
            mSelectedProfileIndex = 0;
            IProfile activeProfile = mProfiles.get(mSelectedProfileIndex);
            String activeProfileName = activeProfile.getName().getText();
            header.setActiveProfile(activeProfile);

            ActionBar bar;
            if ((bar = getSupportActionBar()) != null) {
                bar.setTitle(activeProfileName);
            }

            DataEntry.setTableName(activeProfileName);
            displayIdeasCount();

            switchToProjectColors();
        } else { // No project

            header.setProfiles(mProfiles);
            header.setSelectionSecondLine(getString(R.string.no_project));
            //reset color
            mPrimaryColor = defaultPrimaryColor;
            mSecondaryColor = defaultSecondaryColor;
            mTextColor = defaultTextColor;
            updateColors();
        }
    }

    // Creates the swicthes displayed in the drawer
    private void setUpSwitches() {

        doneSwitch = new SwitchDrawerItem().withName(R.string.show_done_msg).withLevel(2).withIdentifier(6).withOnCheckedChangeListener(onCheckedChangeListener).withSelectable(false);
        if (mTinyDB.getBoolean(getString(R.string.show_done_pref))) doneSwitch.withChecked(true);
        else toggleDoneTab();

        cheerSwitch = new SwitchDrawerItem().withName(R.string.show_cheer_msg).withLevel(2).withIdentifier(7).withOnCheckedChangeListener(onCheckedChangeListener).withSelectable(false);
        if (mTinyDB.getBoolean(getString(R.string.show_cheer_pref))) cheerSwitch.withChecked(true);

        bigTextSwitch = new SwitchDrawerItem().withName(R.string.big_text_msg).withLevel(2).withIdentifier(20).withOnCheckedChangeListener(onCheckedChangeListener).withSelectable(false);
        if (mTinyDB.getBoolean(getString(R.string.big_text_pref), false)) {
            bigTextSwitch.withChecked(true);
            HorizontalAdapter.setBigText(true);
        }

    }


    // DIALOG METHODS //

    // Shows an idea creation dialog
    public void newIdeaDialog() {

        mNewIdeaDialog = new LovelyCustomDialog(this, R.style.EditTextTintTheme)
                .setView(R.layout.new_idea_form)
                .setTopColor(mPrimaryColor)
                .setTitle("New idea")
                .setIcon(R.drawable.ic_bulb)
                .setListener(R.id.doneButton, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Switch doLater = (Switch) mNewIdeaDialog.findViewById(R.id.doLater);
                        RadioGroup radioGroup = (RadioGroup) mNewIdeaDialog.findViewById(R.id.radioGroup);
                        EditText noteField = (EditText) mNewIdeaDialog.findViewById(R.id.editNote);

                        String text = mIdeaField.getText().toString();
                        if (!text.equals("")) {

                            boolean later = doLater.isChecked();

                            if (radioGroup.getCheckedRadioButtonId() != -1) {
                                View radioButton = radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
                                RadioButton btn = (RadioButton) radioGroup.getChildAt(radioGroup.indexOfChild(radioButton));
                                String selection = (String) btn.getText();

                                String note = noteField.getText().toString();
                                int priority = Integer.parseInt(selection);

                                mDbHelper.newEntry(text, note, priority, later); //add the idea to the actual database
                                displayIdeasCount();

                                DatabaseHelper.notifyAllLists();

                            }

                            mNewIdeaDialog.dismiss();

                            if (mTinyDB.getBoolean(getString(R.string.handle_idea_pref))) {
                                //move tab where idea was created
                                int index = 0;
                                if (later) index = 1;

                                tabLayout.setScrollPosition(index, 0f, true);
                                mViewPager.setCurrentItem(index);

                                //start the handle idea guide
                                handleIdeaGuide();
                            }

                        } else {
                            mIdeaError.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .show();

        mIdeaError = (TextView) mNewIdeaDialog.findViewById(R.id.new_error_message);
        mIdeaField = (EditText) mNewIdeaDialog.findViewById(R.id.editText);
        mIdeaField.addTextChangedListener(new HideErrorOnTextChanged());

    }

    // Shows and idea creation dialog
    // pre-select the given priority
    public void newIdeaDialog(int priority) {

        mNewIdeaDialog = new LovelyCustomDialog(this, R.style.EditTextTintTheme)
                .setView(R.layout.new_idea_form)
                .setTopColor(mPrimaryColor)
                .setTitle("New idea")
                .setIcon(R.drawable.ic_bulb)
                .setListener(R.id.doneButton, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Switch doLater = (Switch) mNewIdeaDialog.findViewById(R.id.doLater);
                        EditText noteField = (EditText) mNewIdeaDialog.findViewById(R.id.editNote);

                        String text = mIdeaField.getText().toString();
                        if (!text.equals("")) {

                            if (mRadioGroup.getCheckedRadioButtonId() != -1) {
                                View radioButton = mRadioGroup.findViewById(mRadioGroup.getCheckedRadioButtonId());
                                RadioButton btn = (RadioButton) mRadioGroup.getChildAt(mRadioGroup.indexOfChild(radioButton));
                                String selection = (String) btn.getText();

                                String note = noteField.getText().toString();
                                boolean later = doLater.isChecked();
                                int priority = Integer.parseInt(selection);

                                mDbHelper.newEntry(text, note, priority, later); //add the idea to the actual database

                                DatabaseHelper.notifyAllLists();
                                displayIdeasCount();

                            }

                            mNewIdeaDialog.dismiss();
                        } else {
                            mIdeaError.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .show();

        //set up the error message
        mIdeaError = (TextView) mNewIdeaDialog.findViewById(R.id.new_error_message);
        mIdeaField = (EditText) mNewIdeaDialog.findViewById(R.id.editText);
        mIdeaField.addTextChangedListener(new HideErrorOnTextChanged());

        //check the right priority radio button
        mRadioGroup = (RadioGroup) mNewIdeaDialog.findViewById(R.id.radioGroup);
        RadioButton radio = (RadioButton) mNewIdeaDialog.findViewById(R.id.radioButton1);
        switch (priority) {
            case 1:
                radio = (RadioButton) mNewIdeaDialog.findViewById(R.id.radioButton1);
                break;
            case 2:
                radio = (RadioButton) mNewIdeaDialog.findViewById(R.id.radioButton2);
                break;
            case 3:
                radio = (RadioButton) mNewIdeaDialog.findViewById(R.id.radioButton3);
                break;
        }
        radio.setChecked(true);


    }

    // Shows a dialog allowing to move ideas from tab to tab
    private void newMoveDialog() {

        final View root = findViewById(R.id.main_content);

        mMoveDialog = new LovelyCustomDialog(this)
                .setView(R.layout.move_dialog)
                .setTopColor(mPrimaryColor)
                .setTitle("Move all ideas")
                .setIcon(R.drawable.ic_transfer)
                .setListener(R.id.move_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String from = mFromSpinner.getSelectedItem().toString();
                        final String to = mToSpinner.getSelectedItem().toString();

                        String snackText = "";
                        String errorText = getString(R.string.nothing_move) + from;
                        boolean success = false;
                        if (from.equals(to)) errorText = getString(R.string.must_diff);
                        else if (mDbHelper.moveAllFromTo(from, to)) {
                            snackText = "All ideas from " + from + " moved to " + to;
                            success = true;
                            displayIdeasCount();
                        }

                        Snackbar snackbar = Snackbar.make(root, snackText, Snackbar.LENGTH_LONG);
                        if (success) {
                            snackbar.setAction(R.string.undo, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (to.equals(getString(R.string.trash))) {//undo temp deleting
                                        mDbHelper.recoverAllFromTemp();
                                    } else {
                                        mDbHelper.moveAllFromTo(to, from);
                                    }
                                    displayIdeasCount();
                                }
                            }).setCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    if ((event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) && to.equals(getString(R.string.trash))) {
                                        //delete for real ideas in temp
                                        mDbHelper.deleteAllFromTemp();
                                    }
                                }
                            });
                            mMoveDialog.dismiss();
                            rightDrawer.closeDrawer();
                            snackbar.show();
                        } else {
                            mMoveError.setText(errorText);
                            mMoveError.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .show();

        mMoveError = (TextView) mMoveDialog.findViewById(R.id.move_error_message);
        mFromSpinner = (Spinner) mMoveDialog.findViewById(R.id.spinner_from);
        mToSpinner = (Spinner) mMoveDialog.findViewById(R.id.spinner_to);

        mFromSpinner.setOnItemSelectedListener(new HideErrorOnSpinnerChanged());
        mToSpinner.setOnItemSelectedListener(new HideErrorOnSpinnerChanged());

    }

    // Shows a project creation dialog
    private void newProjectDialog() {

        new LovelyTextInputDialog(this, R.style.EditTextTintTheme)
                .setTopColor(mPrimaryColor)
                .setConfirmButtonColor(ContextCompat.getColor(this, R.color.md_pink_a200))
                .setTitle(R.string.new_pro)
                .setMessage(R.string.new_pro_message)
                .setIcon(R.drawable.ic_notepad)
                .setInputFilter(R.string.error_empty_taken, new LovelyTextInputDialog.TextFilter() {
                    @Override
                    public boolean check(String text) {
                        return isProjectNameAvailable(text) && !text.equals("");
                    }
                })
                .setConfirmButton(R.string.create, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String tableName) {

                        mDbHelper.newTable(tableName);

                        //create the profile with its colored icon
                        Drawable disk = ContextCompat.getDrawable(getApplicationContext(), R.drawable.disk);
                        disk.setColorFilter(defaultPrimaryColor, PorterDuff.Mode.SRC_ATOP);
                        IProfile newProfile = new ProfileDrawerItem().withName(tableName).withIcon(disk).withOnDrawerItemClickListener(profile_listener);
                        mProfiles.add(newProfile);

                        saveProject(new Project(tableName, defaultPrimaryColor, defaultSecondaryColor, defaultTextColor));

                        //open the profile drawer and select the new profile
                        header.setActiveProfile(newProfile);
                        mSelectedProfileIndex = mProfiles.size() - 1;
                        switchToProjectColors();

                        leftDrawer.openDrawer();
                        header.toggleSelectionList(getApplicationContext());
                        mToolbar.setTitle(tableName);
                        displayIdeasCount();

                        if (mNoProject) {
                            mFab.setVisibility(View.VISIBLE);
                            mNoProject = false;

                            mViewPager.setAdapter(null);
                            mViewPager.setAdapter(mSectionsPagerAdapter);
                        }

                        if (mTinyDB.getBoolean(getString(R.string.first_project_pref)))
                            firstProjectGuide();
                    }
                })
                .show();
    }

    // Show a dialog to rename the current project
    private void renameProjectDialog() {

        new LovelyTextInputDialog(this, R.style.EditTextTintTheme)
                .setTopColor(mPrimaryColor)
                .setConfirmButtonColor(ContextCompat.getColor(this, R.color.md_pink_a200))
                .setTitle("Rename " + ((Project) mProjects.get(mSelectedProfileIndex)).getName())
                .setMessage(R.string.rename_pro_message)
                .setIcon(R.drawable.ic_edit)
                .setInputFilter(R.string.error_empty_taken, new LovelyTextInputDialog.TextFilter() {
                    @Override
                    public boolean check(String text) {
                        return !text.equals("") && isProjectNameAvailable(text);
                    }
                })
                .setConfirmButton(R.string.rename, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String tableName) {
                        //update table's name is the list and the database
                        renameProject(tableName);
                        mDbHelper.renameTable(tableName);

                        //update profile's name
                        IProfile profile = mProfiles.get(mSelectedProfileIndex);
                        profile.withName(tableName);
                        header.updateProfile(profile);
                        mProfiles.remove(mSelectedProfileIndex);
                        mProfiles.add(mSelectedProfileIndex, profile);

                        mToolbar.setTitle(tableName);
                    }
                })
                .show();
    }

    // Shows a dialog to delete the current project
    private void deleteProjectDialog() {
        new LovelyStandardDialog(this)
                .setTopColorRes(R.color.md_red_400)
                .setButtonsColorRes(R.color.md_deep_orange_500)
                .setIcon(R.drawable.ic_warning)
                .setTitle("Delete project '" + ((Project) mProjects.get(mSelectedProfileIndex)).getName() + "'")
                .setMessage(R.string.delete_pro_message)
                .setPositiveButton(R.string.delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProfiles.remove(mSelectedProfileIndex);
                        deleteProject();
                        mDbHelper.deleteTable();
                        if (mProjects.isEmpty()) {
                            DataEntry.setTableName("");
                            mToolbar.setTitle(R.string.app_name);
                            mFab.setVisibility(View.INVISIBLE);
                            header.setProfiles(mProfiles);
                            header.setSelectionSecondLine(getString(R.string.no_project));
                            mNoProject = true;

                            mViewPager.setAdapter(null);
                            mViewPager.setAdapter(mSectionsPagerAdapter);

                            //reset color
                            mPrimaryColor = defaultPrimaryColor;
                            mSecondaryColor = defaultSecondaryColor;
                            mTextColor = defaultTextColor;
                            updateColors();
                        }
                        switchToExistingProject(mSelectedProfileIndex);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    // Shows a dialog to reset color preferences to default
    private void resetColorsDialog() {
        new LovelyStandardDialog(this)
                .setTopColor(mPrimaryColor)
                .setButtonsColorRes(R.color.md_pink_a200)
                .setIcon(R.drawable.ic_drop)
                .setTitle(R.string.reset_color_prefs)
                .setMessage(R.string.reset_color_pref_message)
                .setPositiveButton(android.R.string.yes, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPrimaryColor = defaultPrimaryColor;
                        mSecondaryColor = defaultSecondaryColor;
                        mTextColor = defaultTextColor;
                        saveProjectColors();
                        updateColors();

                        //change project icon
                        Drawable disk = ContextCompat.getDrawable(getApplicationContext(), R.drawable.disk);
                        disk.setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);
                        IProfile p = header.getActiveProfile();
                        p.withIcon(disk);
                        header.updateProfile(p);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    /**
     * After project deletion, selects another project
     *
     * @param index the index of the deleted project
     */
    private void switchToExistingProject(int index) {
        index -= 1;
        boolean inBounds = (index >= 0) && (index < mProfiles.size());

        if (!mProfiles.isEmpty()) {

            if (inBounds) mSelectedProfileIndex = index;
            else mSelectedProfileIndex = 0;

            IProfile profileToSelect = mProfiles.get(mSelectedProfileIndex);
            String tableToSelect = profileToSelect.getName().getText();
            header.setActiveProfile(profileToSelect);
            mToolbar.setTitle(tableToSelect);
            mDbHelper.switchTable(tableToSelect);
            displayIdeasCount();

            switchToProjectColors();
        }

    }


    // TUTORIAL AND INTRO METHODS //

    // Launch the app introduction only for the first start
    private void introOnFirstStart() {
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                //  Create a new boolean and preference and set it to true
                boolean firstStart = mTinyDB.getBoolean("firstStart");

                //  If the activity has never started before...
                if (firstStart) {

                    forceIntro();

                    mTinyDB.putBoolean("firstStart", false);
                }
            }
        });

        t.start();
    }

    // Launch the app introduction
    private void forceIntro() {
        Intent i = new Intent(MainActivity.this, MyIntro.class);
        startActivity(i);
    }

    // Shows the tutorial for the first project creation
    private void firstProjectGuide() {

        new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(header.getView()))
                .setContentTitle(getString(R.string.first_project_title))
                .setStyle(R.style.CustomShowcaseTheme2)
                .replaceEndButton(R.layout.got_it)
                .setContentText(getString(R.string.first_project_content))
                .blockAllTouches()
                .build()
                .show();
        mTinyDB.putBoolean(getString(R.string.first_project_pref), false);
    }

    // Shows the tutorial for the first idea creation
    private void firstIdeaGuide() {

        mFirstIdeaguide = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(mFab))
                .setContentTitle(getString(R.string.first_idea_title))
                .setStyle(R.style.CustomShowcaseTheme2)
                .replaceEndButton(R.layout.empty_layout)
                .build();

        mFirstIdeaguide.show();

        rightDrawer.closeDrawer();
        leftDrawer.closeDrawer();
        mTinyDB.putBoolean(getString(R.string.first_idea_pref), false);
    }

    // Shows the tutorial on how interacting with ideas
    private void handleIdeaGuide() {

        View firstIdea = findViewById(R.id.firstIdea);

        mFirstIdeaguide = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(firstIdea))
                .setContentTitle(getString(R.string.handle_idea_title))
                .setContentText(getString(R.string.handle_idea_content))
                .setStyle(R.style.CustomShowcaseTheme2)
                .replaceEndButton(R.layout.got_it)
                .blockAllTouches()
                .build();

        mFirstIdeaguide.show();
        mTinyDB.putBoolean(getString(R.string.handle_idea_pref), false);
    }


    // UI COLOR METHODS //

    @SuppressWarnings("ConstantConditions")
    private void changePrimaryColor() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        toolbar.setBackgroundColor(mPrimaryColor);
        tabLayout.setBackgroundColor(mPrimaryColor);
        appbar.setBackgroundColor(mPrimaryColor);

        if (Build.VERSION.SDK_INT >= 21) {
            //getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getWindow().setStatusBarColor(darken(mPrimaryColor));
        }

        mColorItem1.withIconColor(mPrimaryColor);
        rightDrawer.updateItem(mColorItem1);

        RecyclerOnClickListener.setPrimaryColor(mPrimaryColor);
        RecyclerOnLongClickListener.setPrimaryColor(mPrimaryColor);
    }

    private void changeSecondaryColor() {

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        tabLayout.setSelectedTabIndicatorColor(mSecondaryColor);
        mFab.setBackgroundTintList(ColorStateList.valueOf(mSecondaryColor));

        mColorItem2.withIconColor(mSecondaryColor);
        rightDrawer.updateItem(mColorItem2);
    }

    private void changeTextColor() {

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        tabLayout.setTabTextColors(slightDarken(mTextColor), mTextColor);
        mToolbar.setTitleTextColor(mTextColor);

        ToolbarColorizeHelper.colorizeToolbar(mToolbar, mTextColor, this);

        mColorItem3.withIconColor(mTextColor);
        rightDrawer.updateItem(mColorItem3);

    }

    // Change all UI colors to match the color attributes
    private void updateColors() {
        changePrimaryColor();
        changeSecondaryColor();
        changeTextColor();
    }

    // Change all UI colors to match the selected project preferences
    private void switchToProjectColors() {
        Project selectedProject = (Project) mProjects.get(mSelectedProfileIndex);
        mPrimaryColor = selectedProject.getPrimaryColor();
        mSecondaryColor = selectedProject.getSecondaryColor();
        mTextColor = selectedProject.getTextColor();

        updateColors();

        mColorItem1.withIconColor(mPrimaryColor);
        mColorItem2.withIconColor(mSecondaryColor);
        mColorItem3.withIconColor(mTextColor);

        rightDrawer.updateItem(mColorItem1);
        rightDrawer.updateItem(mColorItem2);
        rightDrawer.updateItem(mColorItem3);

    }

    // Makes a color darker
    private int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.85f;
        color = Color.HSVToColor(hsv);
        return color;
    }

    // Makes a color slightly darker
    private int slightDarken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.90f;
        color = Color.HSVToColor(hsv);
        return color;
    }


    // UI METHODS //

    // Shows/hide the DONE tab
    private void toggleDoneTab() {

        int count = tabLayout.getTabCount();

        for (int i = 0; i < count; i++) {
            if (tabLayout.getTabAt(i).getText().equals("Done")) {
                tabLayout.removeTabAt(i);
                mSectionsPagerAdapter.setTabCount(2);
                mViewPager.setAdapter(null);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mTinyDB.putBoolean(getString(R.string.show_done_pref), false);
                return;
            }
        }
        tabLayout.addTab(tabLayout.newTab().setText("Done"));
        mSectionsPagerAdapter.setTabCount(3);
        mViewPager.setAdapter(null);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTinyDB.putBoolean(getString(R.string.show_done_pref), true);


    }


    // PROJECT METHODS //

    // Saves a project in the TinyDB
    private void saveProject(Project p) {

        if (mProjects == null) {
            mProjects = new ArrayList<>();
        }
        mProjects.add(p);

        // save the project list to preference
        mTinyDB.putListObject(PREF_KEY, mProjects);

    }

    // Saves color preferences for the current project
    private void saveProjectColors() {
        Project p = (Project) mProjects.get(mSelectedProfileIndex);
        p.setPrimaryColor(mPrimaryColor);
        p.setSecondaryColor(mSecondaryColor);
        p.setTextColor(mTextColor);
        mTinyDB.putListObject(PREF_KEY, mProjects);
    }

    private void renameProject(String newName) {
        Project p = (Project) mProjects.get(mSelectedProfileIndex);
        p.setName(newName);
        mTinyDB.putListObject(PREF_KEY, mProjects);
    }

    private boolean isProjectNameAvailable(String name) {

        for (Object o : mProjects) {
            Project p = (Project) o;
            if (p.getName().equalsIgnoreCase(name)) return false;
        }
        return true;
    }

    private void deleteProject() {
        mProjects.remove(mSelectedProfileIndex);
        mTinyDB.putListObject(PREF_KEY, mProjects);
    }

    // Fills the project and profile lists with the projects saved in the TinyDB
    private void loadProjects() {

        mProjects = mTinyDB.getListObject(PREF_KEY, Project.class);
        if (mProjects.size() == 0) {
            DataEntry.setTableName("");
            mToolbar.setTitle(R.string.app_name);
            mFab.setVisibility(View.INVISIBLE);
            mNoProject = true;
        }

        mProfiles = new ArrayList<>();
        for (Object p : mProjects) {
            Project project = (Project) p;
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.disk);
            drawable.setColorFilter(project.getPrimaryColor(), PorterDuff.Mode.SRC_ATOP);
            mProfiles.add(new ProfileDrawerItem().withName(project.getName())
                    .withIcon(drawable)
                    .withOnDrawerItemClickListener(profile_listener));
        }
    }

    public void displayIdeasCount() {
        int count = mDbHelper.getIdeasCount();
        if (count == 0) {
            header.setSelectionSecondLine(getString(R.string.no_ideas));
        } else if (count == 1) {
            header.setSelectionSecondLine(count + " idea");
        } else {
            header.setSelectionSecondLine(count + " ideas");
        }

    }

    // Shows a snackbar message to tell the user there's no project
    private void noProjectSnack() {
        leftDrawer.closeDrawer();
        rightDrawer.closeDrawer();
        Snackbar.make(findViewById(R.id.main_content), R.string.no_project_snack_message, Snackbar.LENGTH_LONG).show();
    }


    // FRAGMENT CLASSES //

    /**
     * Fragment containing the listView to be displayed in each tab
     */
    public static class ListFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private static MainActivity mainActivity;

        public static ListFragment newInstance(String tabName) {
            ListFragment f = new ListFragment();

            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putString("tabName", tabName);
            f.setArguments(args);

            return f;
        }

        public String getTabName() {
            return getArguments().getString("tabName");
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            mainActivity = MainActivity.getActivity(container);

            View rootView = null;
            if (DataEntry.TABLE_NAME.equals("[]")) {
                rootView = inflater.inflate(R.layout.no_project_layout, container, false);
                LinearLayout lin = (LinearLayout) rootView.findViewById(R.id.noProject);
                lin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mainActivity.newProjectDialog();
                    }
                });
                return rootView;
            }

            switch (this.getTabName()) {
                case "Ideas": //IDEAS
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    AnimatedExpandableListView list = (AnimatedExpandableListView) rootView.findViewById(R.id.expandableList);
                    //sets the adapter that provides data to the list
                    MyExandableListAdapter adapter = new MyExandableListAdapter(getContext());
                    DatabaseHelper.setAdapterIdea(adapter);
                    list.setAdapter(adapter);
                    list.expandGroup(0);
                    list.expandGroup(1);
                    list.expandGroup(2);
                    setListeners(list);

                    break;

                case "Later": //LATER
                    rootView = inflater.inflate(R.layout.fragment_secondary, container, false);
                    ListView list2 = (ListView) rootView.findViewById(R.id.list);
                    MyListAdapter adapter2 = new MyListAdapter(getContext(), true);
                    DatabaseHelper.setAdapterLater(adapter2);
                    list2.setAdapter(adapter2);
                    break;

                case "Done": //DONE
                    rootView = inflater.inflate(R.layout.fragment_secondary, container, false);
                    ListView list3 = (ListView) rootView.findViewById(R.id.list);
                    MyListAdapter adapter3 = new MyListAdapter(getContext(), false);
                    DatabaseHelper.setAdapterDone(adapter3);
                    list3.setAdapter(adapter3);
                    break;

            }


            return rootView;
        }

        void setListeners(final AnimatedExpandableListView listView) {
            listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                    if (listView.getExpandableListAdapter().getChildrenCount(groupPosition) != 0) { //group is not empty
                        if (listView.isGroupExpanded(groupPosition)) {
                            listView.collapseGroupWithAnimation(groupPosition);
                        } else {
                            listView.expandGroupWithAnimation(groupPosition);
                        }
                    } else { //group is empty
                        mainActivity.newIdeaDialog(groupPosition + 1);

                    }
                    return true;
                }

            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    int itemType = ExpandableListView.getPackedPositionType(id);

                    if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                        int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                        mainActivity.newIdeaDialog(groupPosition + 1);
                        return true;

                    } else {
                        // null item; we don't consume the click
                        return false;
                    }
                }
            });

        }

    }

    /**
     * Fragment adapter creating the right fragment for the right tab
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private int tabCount = 3;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setTabCount(int count) {
            tabCount = count;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            return ListFragment.newInstance(tabLayout.getTabAt(position).getText().toString());
        }

        @Override
        public int getCount() {
            return tabCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.first_tab);
                case 1:
                    return getString(R.string.second_tab);
                case 2:
                    return getString(R.string.third_tab);
            }
            return null;
        }
    }


    // LISTENERS //

    private class HideErrorOnTextChanged implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mIdeaError.setVisibility(View.GONE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class HideErrorOnSpinnerChanged implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mMoveError.setVisibility(View.GONE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    // Click listener for drawer profiles
    private Drawer.OnDrawerItemClickListener profile_listener = new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

            if (drawerItem != null && drawerItem instanceof IProfile) {
                mSelectedProfileIndex = mProfiles.indexOf(drawerItem);
                String tableName = ((IProfile) drawerItem).getName().getText(MainActivity.this);
                mToolbar.setTitle(tableName);
                mDbHelper.switchTable(tableName);
                displayIdeasCount();
                switchToProjectColors();
            }
            return false;
        }
    };

    // Listener to trigger tutorial when drawer is closed
    private class MyDrawerListener implements Drawer.OnDrawerListener {

        @Override
        public void onDrawerOpened(View drawerView) {

        }

        @Override
        public void onDrawerClosed(View drawerView) {

            if (mTinyDB.getBoolean(getString(R.string.first_idea_pref)) && !mNoProject) {
                firstIdeaGuide();
            }
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
        }
    }

    // Listener for the settings switches in the left drawer
    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {

            int id = (int) drawerItem.getIdentifier();
            switch (id) {
                case 5:
                    toggleDoneTab();
                    break;

                case 6:
                    toggleDoneTab();
                    break;

                case 7:
                    if (isChecked) {
                        mTinyDB.putBoolean(getString(R.string.show_cheer_pref), true);
                    } else {
                        mTinyDB.putBoolean(getString(R.string.show_cheer_pref), false);
                    }
                    break;

                case 20:
                    if (isChecked) {
                        HorizontalAdapter.setBigText(true);
                        mTinyDB.putBoolean(getString(R.string.big_text_pref), true);
                        DatabaseHelper.notifyAllLists();

                    } else {
                        HorizontalAdapter.setBigText(false);
                        mTinyDB.putBoolean(getString(R.string.big_text_pref), false);
                        DatabaseHelper.notifyAllLists();

                    }
            }


        }
    };

}
