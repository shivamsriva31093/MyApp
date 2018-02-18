package task.myapp.ui.homepage;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.samples.apps.iosched.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import task.myapp.R;
import task.myapp.navigation.NavigationModel;
import task.myapp.widgets.BadgedBottomNavigationView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.bottom_navigation)
    BadgedBottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelfNavDrawerItem(NavigationModel.NavigationItemEnum.HOME);
        setNavigationTitleId(R.string.title_activity_homepage);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initUiComponents(savedInstanceState);
    }

    private void initUiComponents(Bundle savedInstanceState) {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
