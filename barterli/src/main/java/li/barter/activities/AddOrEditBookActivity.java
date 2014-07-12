package li.barter.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.AddOrEditBookFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;

/**
 * Activity to Add or Edit books. If you're calling Edit book, it will also check if that book is
 * owned by the current user and crash if not
 * <p/>
 * Created by vinay.shenoy on 12/07/14.
 */
public class AddOrEditBookActivity extends AbstractDrawerActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);
        if (savedInstanceState == null) {
            loadAddOrEditBookFragment();
        }
    }

    /** Load the Add or Edit Book fragment. The intent extras will be added to the fragment */
    private void loadAddOrEditBookFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment.instantiate(this,
                                                                                         AddOrEditBookFragment.class
                                                                                                 .getName(),
                                                                                         getIntent()
                                                                                                 .getExtras()
                     ),

                     AppConstants.FragmentTags.ADD_OR_EDIT_BOOK, false, null
        );
    }

    @Override
    protected boolean isDrawerActionBarToggleEnabled() {
        return false;
    }

    @Override
    protected String getAnalyticsScreenName() {
        return null;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId, final IBlRequestContract request,
                          final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId, final IBlRequestContract request, final
    int errorCode, final String errorMessage, final Bundle errorResponseBundle) {

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}