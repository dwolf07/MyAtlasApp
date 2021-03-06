package com.layer.atlas.messenger;

import android.os.Bundle;

import com.layer.atlas.Atlas;
import com.layer.atlas.AtlasParticipantPicker;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;


/**
 * @author Oleg Orlov
 * @since 24 Apr 2015
 */
public class AtlasParticipantPickersScreen extends Activity {

    /** String[] with selected participants to use as a result*/
    public static final String EXTRA_KEY_USERIDS_SELECTED = "userids.selected";
    /** String[] of userIDs that cannot be selected */
    public static final String EXTRA_KEY_USERIDS_SKIP = "userids.skip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atlas_screen_participants_picker);
        
        String[] skipUserIds = getIntent().getExtras().getStringArray(EXTRA_KEY_USERIDS_SKIP);
        
        View addBtn = findViewById(R.id.atlas_screen_participants_picker_add);
        View cancelBtn = findViewById(R.id.atlas_screen_participants_picker_cancel);
        
        final AtlasParticipantPicker participantsPicker = (AtlasParticipantPicker) findViewById(R.id.atlas_screen_participants_picker_picker);
        participantsPicker.init(skipUserIds, ((App101) getApplication()).getParticipantProvider());
        
        addBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String[] selectedUserIds = participantsPicker.getSelectedUserIds();
                if (selectedUserIds.length == 0) return;
                Intent result = new Intent();
                result.putExtra(EXTRA_KEY_USERIDS_SELECTED, selectedUserIds);
                setResult(RESULT_OK, result);
                finish();
            }
        });
        
        cancelBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        
        prepareActionBar();
    }

    private void prepareActionBar() {
        ((TextView)findViewById(R.id.atlas_actionbar_title_text)).setText("Add People");
        final ImageView btnMenuLeft = (ImageView)findViewById(R.id.atlas_actionbar_left_btn);
        btnMenuLeft.setImageResource(R.drawable.atlas_ctl_btn_back);
        btnMenuLeft.setVisibility(View.VISIBLE);
        btnMenuLeft.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
    
}
